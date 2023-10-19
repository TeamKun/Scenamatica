package org.kunlab.scenamatica.action.actions.inventory;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InventoryOpenAction extends AbstractInventoryAction<InventoryOpenAction.Argument>
        implements Executable<InventoryOpenAction.Argument>, Watchable<InventoryOpenAction.Argument>
{
    public static final String KEY_ACTION_NAME = "inventory_open";

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
        InventoryBean inventoryBean = argument.getInventory();
        assert inventoryBean != null;
        Inventory inventory = inventoryBean.createInventory();

        player.openInventory(inventory);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        argument = this.requireArgsNonNull(argument);
        if (!super.checkMatchedInventoryEvent(argument, engine, event))
            return false;

        Player expectedPlayer = null;
        if (argument.getTargetSpecifier() != null)
            expectedPlayer = argument.getTarget();

        assert event instanceof InventoryOpenEvent;
        InventoryOpenEvent e = (InventoryOpenEvent) event;
        HumanEntity player = e.getPlayer();

        return expectedPlayer == null || expectedPlayer.getUniqueId().equals(player.getUniqueId());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryOpenEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeInventoryIfContains(map, serializer),
                MapUtils.getOrNull(map, Argument.KEY_TARGET_PLAYER)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractInventoryArgument
    {
        public static final String KEY_TARGET_PLAYER = "target";

        String targetPlayer;

        public Argument(@Nullable InventoryBean inventory, String targetPlayer)
        {
            super(inventory);
            this.targetPlayer = targetPlayer;
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

            return Objects.equals(this.targetPlayer, arg.targetPlayer);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                ensureNotPresent(KEY_TARGET_PLAYER, this.targetPlayer);
                ensureNotPresent(KEY_INVENTORY, this.inventory);
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_TARGET_PLAYER, this.targetPlayer
            );
        }
    }
}
