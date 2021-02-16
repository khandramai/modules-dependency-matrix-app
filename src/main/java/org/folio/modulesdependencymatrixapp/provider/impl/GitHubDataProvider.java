package org.folio.modulesdependencymatrixapp.provider.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.modulesdependencymatrixapp.Module;
import org.folio.modulesdependencymatrixapp.json.UIModuleDeserializer;
import org.folio.modulesdependencymatrixapp.provider.DataProvider;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GitHubDataProvider implements DataProvider {

    private static final Logger logger = LogManager.getLogger("GitHubDataProvider");
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "https://raw.githubusercontent.com/folio-org/";
    private static final String TOKEN = "a776953df57448bcec1eb5b36834b16f495f519a";
    private final GitHub gitHub;

    public GitHubDataProvider() throws IOException {
        gitHub = new GitHubBuilder().withOAuthToken(TOKEN).build();
    }

    @Override
    public List<Module> getDataFromMaster() {
        Map<String, String> descriptors = new HashMap<>();

        try {
            var organization = gitHub.getOrganization("folio-org");
            var repositories = organization.getRepositories();

            repositories
                    .keySet()
                    .stream()
                    .filter(repo -> repo.startsWith("edge-") || repo.startsWith("mod-") || repo.startsWith("ui-"))
                    .forEach(repo -> {
                        try {
                            GHRepository repository = gitHub.getRepository("folio-org/" + repo);
                            if (repo.startsWith("ui-")) {
                                descriptors.put(repo, getFileContentForMaster(repository, "package.json"));
                            } else {
                                descriptors.put(repo, getFileContentForMaster(repository, "descriptors/ModuleDescriptor-template.json"));
                            }
                        } catch (IOException e) {
                            logger.error(String.format("%s doesn't have package.json or ModuleDescriptor-template.json", repo));

                        }
                    });
        } catch (IOException e) {
            logger.error(e);
        }

        return mapDescriptorsToModules(descriptors);
    }

    @Override
    public List<Module> getDataFromTag(int number) {
        Map<String, String> descriptors = new HashMap<>();

        try {
            var organization = gitHub.getOrganization("folio-org");
            var repositories = organization.getRepositories();

            repositories
                    .keySet()
                    .stream()
                    .filter(repo -> repo.startsWith("edge-") || repo.startsWith("mod-") || repo.startsWith("ui-"))
                    .forEach(repo -> {
                        try {
                            GHRepository repository = gitHub.getRepository("folio-org/" + repo);
                            var tags = repository.listTags().toList();

                            if (repo.startsWith("ui-")) {
                                descriptors.put(repo, getFileContentForTag(number, repository, "package.json"));
                            } else {
                                descriptors.put(repo, getFileContentForTag(number, repository, "descriptors/ModuleDescriptor-template.json"));
                            }
                        } catch (IOException e) {
                            logger.error(String.format("%s doesn't have package.json or ModuleDescriptor-template.json", repo));

                        }
                    });
        } catch (IOException e) {
            logger.error(e);
        }

        return mapDescriptorsToModules(descriptors);
    }

    private String getFileContent(String name, GHRepository repository, String repoName) throws IOException {
        var file = repository.getFileContent(name);
        logger.info(String.format("Loaded %s from %s", file.getName(), repoName));
        var stream = file.read();
        var streamReader = new InputStreamReader(stream);
        var reader = new BufferedReader(streamReader);
        return reader.lines().parallel().collect(Collectors.joining("\n"));
    }

    private String getFileContentForTag(int tagNumber, GHRepository repository, String fileName) throws IOException {

        var tags = repository.listTags().toList();
        String tag = "master";

        if (!tags.isEmpty() && tagNumber < tags.size()) {
            tag = tags.get(tagNumber).getName();
        }

        String uri = BASE_URL + repository.getName() + "/" + tag + "/" + fileName;
        String body = null;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "token " + TOKEN)
                .build();

        try {
            HttpResponse<String> moduleDescriptorResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            body = moduleDescriptorResponse.body();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException("Can't read file from uri: " + uri);
        }

        return body;
    }

    private String getFileContentForMaster(GHRepository repository, String fileName) throws IOException {
        String uri = BASE_URL + repository.getName() + "/master/" + fileName;
        String body = null;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "token " + TOKEN)
                .build();

        try {
            HttpResponse<String> moduleDescriptorResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            body = moduleDescriptorResponse.body();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException("Can't read file from uri: " + uri);
        }

        return body;
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
                .map(value -> {
                    Module module = null;
                    try {
                        module = new ObjectMapper().readValue(value, Module.class);
                    } catch (JsonProcessingException e) {
                        logger.error("Cannot map BE Descriptor {} to a Module", value);
                        logger.error(e);
                    }
                    return module;
                }).filter(Objects::nonNull).collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Module.class, new UIModuleDeserializer());
        mapper.registerModule(module);

        var uiModules = uiDescriptors.values()
                .stream()
                .map(value -> {
                    Module uiModule = null;
                    try {
                        uiModule = mapper.readValue(value, Module.class);
                    } catch (JsonProcessingException e) {
                        logger.error("Cannot map UI Descriptor {} to a Module", value);
                        logger.error(e);
                    }
                    return uiModule;
                }).filter(Objects::nonNull).collect(Collectors.toList());

        modules.addAll(uiModules);

        return modules;
    }
}
