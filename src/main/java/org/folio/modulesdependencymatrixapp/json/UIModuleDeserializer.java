package org.folio.modulesdependencymatrixapp.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.folio.modulesdependencymatrixapp.entity.Dependency;
import org.folio.modulesdependencymatrixapp.entity.Module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UIModuleDeserializer extends StdDeserializer<Module> {

    private static final String NAME_FIELD = "name";
    private static final String VERSION_FIELD = "version";
    private static final String DEPENDENCIES_FIELD = "okapiInterfaces";
    private static final String PREVIOUS_RELEASE_FIELD = "previousReleaseVersion";
    private static final String PREVIOUS_RELEASE_DATE_FIELD = "previousReleaseData";

    public UIModuleDeserializer() {
        this(null);
    }

    protected UIModuleDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Module deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String name = node.get(NAME_FIELD).asText();
        String version = node.get(VERSION_FIELD).asText();
        List<Dependency> dependencies = new ArrayList<>();
        String previousReleaseVersion = node.get(PREVIOUS_RELEASE_FIELD).asText();
        String previousReleaseDate = node.get(PREVIOUS_RELEASE_DATE_FIELD).asText();

        Iterator<Map.Entry<String, JsonNode>> okapiInterfaces = null;
        var dependenciesNode = node.findValue(DEPENDENCIES_FIELD);
        if (dependenciesNode != null) {
            okapiInterfaces = dependenciesNode.fields();
        }

        if (okapiInterfaces != null) {
            okapiInterfaces.forEachRemaining(element -> {
                Dependency dependency = new Dependency();
                dependency.setId(element.getKey());
                dependency.setVersion(element.getValue().asText());
                dependencies.add(dependency);
            });
        }

        Module module = new Module();
        module.setName(name.replace("@folio/", "UI "));
        module.setId(version);
        module.setRequires(dependencies);
        module.setPreviousReleaseVersion(previousReleaseVersion);
        module.setPreviousReleaseData(previousReleaseDate);

        return module;
    }
}
