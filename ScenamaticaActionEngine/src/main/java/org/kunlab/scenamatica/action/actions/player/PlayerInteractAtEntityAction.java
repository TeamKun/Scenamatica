package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerInteractAtEntityAction extends PlayerInteractEntityAction<PlayerInteractAtEntityAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_interact_at_entity";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    protected void doInteract(PlayerInteractAtEntityAction.Argument argument, Entity targeTentity, Actor actor)
    {
        actor.interactEntity(
                targeTentity,
                NMSEntityUseAction.INTERACT_AT,
                argument.getHand(),
                argument.getPosition().create()
        );
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.isFired(argument, engine, event))
            return false;

        PlayerInteractAtEntityEvent e = (PlayerInteractAtEntityEvent) event;
        Vector clickedPosition = e.getClickedPosition();
        Location loc = clickedPosition.toLocation(engine.getContext().getStage());

        return argument.getPosition() == null || argument.getPosition().isAdequate(loc);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerInteractAtEntityEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        LocationStructure position = null;
        if (map.containsKey(Argument.KEY_POSITION))
            position = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(Argument.KEY_POSITION)),
                    LocationStructure.class
            );

        return new Argument(
                super.deserializeArgument(
                        map,
                        serializer
                ),
                position
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends PlayerInteractEntityAction.Argument
    {
        public static final String KEY_POSITION = "position";

        LocationStructure position;  // ほんとは Vector だけれど, シリアライズ/デシリアライズの簡易性から Location で代用。

        public Argument(PlayerInteractEntityAction.Argument argument, LocationStructure position)
        {
            super(argument.getTargetSpecifier(), argument.getEntity(), argument.getHand());
            this.position = position;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(argument)
                    && Objects.equals(this.position, arg.position);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);

            if (type == ScenarioType.ACTION_EXECUTE)
                ensurePresent(KEY_POSITION, this.position);
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_POSITION, this.position
            );
        }
    }
}
