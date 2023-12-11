package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerItemDamageAction extends AbstractPlayerAction<PlayerItemDamageAction.Argument>
        implements Executable<PlayerItemDamageAction.Argument>, Watchable<PlayerItemDamageAction.Argument>,
        Requireable<PlayerItemDamageAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_item_damage";

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
        ItemStackStructure item = argument.getItem();
        if (item != null)
            actor.getPlayer().getInventory().setItem(slot, item.create());

        ItemStack itemStack = actor.getPlayer().getInventory().getItem(slot);
        if (itemStack == null)
            throw new IllegalStateException("Target does not have item in slot " + slot);
        else if (!(itemStack.getItemMeta() instanceof Damageable))
            throw new IllegalStateException("Target item in slot " + slot + " is not Damageable");

        int damageAmount = argument.getDamage();
        actor.damageItem(slot, damageAmount);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        PlayerItemDamageEvent e = (PlayerItemDamageEvent) event;

        return (argument.getItem() == null || argument.getItem().isAdequate(e.getItem()))
                && (argument.getDamage() == null || argument.getDamage() == e.getDamage());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerItemDamageEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        ItemStackStructure item = null;
        if (map.containsKey(Argument.KEY_ITEM))
            item = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(Argument.KEY_ITEM)),
                    ItemStackStructure.class
            );

        return new Argument(
                super.deserializeTarget(map),
                item,
                MapUtils.getAsNumberOrNull(map, Argument.KEY_DAMAGE, Number::intValue),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_SLOT, EquipmentSlot.class)
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        Player player = argument.getTarget();

        List<EquipmentSlot> slotToCheck;
        if (argument.getSlot() == null)
            slotToCheck = Arrays.asList(EquipmentSlot.values());
        else
            slotToCheck = Collections.singletonList(argument.getSlot());

        ItemStackStructure itemToCheck = argument.getItem();
        int expectedDamage = argument.getDamage();
        for (EquipmentSlot slot : slotToCheck)
        {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null)
                continue;

            ItemMeta meta = item.getItemMeta();
            if (!(meta instanceof Damageable))
                continue;
            Damageable damageable = (Damageable) meta;

            if (!(itemToCheck == null || itemToCheck.isAdequate(item)))
                continue;

            if (expectedDamage == damageable.getDamage())
                return true;
        }

        return false;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_ITEM = "item";
        public static final String KEY_DAMAGE = "damage";
        public static final String KEY_SLOT = "slot";

        ItemStackStructure item;
        Integer damage;
        EquipmentSlot slot;

        public Argument(String target, ItemStackStructure item, Integer damage, EquipmentSlot slot)
        {
            super(target);
            this.item = item;
            this.damage = damage;
            this.slot = slot;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);

            switch (type)
            {
                case CONDITION_REQUIRE:
                case ACTION_EXECUTE:
                    ensurePresent(KEY_DAMAGE, this.damage);
            }

        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(argument)
                    && Objects.equals(this.item, arg.item)
                    && Objects.equals(this.damage, arg.damage);
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_ITEM, this.item,
                    KEY_DAMAGE, this.damage
            );
        }
    }
}
