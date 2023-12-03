package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.EntityUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;

import java.util.Map;
import java.util.Objects;

@Getter
@EqualsAndHashCode
public class EntityArgumentHolder<E extends Entity>
{
    public static final EntityArgumentHolder<?> EMPTY = new EntityArgumentHolder<>(null);

    @Nullable
    protected final String targetSpecifier;
    @Nullable
    protected final EntityStructure targetStructure;

    public EntityArgumentHolder(@Nullable Object mayTarget)
    {
        if (mayTarget instanceof EntityStructure)
        {
            this.targetSpecifier = null;
            this.targetStructure = (EntityStructure) mayTarget;
        }
        else
        {
            this.targetSpecifier = (String) mayTarget;
            this.targetStructure = null;
        }
    }

    public static <E extends Entity> EntityArgumentHolder<E> tryDeserialize(
            Object obj,
            StructureSerializer serializer,
            Class<? extends EntityStructure> structureClass
    )
    {
        if (obj == null)
            // noinspection unchecked
            return (EntityArgumentHolder<E>) EMPTY;

        if (obj instanceof String || obj instanceof EntityStructure)
            return new EntityArgumentHolder<>(obj);

        if (obj instanceof Map)
        {
            // noinspection unchecked
            Map<String, Object> map = (Map<String, Object>) obj;

            return new EntityArgumentHolder<>(serializer.deserialize(map, structureClass));
        }

        throw new IllegalArgumentException("Cannot deserialize EntityArgumentHolder from " + obj);
    }

    public static EntityArgumentHolder<Entity> tryDeserialize(Object obj, StructureSerializer serializer)
    {
        return tryDeserialize(obj, serializer, EntityStructure.class);
    }

    public boolean isSelectable()
    {
        return this.targetSpecifier != null;
    }

    public E selectTarget()
    {
        if (this.targetSpecifier == null)
            throw new IllegalStateException("Cannot select target from targetStructure");

        //noinspection unchecked
        return (E) EntityUtils.getPlayerOrEntityOrThrow(this.targetSpecifier);
    }

    public String getTargetString()
    {
        return this.targetSpecifier;
    }

    public Object getTargetRaw()
    {
        if (this.targetSpecifier != null)
            return this.targetSpecifier;
        else
            return this.targetStructure;
    }

    public boolean hasTarget()
    {
        return this.targetSpecifier != null || this.targetStructure != null;
    }

    public String getArgumentString()
    {
        if (this.targetSpecifier != null)
            return "target=" + this.targetSpecifier;
        else
            return "target=" + this.targetStructure;

    }

    public boolean checkMatchedEntity(Entity entity)
    {
        if (!this.hasTarget())
            return true;

        if (this.isSelectable())
            return this.checkMatchedEntity(this.getTargetString(), entity);
        else /* if (this.getTargetStructure() != null) */
        {
            assert this.getTargetStructure() != null;
            return EntityUtils.tryCastMapped(this.getTargetStructure(), entity).canApplyTo(entity);
        }
    }

    protected boolean checkMatchedEntity(String specifier, @NotNull Entity actualEntity)
    {
        return EntityUtils.selectEntities(specifier)
                .stream()
                .anyMatch(entity -> Objects.equals(entity.getUniqueId(), actualEntity.getUniqueId()));
    }
}
