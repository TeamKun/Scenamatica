package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.EntityUtils;
import org.kunlab.scenamatica.commons.utils.BeanUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityBean;

import java.util.Map;
import java.util.Objects;

@Getter
@EqualsAndHashCode
public class EntityArgumentHolder
{
    public static final EntityArgumentHolder EMPTY = new EntityArgumentHolder(null);

    @Nullable
    protected final String targetSpecifier;
    @Nullable
    protected final EntityBean targetBean;

    public EntityArgumentHolder(@Nullable Object mayTarget)
    {
        if (mayTarget instanceof EntityBean)
        {
            this.targetSpecifier = null;
            this.targetBean = (EntityBean) mayTarget;
        }
        else
        {
            this.targetSpecifier = (String) mayTarget;
            this.targetBean = null;
        }
    }

    public static EntityArgumentHolder tryDeserialize(Object obj, BeanSerializer serializer)
    {
        if (obj == null)
            return EMPTY;

        if (obj instanceof String || obj instanceof EntityBean)
            return new EntityArgumentHolder(obj);

        if (obj instanceof Map)
        {
            // noinspection unchecked
            Map<String, Object> map = (Map<String, Object>) obj;

            return new EntityArgumentHolder(serializer.deserializeEntity(map));
        }

        throw new IllegalArgumentException("Cannot deserialize EntityArgumentHolder from " + obj);
    }

    public boolean isSelectable()
    {
        return this.targetSpecifier != null;
    }

    public Entity selectTarget()
    {
        if (this.targetSpecifier == null)
            throw new IllegalStateException("Cannot select target from targetBean");

        return EntityUtils.getPlayerOrEntityOrThrow(this.targetSpecifier);
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
            return this.targetBean;
    }

    public boolean hasTarget()
    {
        return this.targetSpecifier != null || this.targetBean != null;
    }

    public String getArgumentString()
    {
        if (this.targetSpecifier != null)
            return "target=" + this.targetSpecifier;
        else
            return "target=" + this.targetBean;

    }

    public boolean checkMatchedEntity(Entity entity)
    {
        if (!this.hasTarget())
            return true;

        if (this.isSelectable())
            return this.checkMatchedEntity(this.getTargetString(), entity);
        else /* if (this.getTargetBean() != null) */
        {
            assert this.getTargetBean() != null;
            return BeanUtils.isSame(this.getTargetBean(), entity, /* strict */ false);
        }
    }

    protected boolean checkMatchedEntity(String specifier, @NotNull Entity actualEntity)
    {
        return EntityUtils.selectEntities(specifier)
                .stream()
                .anyMatch(entity -> Objects.equals(entity.getUniqueId(), actualEntity.getUniqueId()));
    }
}
