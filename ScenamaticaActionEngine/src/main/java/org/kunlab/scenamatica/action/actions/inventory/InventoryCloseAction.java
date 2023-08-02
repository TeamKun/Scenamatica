package org.kunlab.scenamatica.action.actions.inventory;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InventoryCloseAction extends AbstractInventoryAction<InventoryCloseAction.Argument>
        implements Watchable<InventoryCloseAction.Argument>
{
    public static final String KEY_ACTION_NAME = "inventory_close";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player player = argument.getTarget();
        InventoryCloseEvent.Reason reason = argument.getReason();

        if (reason == null)
            player.closeInventory();
        else
            player.closeInventory(reason);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        argument = this.requireArgsNonNull(argument);
        if (!super.checkMatchedInventoryEvent(argument, engine, event))
            return false;

        Player expectedPlayer = argument.getTarget();

        assert event instanceof InventoryCloseEvent;
        InventoryCloseEvent e = (InventoryCloseEvent) event;
        HumanEntity player = e.getPlayer();

        return (expectedPlayer == null || expectedPlayer.getUniqueId().equals(player.getUniqueId()))
                && (argument.getReason() == null || argument.getReason() == e.getReason());
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);
        if (type == ScenarioType.ACTION_EXECUTE)
        {
            this.throwIfNotPresent(Argument.KEY_TARGET_PLAYER, argument.getTargetSpecifier());
            this.throwIfPresent(Argument.KEY_INVENTORY, argument.getInventory());
        }
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryCloseEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeInventoryIfContains(map, serializer),
                MapUtils.getOrNull(map, Argument.KEY_TARGET_PLAYER),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_REASON, InventoryCloseEvent.Reason.class)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractInventoryArgument
    {
        public static final String KEY_TARGET_PLAYER = "target";
        public static final String KEY_REASON = "reason";

        String targetPlayer;
        InventoryCloseEvent.Reason reason;

        public Argument(@Nullable InventoryBean inventory, String targetPlayer, InventoryCloseEvent.Reason reason)
        {
            super(inventory);
            this.targetPlayer = targetPlayer;
            this.reason = reason;
        }

        public String getTargetSpecifier()
        {
            return this.targetPlayer;
        }

        public Player getTarget()
        {
            return PlayerUtils.getPlayerOrThrow(this.targetPlayer);
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;
            else if (!super.isSame(argument))
                return false;

            Argument arg = (Argument) argument;

            return Objects.equals(this.targetPlayer, arg.targetPlayer)
                    && this.reason == arg.reason;
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_TARGET_PLAYER, this.targetPlayer,
                    KEY_REASON, this.reason
            );
        }
    }
}
