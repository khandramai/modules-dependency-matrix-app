package org.folio.modulesdependencymatrixapp.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.folio.modulesdependencymatrixapp.Dependency;
import org.folio.modulesdependencymatrixapp.Module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UIModuleDeserializer extends StdDeserializer<Module> {

    private static final String NAME_FIELD = "name";
    private static final String VERSION_FIELD = "version";
    private static final String DEPENDENCIES_FIELD = "okapiInterfaces";

    public UIModuleDeserializer() {
        this(null);
    }

    protected UIModuleDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Module deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String name = node.get(NAME_FIELD).asText();
        String version = node.get(VERSION_FIELD).asText();
        List<Dependency> dependencies = new ArrayList<>();

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
        module.setName(name);
        module.setId(version);
        module.setRequires(dependencies);

        return module;
    }
}
