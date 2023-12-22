package org.kunlab.scenamatica.scenariofile.specifiers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.EntityUtils;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.selector.Selector;

import java.util.List;
import java.util.Map;
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

        if (obj instanceof Map)
        {
            // noinspection unchecked
            Map<String, Object> map = (Map<String, Object>) obj;

            return new EntitySpecifierImpl<>(serializer.deserialize(map, structureClass));
        }
        else if (obj instanceof String)
        {
            UUID mayUUID = tryConvertToUUID((String) obj);

            if (mayUUID == null)
                return new EntitySpecifierImpl<>(Selector.compile((String) obj));
            else
                return new EntitySpecifierImpl<>(mayUUID);
        }

        throw new IllegalArgumentException("Cannot deserialize EntityArgumentHolder from " + obj);
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
            return Bukkit.getEntity(this.mayUUID);
        else if (this.selector != null)
            return this.selector.selectOne(basis).orElse(null);
        else if (this.targetStructure != null)
            return EntityUtils.getEntity(this.targetStructure, context, null);
        else
            return null;
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
        else
            return this.targetStructure;
    }

    public boolean hasUUID()
    {
        return this.mayUUID != null;
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
        else /* if (this.getTargetStructure() != null) */
        {
            assert this.getTargetStructure() != null;
            return this.isAdequate(this.getTargetStructure(), entity);
        }
    }

    protected boolean isAdequate(EntityStructure structure, @NotNull Entity actualEntity)
    {
        return EntityUtils.checkIsAdequate(structure, actualEntity);
    }

    @Override
    public boolean hasStructure()
    {
        return this.targetStructure != null;
    }
}
