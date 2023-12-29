package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
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
    public void execute(@NotNull ActionContext ctxt)
    {
        Actor actor = ctxt.getActorOrThrow(selectTarget(ctxt));
        EquipmentSlot slot = ctxt.orElseInput(IN_SLOT, () -> EquipmentSlot.HAND);

        ctxt.runIfHasInput(IN_ITEM, item -> actor.getPlayer().getInventory().setItem(slot, item.create()));

        ItemStack itemStack = actor.getPlayer().getInventory().getItem(slot);
        if (itemStack == null)
            throw new IllegalStateException("Target does not have item in slot " + slot);
        else if (!(itemStack.getItemMeta() instanceof Damageable))
            throw new IllegalStateException("Target item in slot " + slot + " is not Damageable");

        actor.damageItem(slot, ctxt.input(IN_DAMAGE));
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        PlayerItemDamageEvent e = (PlayerItemDamageEvent) event;

        return ctxt.ifHasInput(IN_ITEM, item -> item.isAdequate(e.getItem()))
                && ctxt.ifHasInput(IN_DAMAGE, damage -> damage == e.getDamage());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerItemDamageEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);

        List<EquipmentSlot> slotToCheck;
        if (ctxt.hasInput(IN_SLOT))
            slotToCheck = Collections.singletonList(ctxt.input(IN_SLOT));
        else
            slotToCheck = Arrays.asList(EquipmentSlot.values());

        int expectedDamage = ctxt.input(IN_DAMAGE);
        for (EquipmentSlot slot : slotToCheck)
        {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null)
                continue;

            ItemMeta meta = item.getItemMeta();
            if (!(meta instanceof Damageable))
                continue;
            Damageable damageable = (Damageable) meta;

            if (!ctxt.ifHasInput(IN_ITEM, itemStructure -> itemStructure.isAdequate(item)))
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
