package org.kunlab.scenamatica.bookkeeper.compiler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.AnnotationValues;
import org.kunlab.scenamatica.bookkeeper.ScenamaticaClassLoader;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.compiler.models.ICompiled;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.IReference;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CategoryManager
{
    private static final String DESC_CATEGORY = Descriptors.getDescriptor(Category.class);
    private static final ObjectWriter MAPPER = new ObjectMapper()
            .writerWithDefaultPrettyPrinter();

    private final ScenamaticaClassLoader cl;
    private final Path categoryPath;
    private final Map<String, CategoryEntry> categories;

    private String currentPhase;

    public CategoryManager(ScenamaticaClassLoader cl, Path categoryPath)
    {
        this.cl = cl;
        this.categoryPath = categoryPath;
        this.categories = new LinkedHashMap<>();
    }

    public void changePhase(String phase)
    {
        if (!this.categories.isEmpty())
        {
            this.flush();
            this.categories.clear();
        }

        this.currentPhase = phase;
    }

    public @Nullable CategoryEntry recogniseCategory(@NotNull ClassNode cn)
    {
        ClassNode current = cn;
        clazz:
        while (true)
        {
            List<AnnotationNode> annos = current.invisibleAnnotations;
            if (annos != null)
                for (AnnotationNode anno : annos)
                {
                    if (!anno.desc.equals(DESC_CATEGORY))
                        continue;

                    AnnotationValues annoValue = AnnotationValues.of(anno);
                    Type inherit;
                    if ((inherit = annoValue.get("inherit", Type.class)) != null)
                    {
                        ClassNode parent = this.cl.getClassByName(inherit.getInternalName());
                        if (parent == null)
                            throw new IllegalStateException("Failed to find the parent class " + inherit
                                    + " for the category " + cn.name);

                        current = parent;
                        continue clazz;
                    }

                    String id = annoValue.getAsString("id");
                    String name = annoValue.getAsString("name");
                    String desc = annoValue.getAsString("description");
                    if (id == null || id.isEmpty())
                        throw new IllegalStateException("Found ambiguous category ID for " + cn.name);

                    CategoryEntry entry = new CategoryEntry(id, name, desc, this.currentPhase);
                    this.categories.put(id, entry);
                    return entry;
                }

            // 見つからなかったので親クラスを探す
            String superName = current.superName;
            if (superName == null || superName.startsWith("java/lang/Object") || superName.startsWith("java/lang/Enum"))
            {
                // 親クラスが見つからなかった => インタフェースを探す。
                for (String iface : current.interfaces)
                {
                    ClassNode ifaceNode = this.cl.getClassByName(iface);
                    if (ifaceNode == null)
                        continue;

                    try
                    {
                        return this.recogniseCategory(ifaceNode);
                    }
                    catch (IllegalStateException ignored)
                    {
                    }
                }

                return null;
            }

            current = this.cl.getClassByName(superName);
            if (current == null)
                throw new IllegalStateException("Failed to find the parent class " + superName
                        + " for the category " + cn.name);
        }
    }

    public CategoryEntry registerCategoryManually(String id, String name, String desc)
    {
        if (this.categories.containsKey(id))
            throw new IllegalStateException("The category " + id + " is already registered");

        CategoryEntry entry;
        this.categories.put(id, entry = new CategoryEntry(id, name, desc, this.currentPhase));
        return entry;
    }

    public void flush()
    {
        try
        {
            Path phaseDir = this.categoryPath.resolve(this.currentPhase);
            if (!phaseDir.toFile().exists())
                phaseDir.toFile().mkdirs();

            for (Map.Entry<String, CategoryEntry> entry : this.categories.entrySet())
            {
                String id = entry.getKey();
                CategoryEntry category = entry.getValue();

                // Write the category to the file
                Path categoryFile = phaseDir.resolve(id + ".json");
                MAPPER.writeValue(categoryFile.toFile(), category.serialize());
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to flush categories", e);
        }
    }

    public CategoryEntry getCategoryByID(String server)
    {
        return this.categories.get(server);
    }

    @Value
    public static class CategoryEntry implements IReference<ICompiled>
    {
        private static final String KEY_ID = "id";
        private static final String KEY_NAME = "name";
        private static final String KEY_DESC = "description";
        private static final String KEY_PHASE = "phase";

        String id;
        String name;
        String description;

        String phase;

        public Map<String, Object> serialize()
        {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(KEY_ID, this.id);
            map.put(KEY_NAME, this.name);
            map.put(KEY_DESC, this.description);
            map.put(KEY_PHASE, this.phase);
            map.put(ICompiler.KEY_REFERENCE, this.getReference());
            return map;
        }

        @Override
        @JsonIgnore
        public String getReference()
        {
            return "$ref:category:" + this.phase + ":" + this.id;
        }

        @JsonIgnore
        public Path getChildrenPath()
        {
            return Paths.get(this.id);
        }

        @Override
        @JsonIgnore
        public ICompiled getResolved()
        {
            return null;
        }
    }
}
