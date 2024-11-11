package org.kunlab.scenamatica.structures.specifiers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.EntityUtils;
import org.kunlab.scenamatica.commons.utils.ThreadingUtil;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.selector.Selector;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@EqualsAndHashCode
public class EntitySpecifierImpl<E extends Entity> implements EntitySpecifier<E>
{
    public static final EntitySpecifier<Entity> EMPTY = new EntitySpecifierImpl<>();

    protected final UUID mayUUID;
    protected final Selector selector;
    protected final EntityStructure targetStructure;

    protected EntitySpecifierImpl(@NotNull EntityStructure targetStructure)
    {
        this(null, null, targetStructure);
    }

    protected EntitySpecifierImpl(@NotNull Selector selector)
    {
        this(null, selector, null);
    }

    protected EntitySpecifierImpl(@NotNull UUID mayUUID)
    {
        this(mayUUID, null, null);
    }

    protected EntitySpecifierImpl()
    {
        this(null, null, null);
    }

    protected EntitySpecifierImpl(UUID mayUUID, Selector selector, EntityStructure targetStructure)
    {
        this.mayUUID = mayUUID;
        this.selector = selector;
        this.targetStructure = targetStructure;
    }

    @SneakyThrows
    public static <E extends Entity> EntitySpecifier<E> tryDeserialize(
            Object obj,
            StructureSerializer serializer,
            Class<? extends EntityStructure> structureClass
    )
    {
        if (obj == null)
            // noinspection unchecked
            return (EntitySpecifierImpl<E>) EMPTY;

        if (obj instanceof Selector)
            return new EntitySpecifierImpl<>((Selector) obj);
        else if (obj instanceof EntityStructure)
            return new EntitySpecifierImpl<>((EntityStructure) obj);
        else if (obj instanceof UUID)
            return new EntitySpecifierImpl<>((UUID) obj);

        if (!(obj instanceof StructuredYamlNode))
            throw new IllegalArgumentException("Cannot deserialize EntityArgumentHolder from " + obj);

        StructuredYamlNode node = (StructuredYamlNode) obj;
        if (node.isType(YAMLNodeType.MAPPING))
            return new EntitySpecifierImpl<>(serializer.deserialize(node, structureClass));
        else if (node.isType(YAMLNodeType.STRING))
        {
            String str = node.asString();
            UUID mayUUID = tryConvertToUUID(str);

            if (mayUUID == null)
                return new EntitySpecifierImpl<>(Selector.compile(str));
            else
                return new EntitySpecifierImpl<>(mayUUID);
        }
        else if (node.isNullNode())
            // noinspection unchecked
            return (EntitySpecifierImpl<E>) EMPTY;

        throw new IllegalArgumentException("Cannot deserialize EntityArgumentHolder from " + obj);
    }

    public static <E extends Entity> EntitySpecifier<E> of(@NotNull E entity)
    {
        return new EntitySpecifierImpl<>(entity.getUniqueId());
    }

    public static <E extends Entity> EntitySpecifierImpl<E> of(@NotNull UUID entityUUID)
    {
        return new EntitySpecifierImpl<>(entityUUID);
    }

