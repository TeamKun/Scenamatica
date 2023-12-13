package org.kunlab.scenamatica.action.actions.inventory;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.specifiers.PlayerSpecifierImpl;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class InventoryCloseAction extends AbstractInventoryAction<InventoryCloseAction.Argument>
        implements Executable<InventoryCloseAction.Argument>, Watchable<InventoryCloseAction.Argument>
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

        Player player = argument.getTargetSpecifier().selectTarget(engine.getContext());
        if (player == null)
            throw new IllegalStateException("Cannot select target for this action, please specify target with valid specifier.");

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

        assert event instanceof InventoryCloseEvent;
        InventoryCloseEvent e = (InventoryCloseEvent) event;
        HumanEntity player = e.getPlayer();

        return (!argument.getTargetSpecifier().canProvideTarget() || argument.getTargetSpecifier().checkMatchedPlayer((Player) player))
                && (argument.getReason() == null || argument.getReason() == e.getReason());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryCloseEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(
                super.deserializeInventoryIfContains(map, serializer),
                PlayerSpecifierImpl.tryDeserializePlayer(map.get(Argument.KEY_TARGET_PLAYER), serializer),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_REASON, InventoryCloseEvent.Reason.class)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractInventoryArgument
    {
        public static final String KEY_TARGET_PLAYER = "target";
        public static final String KEY_REASON = "reason";

        @NotNull
        PlayerSpecifier targetSpecifier;
        InventoryCloseEvent.Reason reason;

        public Argument(@Nullable InventoryStructure inventory, @NotNull PlayerSpecifier targetSpecifier, InventoryCloseEvent.Reason reason)
        {
            super(inventory);
            this.targetSpecifier = targetSpecifier;
            this.reason = reason;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;
            else if (!super.isSame(argument))
                return false;

            Argument arg = (Argument) argument;

            return Objects.equals(this.targetSpecifier, arg.targetSpecifier)
                    && this.reason == arg.reason;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                ensurePresent(Argument.KEY_TARGET_PLAYER, this.targetSpecifier);
                ensureNotPresent(Argument.KEY_INVENTORY, this.inventory);
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_TARGET_PLAYER, this.targetSpecifier,
                    KEY_REASON, this.reason
            );
        }
    }
}
