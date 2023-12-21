package org.kunlab.scenamatica.scenariofile.specifiers;

import lombok.EqualsAndHashCode;
import lombok.Getter;
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

@Getter
@EqualsAndHashCode
public class EntitySpecifierImpl<E extends Entity> implements EntitySpecifier<E>
{
    public static final EntitySpecifier<Entity> EMPTY = new EntitySpecifierImpl<>(null);

    protected final Selector selector;
    protected final EntityStructure targetStructure;

    public EntitySpecifierImpl(@Nullable Object mayTarget)
    {
        if (mayTarget == null)
        {
            this.selector = null;
            this.targetStructure = null;
        }
        else if (mayTarget instanceof EntityStructure)
        {
            this.selector = null;
            this.targetStructure = (EntityStructure) mayTarget;
        }
        else if (mayTarget instanceof String)
        {
            this.selector = Selector.compile((String) mayTarget);
            this.targetStructure = null;
        }
        else
            throw new IllegalArgumentException("Unknown target type: " + mayTarget.getClass() + ", try deserialize instead");
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
        if (this.selector != null)
            return this.selector.selectOne(basis).orElse(null);

        if (this.targetStructure == null)
            throw new IllegalStateException("Cannot select target from this specifier: " + this);

        return EntityUtils.getEntity(this.targetStructure, context, null);
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

    @Override
    public boolean canProvideTarget()
    {
        return this.isSelectable() || this.hasStructure();
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
        if (!this.canProvideTarget())
            return true;

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
