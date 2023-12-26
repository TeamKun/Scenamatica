package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlayerItemDamageAction extends AbstractPlayerAction
        implements Executable, Watchable, Requireable
{
    public static final String KEY_ACTION_NAME = "player_item_damage";
    public static final InputToken<ItemStackStructure> IN_ITEM = ofInput(
            "item",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );
    public static final InputToken<Integer> IN_DAMAGE = ofInput(
            "damage",
            Integer.class
    );
    public static final InputToken<EquipmentSlot> IN_SLOT = ofEnumInput(
            "slot",
            EquipmentSlot.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Actor actor = PlayerUtils.getActorOrThrow(engine, selectTarget(argument, engine));
        EquipmentSlot slot = argument.orElse(IN_SLOT, () -> EquipmentSlot.HAND);

        argument.runIfPresent(IN_ITEM, item -> actor.getPlayer().getInventory().setItem(slot, item.create()));

        ItemStack itemStack = actor.getPlayer().getInventory().getItem(slot);
        if (itemStack == null)
            throw new IllegalStateException("Target does not have item in slot " + slot);
        else if (!(itemStack.getItemMeta() instanceof Damageable))
            throw new IllegalStateException("Target item in slot " + slot + " is not Damageable");

        actor.damageItem(slot, argument.get(IN_DAMAGE));
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        PlayerItemDamageEvent e = (PlayerItemDamageEvent) event;

        return argument.ifPresent(IN_ITEM, item -> item.isAdequate(e.getItem()))
                && argument.ifPresent(IN_DAMAGE, damage -> damage == e.getDamage());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerItemDamageEvent.class
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        Player player = selectTarget(argument, engine);

        List<EquipmentSlot> slotToCheck;
        if (argument.isPresent(IN_SLOT))
            slotToCheck = Collections.singletonList(argument.get(IN_SLOT));
        else
            slotToCheck = Arrays.asList(EquipmentSlot.values());

        int expectedDamage = argument.get(IN_DAMAGE);
        for (EquipmentSlot slot : slotToCheck)
        {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null)
                continue;

            ItemMeta meta = item.getItemMeta();
            if (!(meta instanceof Damageable))
                continue;
            Damageable damageable = (Damageable) meta;

            if (!argument.ifPresent(IN_ITEM, itemStructure -> itemStructure.isAdequate(item)))
                continue;

            if (expectedDamage == damageable.getDamage())
                return true;
        }

        return false;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_ITEM, IN_DAMAGE, IN_SLOT);
        if (type == ScenarioType.CONDITION_REQUIRE || type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_DAMAGE);

        return board;
    }
}
