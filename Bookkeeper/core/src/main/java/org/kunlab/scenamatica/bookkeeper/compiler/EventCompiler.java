package org.kunlab.scenamatica.bookkeeper.compiler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledEvent;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.EventReference;
import org.kunlab.scenamatica.bookkeeper.definitions.IDefinition;
import org.objectweb.asm.tree.ClassNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Slf4j(topic = "Compiler/Events")
public class EventCompiler implements ICompiler<EventCompiler.DummyEventDefinition, CompiledEvent, EventReference>
{
    private static final Path eventsDir = Paths.get("events");
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Locale FALLBACK_LANG = Locale.JAPANESE;

    @Getter
    private final Locale language;
    private final Map<String, EventReference> references;
    @Getter
    private final Path outDir;
    private final Path tempDir;
    private final URL eventsURL;
    private final URL licenseURL;

    @Getter
    private Path eventsFile;
    private List<JsonEventNode> events;

    public EventCompiler(Path outDir, Path tempDir, Locale language, String eventsURL, String licenseURL)
    {
        this.language = language;
        this.references = new HashMap<>();
        this.outDir = outDir.resolve(eventsDir);
        this.tempDir = tempDir.resolve(eventsDir);

        this.eventsURL = toURL(eventsURL);
        this.licenseURL = toURL(licenseURL);

        createDir(this.outDir);
        createDir(this.tempDir);
    }

    @Override
    public void init()
    {
        log.info("Downloading events file...");
        Path file = this.eventsFile = downloadFile(this.eventsURL, this.tempDir.resolve("events.json"));
        downloadFile(this.licenseURL, this.outDir.resolve("LICENSE"));

        log.info("Events file downloaded to {}", file);

        parseJson(file);
    }

    @Override
    public Class<DummyEventDefinition> getDefinitionType()
    {
        return DummyEventDefinition.class;
    }

    @Override
    public void flush(Path directory)
    {
        for (EventReference reference : this.references.values())
        {
            Path file = directory.resolve(Paths.get("events", reference.getID() + ".json"));
            try
            {
                MAPPER.writeValue(file.toFile(), reference.getResolved().serialize());
            }
            catch (IOException e)
            {
                throw new IllegalStateException("Failed to write event reference to file: " + file, e);
            }
        }
    }

    private void parseJson(Path eventsFile)
    {
        log.info("Parsing events file...");
        try (BufferedReader reader = Files.newBufferedReader(eventsFile))
        {
            ObjectNode root = (ObjectNode) MAPPER.readTree(reader);
            if (!root.has("events"))
                throw new IllegalStateException("Events file does not contain 'events' array.");

            ArrayNode events = (ArrayNode) root.get("events");
            this.events = MAPPER.readerForListOf(JsonEventNode.class).readValue(events);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to parse events file: " + eventsFile, e);
        }

    }

    @Override
    public EventReference compile(DummyEventDefinition definition)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventReference resolve(String eventName)
    {
        if (this.references.containsKey(eventName))
            return this.references.get(eventName);

        JsonEventNode node = null;
        for (JsonEventNode event : this.events)
        {
            if (event.name.equalsIgnoreCase(eventName))
            {
                node = event;
                break;
            }
        }

        if (node == null)
            throw new IllegalArgumentException("Unresolved event: " + eventName);

        String description;
        if (node.description.containsKey(this.language.getLanguage()))
            description = node.description.get(this.language.getLanguage());
        else
            description = node.description.getOrDefault(
                    FALLBACK_LANG.getLanguage(),
                    "No description available."
            );

        EventReference reference = new EventReference(
                node.name,
                new CompiledEvent(
                        node.name,
                        node.name,
                        node.javadoc,
                        node.link,
                        CompiledEvent.Source.fromId(node.source),
                        description
                )
        );

        this.references.put(node.name, reference);
        return reference;
    }

    @Override
    public List<EventReference> getResolvedReferences()
    {
        return Collections.unmodifiableList(new ArrayList<>(this.references.values()));
    }

    private static void createDir(Path outDir)
    {
        try
        {
            Files.createDirectories(outDir);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to create events directory: " + outDir, e);
        }
    }

    private static URL toURL(String url)
    {
        try
        {
            return new URL(url);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    private static Path downloadFile(URL url, Path dist)
    {
        log.info("Downloading file from {} to {}", url, dist);
        try
        {
            assert Objects.equals(url.getProtocol(), "http") || Objects.equals(url.getProtocol(), "https");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "Mozilla/8.10 (X931; Peyantu; Linux x86_64) PeyangWebKit/114.514(KUN, like Gacho) Scenamatica/Bookkeeper");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                throw new IllegalStateException("Failed to download file from " + url + ". Response code: " + conn.getResponseCode());

            try (InputStream in = conn.getInputStream();
                 OutputStream out = Files.newOutputStream(dist))
            {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1)
                    out.write(buffer, 0, len);

                return dist;
            }


        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to download file from " + url, e);
        }
    }

    /* non-public */ static class DummyEventDefinition implements IDefinition
    {

        @Override
        public ClassNode getAnnotatedClass()
        {
            return null;
        }

        @Override
        public Class<?> getAnnotationType()
        {
            return null;
        }

        @Override
        public boolean isDependsOn(@NotNull IDefinition classNode)
        {
            return false;
        }
    }

    @Value
    @AllArgsConstructor
    private static class JsonEventNode
    {
        Map<String, String> description;
        @JsonProperty("abstract")
        boolean isAbstract;
        String href;
        String javadoc;
        String link;
        String name;
        String source;

        public JsonEventNode()
        {
            this(null, false, null, null, null, null, null);
        }
    }
}
