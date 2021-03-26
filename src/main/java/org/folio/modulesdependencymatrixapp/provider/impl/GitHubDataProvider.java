package org.folio.modulesdependencymatrixapp.provider.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.modulesdependencymatrixapp.entity.Module;
import org.folio.modulesdependencymatrixapp.entity.Release;
import org.folio.modulesdependencymatrixapp.json.UIModuleDeserializer;
import org.folio.modulesdependencymatrixapp.provider.DataProvider;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GitHubDataProvider implements DataProvider {

    public static final String ARTIFACT_ID_X_PATH = "/project/artifactId";
    public static final String RAML_MODULE_BUILDER_VERSION_X_PATH = "/project/properties/raml-module-builder.version";
    private static final Logger logger = LogManager.getLogger(GitHubDataProvider.class);
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "https://raw.githubusercontent.com/folio-org/";
    private static final String POM_XML = "pom.xml";
    private static final String RELEASES_URL = "https://api.github.com/repos/folio-org/*/releases";

    private final GitHub gitHub;
    private static String token;

    private final List<GHRepository> repositories;
    private final List<List<GHRepository>> batches;

    public GitHubDataProvider(String gitHubToken) throws IOException {
        token = gitHubToken;
        gitHub = new GitHubBuilder().withOAuthToken(token).build();
        repositories = getRepositories();
        batches = splitIntoBatches(repositories,10);
    }

    public List<Module> getDataFromMaster() {
        Map<String, String> descriptors = new HashMap<>();
        List<CompletableFuture<Map<String, String>>> futures = new ArrayList<>();

        logger.info("Getting data from the master.");

        for (List<GHRepository> batch : batches) {
            futures.add(getFileContentForMasterAsync(batch));
        }

        for (CompletableFuture<Map<String, String>> future : futures) {
            try {
                descriptors.putAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return mapDescriptorsToModules(descriptors);
    }

    @Override
    public List<Module> getDataFromTag(int number) {
        Map<String, String> descriptors = new HashMap<>();
        List<CompletableFuture<Map<String, String>>> futures = new ArrayList<>();

        logger.info("Getting data from a tag with index: {}.", number);

        for (List<GHRepository> batch : batches) {
            futures.add(getFileContentForTagAsync(batch, number));
        }

        for (CompletableFuture<Map<String, String>> future : futures) {
            try {
                descriptors.putAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return mapDescriptorsToModules(descriptors);
    }

    private List<GHRepository> getRepositories() {
        try {
            var organization = gitHub.getOrganization("folio-org");
            var repositoriesMap = organization.getRepositories();

            return repositoriesMap
                    .values()
                    .stream()
                    .filter(repo -> repo.getName().startsWith("edge-") || repo.getName().startsWith("mod-") || repo.getName().startsWith("ui-"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error(e);
        }
        return new ArrayList<>();
    }

    private static List<List<GHRepository>> splitIntoBatches(List<GHRepository> repositories, int batchSize) {
        List<List<GHRepository>> batches = new ArrayList<>();
        int index = 0;
        while (index < repositories.size()) {
            batches.add(repositories.subList(index, Math.min(index + batchSize, repositories.size())));
            index += batchSize;
        }
        return batches;
    }

    private static CompletableFuture<Map<String, String>> getFileContentForTagAsync(List<GHRepository> repositories, int index) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> descriptors = new HashMap<>();
            repositories.forEach(repository -> {
                try {
                    if (repository.getName().startsWith("ui-")) {
                        descriptors.put(repository.getName(), getFileContentForTag(index, repository, "package.json"));
                    } else {
                        descriptors.put(repository.getName(), getFileContentForTag(index, repository, "descriptors/ModuleDescriptor-template.json"));
                    }
                    logger.info("Module: {} Tag index: {}", repository.getName(), index);
                } catch (IOException e) {
                    logger.error("{} doesn't have package.json or ModuleDescriptor-template.json (search by Tag)", repository.getName());
                }
            });

            return descriptors;
        });
    }

    private static CompletableFuture<Map<String, String>> getFileContentForMasterAsync(List<GHRepository> repositories) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> descriptors = new HashMap<>();

            repositories.forEach(repository -> {
                try {
                    if (repository.getName().startsWith("ui-")) {
                        descriptors.put(repository.getName(), getFileContentForMaster(repository, "package.json"));
                    } else {
                        descriptors.put(repository.getName(), getFileContentForMaster(repository, "descriptors/ModuleDescriptor-template.json"));
                    }
                    logger.info("Module: {} Master", repository.getName());
                } catch (IOException e) {
                    logger.error("{} doesn't have package.json or ModuleDescriptor-template.json (Master)", repository.getName());
                }
            });

            return descriptors;
        });
    }

    private static String getFileContentForTag(int tagNumber, GHRepository repository, String filePackageName) throws IOException {
        var tags = repository.listTags().toList();
        String tag = "master";

        if (!tags.isEmpty() && tagNumber < tags.size()) {
            tag = tags.get(tagNumber).getName();
        }

        String uriPom = BASE_URL + repository.getName() + "/master/" + POM_XML;
        String uriPackage = BASE_URL + repository.getName() + "/" + tag + "/" + filePackageName;
        String body = "";


        HttpRequest requestPom = HttpRequest.newBuilder()
                .uri(URI.create(uriPom))
                .header("Authorization", "token " + token)
                .build();

        HttpRequest requestPackage = HttpRequest.newBuilder()
                .uri(URI.create(uriPackage))
                .header("Authorization", "token " + token)
                .build();

        HttpRequest requestReleases = HttpRequest.newBuilder()
                .uri(URI.create(RELEASES_URL.replace("*", repository.getName())))
                .header("Authorization", "token " + token)
                .build();

        try {
            var moduleDescriptorResponsePackageFuture = client.sendAsync(requestPackage, HttpResponse.BodyHandlers.ofString());
            var moduleDescriptorResponsePomFuture = client.sendAsync(requestPom, HttpResponse.BodyHandlers.ofString());
            var releasesResponseFuture = client.sendAsync(requestReleases, HttpResponse.BodyHandlers.ofString());

            CompletableFuture.allOf(moduleDescriptorResponsePackageFuture, moduleDescriptorResponsePomFuture, releasesResponseFuture).get();

            HttpResponse<String> moduleDescriptorResponsePackage = moduleDescriptorResponsePackageFuture.get();
            HttpResponse<String> moduleDescriptorResponsePom = moduleDescriptorResponsePomFuture.get();
            HttpResponse<String> releasesResponse = releasesResponseFuture.get();

            List<Release> releaseList = new ArrayList<>();
            String releaseDate = "none";
            try {
                if (releasesResponse.body() != null && !releasesResponse.body().isEmpty()) {
                    releaseList = new ObjectMapper().readValue(releasesResponse.body(), new TypeReference<List<Release>>() {
                    });
                    if (!releaseList.isEmpty()) {
                        releaseDate = releaseList.get(0).getPublishedAt().toString();
                    }
                }
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            body = builderToJson(moduleDescriptorResponsePom.body(), moduleDescriptorResponsePackage.body(), tag, releaseDate);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException("Can't read file from uri: " + uriPackage);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return body;
    }

    private static String getFileContentForMaster(GHRepository repository, String fileName) throws IOException {
        String uri = BASE_URL + repository.getName() + "/master/" + fileName;
        String uriPom = BASE_URL + repository.getName() + "/master/" + POM_XML;
        String body;

        HttpRequest requestPom = HttpRequest.newBuilder()
                .uri(URI.create(uriPom))
                .header("Authorization", "token " + token)
                .build();
        HttpRequest requestPackage = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "token " + token)
                .build();

        try {
            HttpResponse<String> moduleDescriptorResponsePackageJson = client.send(requestPackage, HttpResponse.BodyHandlers.ofString());
            HttpResponse<String> moduleDescriptorResponsePom = client.send(requestPom, HttpResponse.BodyHandlers.ofString());

            String tag = getTag(repository);

            body = builderToJson(moduleDescriptorResponsePom.body(), moduleDescriptorResponsePackageJson.body(), tag, LocalDateTime.now().toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException("Can't read file from uri: " + uri);
        }
        return body;
    }

    private static String builderToJson(String pomXml, String packageJson, String tag, String releaseDate) {
        String artefactId = "n/a";
        String rmb = "n/a";
        if (isNotFound(pomXml)) {
            artefactId = getStringByXPath(pomXml, ARTIFACT_ID_X_PATH);
            rmb = getStringByXPath(pomXml, RAML_MODULE_BUILDER_VERSION_X_PATH);
        }

        ObjectMapper mapper = new ObjectMapper();

        String jsonToAddFirst = packageJson.substring(0, packageJson.length() - 3) + ",";
        // create a JSON object
        ObjectNode node = mapper.createObjectNode();
        node.put("artifactId", artefactId);
        node.put("previousReleaseVersion", tag);
        node.put("rmb", rmb);
        node.put("previousReleaseData", releaseDate);
        String json = "";
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonToAddFirst + json.substring(1);
    }

    private static boolean isNotFound(String pomXml) {
        return !Pattern.compile("404: Not Found").matcher(pomXml).find();
    }


    @SneakyThrows
    private static String getStringByXPath(String body, String regexp) {
        body = correctResponseBody(body);
        InputSource source = new InputSource(new StringReader(body));

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(source);

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        String res = xpath.evaluate(regexp, document);
        if (res.isEmpty()) {
            return "n/a";
        }
        return res;
    }

    private static String correctResponseBody(String body) {
        return body.trim().replaceFirst("^([\\W]+)<", "<");
    }

    private List<Module> mapDescriptorsToModules(Map<String, String> descriptors) {
        Map<String, String> uiDescriptors = new HashMap<>();
        Map<String, String> beDescriptors = new HashMap<>();

        descriptors.keySet().forEach(key -> {
            if (key.startsWith("ui-")) {
                uiDescriptors.put(key, descriptors.get(key));
            } else {
                beDescriptors.put(key, descriptors.get(key));
            }
        });

        var modules = beDescriptors.values()
                .stream()
                .filter(el -> !Pattern.compile("404").matcher(el).find())
                .map(value -> {
                    Module module = null;
                    try {
                        module = new ObjectMapper().readValue(value, Module.class);
                    } catch (JsonProcessingException e) {
                        logger.error("Cannot map BE Descriptor {} to a Module", value);
                        logger.error(e);
                    }
                    return module;
                }).filter(Objects::nonNull).distinct().collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Module.class, new UIModuleDeserializer());
        mapper.registerModule(module);

        var uiModules = uiDescriptors.values()
                .stream()
                .filter(el -> !Pattern.compile("404").matcher(el).find())
                .map(value -> {
                    Module uiModule = null;
                    try {
                        uiModule = mapper.readValue(value, Module.class);
                    } catch (JsonProcessingException e) {
                        logger.error("Cannot map UI Descriptor {} to a Module", value);
                        logger.error(e);
                    }
                    return uiModule;
                }).filter(Objects::nonNull).distinct().collect(Collectors.toList());

        modules.addAll(uiModules);

        return modules;
    }

    private static String getTag(GHRepository repository) throws IOException {
        String tag = "master";
        var tags = repository.listTags().toList();
        if (!tags.isEmpty()) {
            tag = tags.get(0).getName();
        }
        return tag;
    }

}
