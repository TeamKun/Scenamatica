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
    public void execute(@NotNull ScenarioEngine engine, @NotNull InventoryOpenAction.Argument argument)
    {
        Player player = argument.getTargetSpecifier().selectTarget(engine.getContext())
                .orElseThrow(() -> new IllegalStateException("Cannot select target for this action, please specify target with valid specifier."));

        InventoryStructure inventoryStructure = argument.getInventory();
        assert inventoryStructure != null;
        Inventory inventory = inventoryStructure.create();

        player.openInventory(inventory);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedInventoryEvent(argument, engine, event))
            return false;

        assert event instanceof InventoryOpenEvent;
        InventoryOpenEvent e = (InventoryOpenEvent) event;
        HumanEntity player = e.getPlayer();
        if (!(player instanceof Player))
            return false;

        return (!argument.getTargetSpecifier().canProvideTarget() || argument.getTargetSpecifier().checkMatchedPlayer((Player) player));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryOpenEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(
                super.deserializeInventoryIfContains(map, serializer),
                serializer.tryDeserializePlayerSpecifier(map.get(InventoryOpenAction.Argument.KEY_TARGET_PLAYER))
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractInventoryArgument
    {
        public static final String KEY_TARGET_PLAYER = "target";

        @NotNull
        PlayerSpecifier targetSpecifier;

        public Argument(@Nullable InventoryStructure inventory, @NotNull PlayerSpecifier targetSpecifier)
        {
            super(inventory);
            this.targetSpecifier = targetSpecifier;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;
            else if (!super.isSame(argument))
                return false;

            Argument arg = (Argument) argument;

            return Objects.equals(this.targetSpecifier, arg.targetSpecifier);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                if (!this.targetSpecifier.canProvideTarget())
                    throw new IllegalArgumentException("Cannot select target for this action, please specify target with valid specifier.");

                ensurePresent(KEY_INVENTORY, this.inventory);
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_TARGET_PLAYER, this.targetSpecifier
            );
        }
    }
}
