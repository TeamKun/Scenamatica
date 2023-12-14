package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.specifiers.EntitySpecifierImpl;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerInteractEntityAction<A extends PlayerInteractEntityAction.Argument> extends AbstractPlayerAction<A>
        implements Executable<A>, Watchable<A>
{
    public static final String KEY_ACTION_NAME = "player_interact_entity";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable A argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player player = argument.getTarget(engine);
        Entity targetEntity = argument.getEntity().selectTarget(engine.getContext());
        if (targetEntity == null)
            throw new IllegalStateException("Cannot select target for this action, please specify target with valid specifier.");

        int distanceFromEntity = (int) player.getLocation().distance(targetEntity.getLocation());
        if (distanceFromEntity > 36)
        {
            engine.getPlugin().getLogger().warning(engine.getLogPrefix() + "The distance between player and entity is too far. ("
                    + distanceFromEntity + " blocks), so the actual action will not be executed(only event will be fired).");

            this.eventOnlyMode(engine, argument, targetEntity);
            return;
        }

        Actor actor = PlayerUtils.getActorOrThrow(engine, player);
        this.doInteract(argument, targetEntity, actor);
    }

    protected void doInteract(A argument, Entity targeTentity, Actor actor)
    {
        actor.interactEntity(targeTentity, NMSEntityUseAction.INTERACT, argument.getHand(), actor.getPlayer().getLocation());
    }

    private void eventOnlyMode(@NotNull ScenarioEngine engine, @NotNull A argument, @NotNull Entity targetEntity)
    {
        PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(
                argument.getTarget(engine),
                targetEntity,
                argument.getHand() == null ? EquipmentSlot.HAND: argument.getHand()
        );

        engine.getPlugin().getServer().getPluginManager().callEvent(event);
    }

    @Override
    public boolean isFired(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        PlayerInteractEntityEvent e = (PlayerInteractEntityEvent) event;
        return (argument.getHand() == null || argument.getHand() == e.getHand())
                && (argument.getEntity().isSelectable() || argument.getEntity().checkMatchedEntity(e.getRightClicked()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerInteractEntityEvent.class
        );
    }

    @Override
    public A deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        // noinspection unchecked
        return (A) new Argument(
                super.deserializeTarget(map, serializer),
                EntitySpecifierImpl.tryDeserialize(map.get(Argument.KEY_ENTITY), serializer),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_HAND, EquipmentSlot.class)
        );
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_ENTITY = "entity";
        public static final String KEY_HAND = "hand";

        @NotNull
        EntitySpecifier<?> entity;
        EquipmentSlot hand;

        public Argument(PlayerSpecifier target, @NotNull EntitySpecifier<?> entity, EquipmentSlot hand)
        {
            super(target);
            this.entity = entity;
            this.hand = hand;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);
            if (this.hand != null && !(this.hand == EquipmentSlot.HAND || this.hand == EquipmentSlot.OFF_HAND))
                throw new IllegalStateException("Hand must be either HAND or OFF_HAND");

            if (type == ScenarioType.ACTION_EXECUTE
                    && !this.entity.isSelectable())
                throw new IllegalStateException("Entity must be selectable on execute action");

        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(argument)
                    && Objects.equals(this.entity, arg.entity)
                    && this.hand == arg.hand;
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_ENTITY, this.entity,
                    KEY_HAND, this.hand
            );
        }
    }
}
