package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.action.utils.EntityUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.EntityBean;

import java.util.Objects;

@Getter
@EqualsAndHashCode
public class EntityArgumentHolder
{
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

    public String getArgumentString()
    {
        if (this.targetSpecifier != null)
            return "target=" + this.targetSpecifier;
        else
            return "target=" + this.targetBean;

    }

    public boolean checkMatchedEntity(Entity entity)
    {
        if (this.isSelectable())
            return this.checkMatchedEntity(this.getTargetString(), entity);
        else
        {
            if (this.getTargetBean() == null)
                return true;
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
