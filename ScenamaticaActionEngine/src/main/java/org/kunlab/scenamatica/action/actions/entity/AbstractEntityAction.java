package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.utils.EntityUtils;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractEntityAction<A extends AbstractEntityActionArgument> extends AbstractAction<A>
{
    public static List<? extends AbstractAction<?>> getActions()
    {
        List<AbstractAction<?>> actions = new ArrayList<>();

        actions.add(new EntityAction());
        actions.add(new EntityDamageAction<>());
        actions.add(new EntityDamageByEntityAction());
        actions.add(new EntityDeathAction());
        actions.add(new EntityDropItemAction());
        actions.add(new EntityMoveAction());
        actions.add(new EntityPickupItemAction());
        actions.add(new EntityPlaceAction());
        actions.add(new EntitySpawnAction());  // AbstractEntityAction を継承してない(引数都合)

        return actions;
    }

    protected boolean checkMatchedEntityEvent(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof EntityEvent))
            return false;

        EntityEvent e = (EntityEvent) event;
        return argument.checkMatchedEntity(e.getEntity());
    }

    protected boolean checkMatchedEntity(String specifier, @NotNull Entity actualEntity)
    {
        return EntityUtils.selectEntities(specifier)
                .stream()
                .anyMatch(entity -> Objects.equals(entity.getUniqueId(), actualEntity.getUniqueId()));
    }

    protected EntityArgumentHolder<Entity> deserializeTarget(Map<String, Object> map, StructureSerializer serializer)
    {
        return EntityArgumentHolder.tryDeserialize(
                map.get(AbstractEntityActionArgument.KEY_TARGET_ENTITY),
                serializer,
                EntityStructure.class
        );
    }

    protected <E extends Entity> EntityArgumentHolder<E> deserializeTarget(
            Map<String, Object> map,
            StructureSerializer serializer,
            Class<? extends EntityStructure> structureClass)
    {
        return EntityArgumentHolder.tryDeserialize(
                map.get(AbstractEntityActionArgument.KEY_TARGET_ENTITY),
                serializer,
                structureClass
        );
    }

}
