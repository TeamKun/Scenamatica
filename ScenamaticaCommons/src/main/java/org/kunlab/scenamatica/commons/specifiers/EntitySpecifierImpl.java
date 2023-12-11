package org.kunlab.scenamatica.commons.specifiers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.EntityUtils;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.Map;
import java.util.Objects;

@Getter
@EqualsAndHashCode
public class EntitySpecifierImpl<E extends Entity> implements EntitySpecifier<E>
{
    public static final EntitySpecifierImpl<?> EMPTY = new EntitySpecifierImpl<>(null);

    protected final String targetSpecifier;
    protected final EntityStructure targetStructure;

    public EntitySpecifierImpl(@Nullable Object mayTarget)
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

    public static <E extends Entity> EntitySpecifierImpl<E> tryDeserialize(
            Object obj,
            StructureSerializer serializer,
            Class<? extends EntityStructure> structureClass
    )
    {
        if (obj == null)
            // noinspection unchecked
            return (EntitySpecifierImpl<E>) EMPTY;

        if (obj instanceof String || obj instanceof EntityStructure)
            return new EntitySpecifierImpl<>(obj);

        if (obj instanceof Map)
        {
            // noinspection unchecked
            Map<String, Object> map = (Map<String, Object>) obj;

            return new EntitySpecifierImpl<>(serializer.deserialize(map, structureClass));
        }

        throw new IllegalArgumentException("Cannot deserialize EntityArgumentHolder from " + obj);
    }

    public static EntitySpecifier<Entity> tryDeserialize(Object obj, StructureSerializer serializer)
    {
        return tryDeserialize(obj, serializer, EntityStructure.class);
    }

    @Override
    public boolean isSelectable()
    {
        return this.targetSpecifier != null;
    }

    @Override
    public E selectTarget(@NotNull Context context)
    {
        if (this.targetSpecifier != null)
            //noinspection unchecked
            return (E) EntityUtils.getPlayerOrEntityOrThrow(this.targetSpecifier);

        if (this.targetStructure == null)
            throw new IllegalStateException("Cannot select target from this specifier: " + this);

        //noinspection unchecked
        return (E) EntityUtils.getEntity(this.targetStructure, context, null);
    }

    @Override
    public String getSelectorString()
    {
        return this.targetSpecifier;
    }

    @Override
    public Object getTargetRaw()
    {
        if (this.targetSpecifier != null)
            return this.targetSpecifier;
        else
            return this.targetStructure;
    }

    @Override
    public boolean canProvideTarget()
    {
        return this.isSelectable() || this.hasStructure();
    }

    @Override
    public String getArgumentString()
    {
        if (this.targetSpecifier != null)
            return "target=" + this.targetSpecifier;
        else
            return "target=" + this.targetStructure;

    }

    @Override
    public boolean checkMatchedEntity(Entity entity)
    {
        if (!this.canProvideTarget())
            return true;

        if (this.isSelectable())
            return this.checkMatchedEntity(this.getSelectorString(), entity);
        else /* if (this.getTargetStructure() != null) */
        {
            assert this.getTargetStructure() != null;
            return EntityUtils.tryCheckIsAdequate(this.getTargetStructure(), entity);
        }
    }

    protected boolean checkMatchedEntity(String specifier, @NotNull Entity actualEntity)
    {
        return EntityUtils.selectEntities(specifier)
                .stream()
                .anyMatch(entity -> Objects.equals(entity.getUniqueId(), actualEntity.getUniqueId()));
    }

    @Override
    public boolean hasStructure()
    {
        return this.targetStructure != null;
    }
}
