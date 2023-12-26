package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;

import java.util.Collections;
import java.util.List;

public class PlayerInteractEntityAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_interact_entity";
    public static final InputToken<EntitySpecifier<Entity>> IN_ENTITY = ofSpecifier("entity");
    public static final InputToken<EquipmentSlot> IN_HAND = ofEnumInput("hand", EquipmentSlot.class)
            .validator(
                    (slot) -> slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND,
                    "The hand must be either hand or off hand"
            );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Player player = selectTarget(argument, engine);
        Entity targetEntity = argument.get(IN_ENTITY).selectTarget(engine.getContext())
                .orElseThrow(() -> new IllegalStateException("Target entity is not found."));

        int distanceFromEntity = (int) player.getLocation().distance(targetEntity.getLocation());
        if (distanceFromEntity > 36)
        {
            engine.getPlugin().getLogger().warning(engine.getLogPrefix() + "The distance between player and entity is too far. ("
                    + distanceFromEntity + " blocks), so the actual action will not be executed(only event will be fired).");

            this.eventOnlyMode(engine, argument, player, targetEntity);
            return;
        }

        Actor actor = PlayerUtils.getActorOrThrow(engine, player);
        this.doInteract(argument, targetEntity, actor);
    }

    protected void doInteract(InputBoard argument, Entity targeTentity, Actor actor)
    {
        actor.interactEntity(
                targeTentity,
                NMSEntityUseAction.INTERACT,
                argument.orElse(IN_HAND, () -> EquipmentSlot.HAND),
                actor.getPlayer().getLocation()
        );
    }

    private void eventOnlyMode(@NotNull ScenarioEngine engine, @NotNull InputBoard argument, @NotNull Player who, @NotNull Entity targetEntity)
    {
        PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(
                who,
                targetEntity,
                argument.orElse(IN_HAND, () -> EquipmentSlot.HAND)
        );

        engine.getPlugin().getServer().getPluginManager().callEvent(event);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerInteractEntityEvent;
        PlayerInteractEntityEvent e = (PlayerInteractEntityEvent) event;

        return argument.ifPresent(IN_ENTITY, entity -> entity.checkMatchedEntity(e.getRightClicked()))
                && argument.ifPresent(IN_HAND, hand -> hand == e.getHand());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerInteractEntityEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_ENTITY, IN_HAND);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_TARGET);

        return board;
    }
}
