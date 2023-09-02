package org.kunlab.scenamatica.results;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.ExceptionHandler;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.w3c.dom.Document;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScenarioResultWriter
{
    @NotNull
    private final ExceptionHandler exceptionHandler;
    @NotNull
    private final Path directory;
    @NotNull
    private final String fileNamePattern;
    @NotNull
    @Getter
    private final LogCapture logCapture;

    public ScenarioResultWriter(@NotNull Path directory, @NotNull ExceptionHandler exceptionHandler, @NotNull String fileNamePattern)
    {
        this.directory = directory;
        this.exceptionHandler = exceptionHandler;
        this.fileNamePattern = fileNamePattern;

        this.logCapture = new LogCapture();
    }

    public void init(@NotNull Plugin scenamatica)
    {
        this.logCapture.init(scenamatica);
    }

    public Path write(@NotNull ScenarioSession session)
    {
        return this.write(session, null);
    }

    @SneakyThrows(IOException.class)  // Files.createDirectories
    public Path write(@NotNull ScenarioSession session, @Nullable Path file)
    {
        if (session.isRunning())
            throw new IllegalStateException("Scenario session is running.");

        Path dest = this.composeDistPath(file);

        ScenarioResultDocumentBuilder builder = new ScenarioResultDocumentBuilder(this);

        Document document = builder.build(session);
        if (!Files.exists(dest.getParent()))
            Files.createDirectories(dest.getParent());
        try (FileOutputStream stream = new FileOutputStream(dest.toFile()))
        {

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(document), new StreamResult(stream));

            return dest;
        }
        catch (TransformerException | IOException e)
        {
            this.exceptionHandler.report(e);
            return null;
        }

    }

    private Path composeDistPath(@Nullable Path file)
    {
        if (file != null)
            return this.directory.resolve(file);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
        LocalDateTime dateTime = LocalDateTime.now();
        String dateTimeString = dateTime.format(formatter);

        return this.directory.resolve(this.fileNamePattern
                .replace("{dateTime}", dateTimeString)
        );
    }
}
