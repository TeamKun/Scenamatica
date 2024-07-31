package org.kunlab.scenamatica.bookkeeper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j(topic = "Packer")
public class ArtifactPacker
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Path[] excludePaths;
    private final boolean doOptimiseJson;
    private final int compressionLevel;

    public ArtifactPacker(Path[] excludePaths, boolean doOptimiseJson, int compressionLevel)
    {
        this.excludePaths = excludePaths;
        this.doOptimiseJson = doOptimiseJson;
        this.compressionLevel = compressionLevel;
    }

    private void optimiseJson(Path file)
    {
        // 読み取って => 書き込む と, minify されるのでそうする。
        try // ここで fos を開くと, ファイルが上書きされる。
        {
            long sizeBefore = Files.size(file);
            JsonNode node = MAPPER.readTree(file.toFile());
            try (FileOutputStream fos = new FileOutputStream(file.toFile()))
            {
                MAPPER.writeValue(fos, node);
            }
            long sizeAfter = Files.size(file);

            log.debug("Optimised JSON file: {} ({} -> {} bytes, {}%)", file, sizeBefore, sizeAfter, Math.round((double) sizeAfter / sizeBefore * 100));
        }
        catch (IOException e)
        {
            log.error("Failed to optimise JSON file: {}", file, e);
            throw new UncheckedIOException(e);
        }
    }

    public Path pack(Path directory, Path dist)
    {
        try (FileOutputStream fos = new FileOutputStream(dist.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos))
        {
            zos.setLevel(this.compressionLevel);
            zos.setMethod(ZipOutputStream.DEFLATED);

            Files.walk(directory).forEach(path -> {
                try
                {
                    if (path.getFileName().equals(dist.getFileName())
                            || Arrays.stream(this.excludePaths).anyMatch(path::startsWith))
                        return;

                    String zipEntryName = directory.relativize(path).toString().replace("\\", "/");
                    if (Files.isDirectory(path))
                    {
                        if (zipEntryName.isEmpty())
                            log.debug("Skipping root directory: {}", path);
                        else
                        {
                            log.debug("Packing directory: {}", path);
                            zos.putNextEntry(new ZipEntry(zipEntryName + "/"));
                            zos.closeEntry();
                        }
                    }
                    else
                    {
                        log.debug("Packing file: {}", path);
                        zos.putNextEntry(new ZipEntry(zipEntryName));
                        if (this.doOptimiseJson && zipEntryName.endsWith(".json"))
                            optimiseJson(path);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    }
                }
                catch (IOException e)
                {
                    log.error("Failed to pack file: {}", path, e);
                    throw new UncheckedIOException(e);
                }
            });
        }
        catch (IOException e)
        {
            log.error("Failed to pack directory: {}", directory, e);
            throw new UncheckedIOException(e);
        }

        return dist;
    }
}
