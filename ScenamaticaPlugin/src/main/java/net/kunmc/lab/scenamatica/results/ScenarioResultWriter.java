package net.kunmc.lab.scenamatica.results;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.kunmc.lab.scenamatica.Scenamatica;
import net.kunmc.lab.scenamatica.interfaces.ExceptionHandler;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioSession;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private final Plugin scenamatica;
    @NotNull
    private final ExceptionHandler exceptionHandler;
    @NotNull
    private final Path directory;
    @NotNull
    private final String fileNamePattern;

    public ScenarioResultWriter(@NotNull Scenamatica scenamatica, @NotNull Path directory, @NotNull ExceptionHandler exceptionHandler, @NotNull String fileNamePattern)
    {
        this.scenamatica = scenamatica;
        this.directory = directory;
        this.exceptionHandler = exceptionHandler;
        this.fileNamePattern = fileNamePattern;
    }

    public Path write(@NotNull ScenarioSession session)
    {
        return this.write(session, null);
    }

    public Path write(@NotNull ScenarioSession session, @Nullable Path file)
    {
        if (session.isRunning())
            throw new IllegalStateException("Scenario session is running.");

        Path dest = this.composeDistPath(file);

        Document document = ScenarioResultDocumentBuilder.build(this.scenamatica, session);
        try (FileOutputStream stream = new FileOutputStream(dest.toFile()))
        {
            if (!Files.exists(dest.getParent()))
                Files.createDirectories(dest.getParent());

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
