package org.kunlab.scenamatica.bookkeeper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.kunlab.scenamatica.bookkeeper.compiler.SerializingContext;
import org.kunlab.scenamatica.bookkeeper.compiler.TypeCompiler;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledType;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j(topic = "JSON schema generator")
public class JsonSchemaGenerator
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TEMPLATE_NAME = "json-schema.json";
    private static final String APEX_TYPE_CLASS_NAME = "org/kunlab/scenamatica/interfaces/scenariofile/ScenarioFileStructure";
    private static final int MAX_DEPTH = 5;
    private final BookkeeperCore core;
    private final Path dist;
    private final TypeCompiler typeCompiler;

    public JsonSchemaGenerator(BookkeeperCore core, Path dist, TypeCompiler typeCompiler)
    {
        this.core = core;
        this.dist = dist;
        this.typeCompiler = typeCompiler;
    }

    public void generate()
    {
        CompiledType apexType = lookupApexType(this.typeCompiler);
        Map<String, Object> definitions = new HashMap<>();
        Map<String, Object> serialized = apexType.serialize(new SerializingContext(
                definitions,
                this.core,
                true
        ));
        serialized.put("definitions", definitions);

        mergeAndSaveSchema(this.dist, serialized);
    }

    private static void mergeAndSaveSchema(Path dist, Map<String, Object> serialized)
    {
        JsonNode serializedSchema = createJSONNode(serialized);

        try (InputStream templateStream = JsonSchemaGenerator.class.getClassLoader().getResourceAsStream(TEMPLATE_NAME))
        {
            log.info("Loading the JSON schema template");
            JsonNode template = MAPPER.readTree(templateStream);

            log.info("Merging the JSON schema template with the serialized apex type");
            JsonNode merged = MAPPER.readerForUpdating(template).readValue(serializedSchema);

            log.info("Writing the JSON schema to the disk");
            MAPPER.writeValue(dist.toFile(), merged);
        }
        catch (IOException e)
        {
            log.error("Failed to generate JSON schema", e);
        }
    }

    private static JsonNode createJSONNode(Map<String, Object> serialized)
    {
        try
        {
            log.info("Creating a JSON node from the serialized apex type");
            return MAPPER.valueToTree(serialized);
        }
        catch (Exception e)
        {
            log.error("Failed to create a JSON node from the serialized apex type", e);
            return null;
        }
    }

    private static CompiledType lookupApexType(TypeCompiler typeCompiler)
    {
        log.info("Looking up the apex type {}", APEX_TYPE_CLASS_NAME);
        for (TypeReference compiledTypeRef : typeCompiler.getResolvedReferences())
        {
            CompiledType compiledType = compiledTypeRef.getResolved();
            if (compiledType.getClassName().equals(APEX_TYPE_CLASS_NAME))
            {
                log.info("Found the apex type {}", compiledType.getClassName());
                return compiledType;
            }
        }

        throw new IllegalStateException("Apex type " + APEX_TYPE_CLASS_NAME + " not found. If you are trying to" +
                " generate JSON schema which jar is not the official build of Scenamatica, turn off the JSON schema generation.");
    }
}
