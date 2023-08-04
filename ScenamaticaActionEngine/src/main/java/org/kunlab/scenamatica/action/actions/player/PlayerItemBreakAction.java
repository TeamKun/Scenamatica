package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerItemBreakAction extends AbstractPlayerAction<PlayerItemBreakAction.Argument>
        implements Executable<PlayerItemBreakAction.Argument>, Watchable<PlayerItemBreakAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_item_break";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Actor actor = PlayerUtils.getActorOrThrow(engine, argument.getTarget());
        EquipmentSlot slot = argument.getSlot() == null ? EquipmentSlot.HAND: argument.getSlot();
        ItemStackBean item = argument.getItem();
        if (item != null)
            actor.getPlayer().getInventory().setItem(slot, item.toItemStack());

        PlayerUtils.getActorOrThrow(engine, argument.getTarget()).breakItem(slot);
    }

    private boolean isDamageable(ItemStack itemInMainHand)
    {
        Material type = itemInMainHand.getType();
        switch (type)
        {
            // <editor-fold desc="Tools">
            case WOODEN_AXE:
            case WOODEN_HOE:
            case WOODEN_PICKAXE:
            case WOODEN_SHOVEL:
            case WOODEN_SWORD:
            case STONE_AXE:
            case STONE_HOE:
            case STONE_PICKAXE:
            case STONE_SHOVEL:
            case STONE_SWORD:
            case IRON_AXE:
            case IRON_HOE:
            case IRON_PICKAXE:
            case IRON_SHOVEL:
            case IRON_SWORD:
            case GOLDEN_AXE:
            case GOLDEN_HOE:
            case GOLDEN_PICKAXE:
            case GOLDEN_SHOVEL:
            case GOLDEN_SWORD:
            case DIAMOND_AXE:
            case DIAMOND_HOE:
            case DIAMOND_PICKAXE:
            case DIAMOND_SHOVEL:
            case DIAMOND_SWORD:
            case NETHERITE_AXE:
            case NETHERITE_HOE:
            case NETHERITE_PICKAXE:
            case NETHERITE_SHOVEL:
            case NETHERITE_SWORD:
            case SHEARS:
            case FISHING_ROD:
            case FLINT_AND_STEEL:
            case SHIELD:
            case BOW:
            case CROSSBOW:
            case CARROT_ON_A_STICK:
            case WARPED_FUNGUS_ON_A_STICK:
                // </editor-fold>
                // <editor-fold desc="Armors">
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case GOLDEN_HELMET:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
            case NETHERITE_HELMET:
            case NETHERITE_CHESTPLATE:
            case NETHERITE_LEGGINGS:
            case NETHERITE_BOOTS:
                // </editor-fold>
            case ELYTRA:
            case TRIDENT:
            case TURTLE_HELMET:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerItemBreakEvent;
        PlayerItemBreakEvent e = (PlayerItemBreakEvent) event;

        return argument.getItem() == null || BeanUtils.isSame(
                argument.getItem(),
                e.getBrokenItem(),
                /* strict */ false
        );
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerItemBreakEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        ItemStackBean item = null;
        if (map.containsKey(Argument.KEY_ITEM))
            item = serializer.deserializeItemStack(MapUtils.checkAndCastMap(
                    map.get(Argument.KEY_ITEM),
                    String.class,
                    Object.class
            ));

        return new Argument(
                super.deserializeTarget(map),
                item,
                MapUtils.getAsEnumOrNull(map, Argument.KEY_SLOT, EquipmentSlot.class)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_ITEM = "item";
        public static final String KEY_SLOT = "slot";

        @Nullable
        ItemStackBean item;
        @Nullable
        EquipmentSlot slot;

        public Argument(@NotNull String target, @Nullable ItemStackBean item, @Nullable EquipmentSlot slot)
        {
            super(target);
            this.item = item;
            this.slot = slot;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && Objects.equals(this.item, arg.item);
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_ITEM, this.item
            );
        }
    }
}
