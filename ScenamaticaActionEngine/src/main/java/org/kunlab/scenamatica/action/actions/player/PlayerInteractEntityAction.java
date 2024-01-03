package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
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

    public static final String KEY_OUT_ENTITY = "entity";
    public static final String KEY_OUT_HAND = "hand";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        Entity targetEntity = ctxt.input(IN_ENTITY).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalStateException("Target entity is not found."));
        EquipmentSlot hand = ctxt.orElseInput(IN_HAND, () -> EquipmentSlot.HAND);

        int distanceFromEntity = (int) player.getLocation().distance(targetEntity.getLocation());
        if (distanceFromEntity > 36)
        {
            ctxt.getLogger().warning("The distance between player and entity is too far. ("
                    + distanceFromEntity + " blocks), so the actual action will not be executed(only event will be fired).");

            this.eventOnlyMode(ctxt, player, targetEntity, hand);
            return;
        }

        Actor actor = ctxt.getActorOrThrow(player);
        this.doInteract(ctxt, targetEntity, actor, hand);
    }

    protected void doInteract(ActionContext ctxt, Entity targeTentity, Actor actor, @NotNull EquipmentSlot hand)
    {
        this.makeOutputs(ctxt, actor.getPlayer(), targeTentity, hand);
        actor.interactEntity(
                targeTentity,
                NMSEntityUseAction.INTERACT,
                hand,
                actor.getPlayer().getLocation()
        );
    }

    private void eventOnlyMode(@NotNull ActionContext ctxt, @NotNull Player who, @NotNull Entity targetEntity,
                               @NotNull EquipmentSlot hand)
    {
        this.makeOutputs(ctxt, who, targetEntity, hand);
        PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(
                who,
                targetEntity,
                hand
        );

        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerInteractEntityEvent;
        PlayerInteractEntityEvent e = (PlayerInteractEntityEvent) event;

        boolean result = ctxt.ifHasInput(IN_ENTITY, entity -> entity.checkMatchedEntity(e.getRightClicked()))
                && ctxt.ifHasInput(IN_HAND, hand -> hand == e.getHand());
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getRightClicked(), e.getHand());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @Nullable Entity targetEntity,
                               @NotNull EquipmentSlot hand)
    {
        ctxt.output(KEY_OUT_ENTITY, targetEntity);
        ctxt.output(KEY_OUT_HAND, hand);
        super.makeOutputs(ctxt, player);
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
