package org.kunlab.scenamatica.bookkeeper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kunlab.scenamatica.bookkeeper.definitions.IDefinition;
import org.kunlab.scenamatica.bookkeeper.reader.IAnnotationReader;
import org.kunlab.scenamatica.bookkeeper.utils.Timekeeper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j(topic = "Bookkeeper/Processor/CL")
public class ScenamaticaClassLoader
{
    @Getter
    private final Path jarPath;
    private final List<? extends IAnnotationReader<? extends IDefinition>> processors;

    private final List<Path> classPaths;

    private final Map<String, ClassNode> scenamaticaClasses;
    private final Map<String, ClassNode> otherClasses;

    private ScenamaticaClassLoader(Path jarPath, List<? extends IAnnotationReader<? extends IDefinition>> processors)
    {
        this.jarPath = jarPath;
        this.processors = processors;

        this.classPaths = new ArrayList<>();
        this.scenamaticaClasses = new ConcurrentHashMap<>();
        this.otherClasses = new ConcurrentHashMap<>();
    }

    public void addClasspaths(Collection<? extends Path> classPaths)
    {
        for (Path path : classPaths)
            if (!this.classPaths.contains(path))
                this.classPaths.add(path);
    }

    private void scanFile(Path path)
    {
        Timekeeper timekeeper = new Timekeeper(log, "SCAN");
        log.info("Scanning classes from jar: {}", path);

        timekeeper.start();

        long count = 0;
        try (JarFile jarFile = new JarFile(path.toFile()))
        {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements())
            {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (!entryName.endsWith(".class")
                        || entryName.startsWith("META-INF")
                        || entryName.endsWith("package-info.class")
                        || entryName.endsWith("module-info.class"))
                    continue;

                byte[] classData = this.readClassData(jarFile, entry);
                if (classData == null)
                    continue;

                ClassReader reader = new ClassReader(classData);
                ClassNode node = new ClassNode();
                reader.accept(
                        node,
                        ClassReader.SKIP_CODE |
                                ClassReader.SKIP_DEBUG |
                                ClassReader.SKIP_FRAMES
                );

                String className = node.name;
                if (this.scenamaticaClasses.containsKey(className) || this.otherClasses.containsKey(className))
                    log.warn("Overwriting class {} from {}", className, path.getFileName());
                else
                    log.debug("Loaded class {} from {}", className, path.getFileName());
                if (this.isScenamaticaRelatedClass(node))
                    this.scenamaticaClasses.put(className, node);
                else
                    this.otherClasses.put(className, node);

                count++;
            }
        }
        catch (Exception e)
        {
            log.error("Failed to load classes from jar: " + this.jarPath, e);
            timekeeper.interrupt();
        }

        log.info("Loaded {} classes from {}", count, path.getFileName());
        timekeeper.end();
    }

    public void scanAll(int threads)
    {
        if (this.classPaths.isEmpty())
            return;

        List<Path> paths = new ArrayList<>(this.classPaths);
        paths.add(this.jarPath);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(paths.size());
        for (Path path : paths)
            executor.submit(() -> {
                Thread.currentThread().setName("CL/" + path.getFileName());
                this.scanFile(path);
                latch.countDown();
            });

        try
        {
            latch.await();
        }
        catch (InterruptedException e)
        {
            throw new IllegalStateException("Scanning interrupted", e);
        }
    }

    public List<ClassNode> getScenamaticaClasses()
    {
        return Collections.unmodifiableList(new ArrayList<>(this.scenamaticaClasses.values()));
    }

    public List<ClassNode> getOtherClasses()
    {
        return Collections.unmodifiableList(new ArrayList<>(this.otherClasses.values()));
    }

    public ClassNode getClassByName(String name)
    {
        // Replace '.' with '/' for internal name
        name = name.replace('.', '/');

        ClassNode node = this.scenamaticaClasses.get(name);
        if (node == null)
            node = this.otherClasses.get(name);
        return node;
    }

    public void notifyChange(ClassNode cn)
    {
        if (this.scenamaticaClasses.containsKey(cn.name))
            this.scenamaticaClasses.put(cn.name, cn);
        else if (this.otherClasses.containsKey(cn.name))
            this.otherClasses.put(cn.name, cn);
        else
            throw new IllegalStateException("Class not found: " + cn.name);
    }

    private boolean isScenamaticaRelatedClass(ClassNode node)
    {
        return Stream.of(node.visibleAnnotations, node.invisibleAnnotations)
                .parallel()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .anyMatch(anno -> this.processors.stream().parallel()
                        .anyMatch(p -> p.canRead(anno))
                );
    }

    private byte[] readClassData(ZipFile file, ZipEntry entry)
    {
        try (InputStream inputStream = file.getInputStream(entry);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream))
        {
            byte[] data = new byte[(int) entry.getSize()];
            bufferedInputStream.read(data);
            return data;
        }
        catch (Exception e)
        {
            log.error("Failed to read class data: " + entry.getName(), e);
            log.info("Skipping...");
            return null;
        }
    }

    public static ScenamaticaClassLoader create(Path jarPath, List<? extends IAnnotationReader<? extends IDefinition>> processors)
    {
        return new ScenamaticaClassLoader(jarPath, processors);
    }

}
