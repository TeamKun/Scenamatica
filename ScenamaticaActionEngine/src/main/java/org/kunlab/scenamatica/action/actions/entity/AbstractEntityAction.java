package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        actions.add(new EntitySpawnAction<>());  // AbstractEntityAction を継承してない(引数都合)
        actions.add(new ProjectileHitAction());
        actions.add(new ProjectileLaunchAction());

        return actions;
    }

    protected boolean checkMatchedEntityEvent(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof EntityEvent))
            return false;

        EntityEvent e = (EntityEvent) event;
        return argument.checkMatchedEntity(e.getEntity());
    }

    protected EntitySpecifier<Entity> deserializeTarget(Map<String, Object> map, StructureSerializer serializer)
    {
        return serializer.tryDeserializeEntitySpecifier(map.get(AbstractEntityActionArgument.KEY_TARGET_ENTITY));
    }

    protected <E extends Entity> EntitySpecifier<E> deserializeTarget(
            Map<String, Object> map,
            StructureSerializer serializer,
            Class<? extends EntityStructure> structureClass)
    {
        return serializer.tryDeserializeEntitySpecifier(
                map.get(AbstractEntityActionArgument.KEY_TARGET_ENTITY),
                structureClass
        );
    }

}