    protected static UUID tryConvertToUUID(String mayUUID)
    {
        try
        {
            return UUID.fromString(mayUUID);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    protected static boolean isUUIDLike(String mayUUID)
    {
        return tryConvertToUUID(mayUUID) != null;
    }

    @Override
    public boolean isSelectable()
    {
        return this.selector != null;
    }

    @Override
    public Optional<E> selectTarget(@Nullable Context context)
    {
        return this.selectTarget(null, context);
    }

    @Override
    public Optional<E> selectTarget(@Nullable Player basis, @Nullable Context context)
    {
        if (!this.canProvideTarget())
            return Optional.empty();

        // noinspection unchecked
        return (Optional<E>) Optional.ofNullable(this.selectTargetRaw(basis, context));
    }

    @Override
    public @NotNull List<? extends Entity> selectTargets(@Nullable Context context)
    {
        return this.selectTargets(null, context);
    }

    @Override
    public @NotNull List<? extends Entity> selectTargets(@Nullable Player basis, @Nullable Context context)
    {
        if (this.selector != null)
            return this.selector.select(basis);

        if (this.targetStructure == null)
            throw new IllegalStateException("Cannot select target from this specifier: " + this);

        return EntityUtils.getEntities(this.targetStructure, context, null);
    }

    protected Entity selectTargetRaw(@Nullable Player basis, @Nullable Context context)
    {
        if (this.mayUUID != null)
            return this.getEntityByUUIDSynced(context, this.mayUUID);
        else if (this.selector != null)
            return this.getEntityBySelectorSynced(basis, context);
        else if (this.targetStructure != null)
            return this.getEntityWideSynced(context);
        else
            return null;
    }

    private Entity getEntityBySelectorSynced(@Nullable Player basis, @Nullable Context context)
    {
        Optional<Entity> entityInSelector = this.selector.selectOne(basis);  // sync は中で行っている
        if (entityInSelector.isPresent())
            return entityInSelector.get();

        // 存在しない場合は, context の entities を変えながらテストする
        if (context == null)
            return null;
        return context.getEntities().stream()
                .filter(e -> this.selector.test(basis, e))
                .findFirst().orElse(null);
    }

    private Entity getEntityByUUIDSynced(@Nullable Context ctxt, @NotNull UUID uuid)
    {
        // Context に Entity が含まれている場合はそれを優先
        Optional<? extends Entity> entityInContext;
        if (!(ctxt == null || ctxt.getEntities().isEmpty())
                && (entityInContext = ctxt.getEntities().stream()
                .filter(e -> e.getUniqueId().equals(uuid)).findFirst()).isPresent())
            return entityInContext.get();

        if (Bukkit.isPrimaryThread())
            return Bukkit.getEntity(uuid);
        else
            return ThreadingUtil.waitFor(() -> Bukkit.getEntity(uuid));
    }

    private Entity getEntityWideSynced(@Nullable Context ctxt)
    {
        if (Bukkit.isPrimaryThread())
            return EntityUtils.getEntity(this.targetStructure, ctxt, null);
        else
            return ThreadingUtil.waitFor(() -> EntityUtils.getEntity(this.targetStructure, ctxt, null));
    }

    @Override
    public String getSelectorString()
    {
        return this.selector.getOriginal();
    }

    @Override
    public Object getTargetRaw()
    {
        if (this.selector != null)
            return this.selector.getOriginal();
        else if (this.mayUUID != null)
            return this.mayUUID;
        else return this.targetStructure;
    }

    @Override
    public boolean hasUUID()
    {
        return this.mayUUID != null;
    }

    @Override
    public UUID getSelectingUUID()
    {
        return this.mayUUID;
    }

    @Override
    public boolean canProvideTarget()
    {
        return this.isSelectable() || this.hasStructure() || this.hasUUID();
    }

    @Override
    public String getArgumentString()
    {
        if (this.selector != null)
            return "target=" + this.selector;
        else
            return "target=" + this.targetStructure;
    }

    @Override
    public boolean checkMatchedEntity(Entity entity)
    {
        if (entity == null)
            return false;
        else if (!this.canProvideTarget())
            return false;

        if (this.mayUUID != null)
            return entity.getUniqueId().equals(this.mayUUID);
        if (this.isSelectable())
            return this.getSelector().test(null, entity);
        else if (this.targetStructure != null)
        {
            EntityStructure structure = this.getTargetStructure();
            assert structure != null;
            return structure.canApplyTo(entity) && structure.isAdequate(entity);
        }
        else
            return false;
    }

    @Override
    public boolean hasStructure()
    {
        return this.targetStructure != null;
    }
}
