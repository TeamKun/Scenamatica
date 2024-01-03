package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;

import java.util.Collections;
import java.util.List;

public class PlayerItemConsumeAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_item_consume";
    public static final InputToken<ItemStackStructure> IN_ITEM = ofInput(
            "item",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );
    public static final InputToken<ItemStackStructure> IN_REPLACEMENT = ofInput(
            "replacement",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );

    public static final String KEY_OUT_ITEM = "item";
    public static final String KEY_OUT_REPLACEMENT = "replacement";

    private static boolean isConsumable(@NotNull ItemStack item)
    {
        Material type = item.getType();
        return type.isEdible() || type == Material.POTION || type == Material.MILK_BUCKET;
    }

    private static ItemStack getReplacement(@NotNull ItemStack item)
    {
        Material type = item.getType();
        if (type == Material.POTION)
            return new ItemStack(Material.GLASS_BOTTLE);
        else if (type == Material.MILK_BUCKET)
            return new ItemStack(Material.BUCKET);
        else if (item.getAmount() > 1)
        {
            ItemStack newStack = item.clone();
            newStack.setAmount(item.getAmount() - 1);
            return newStack;
        }
        else
            return null;

    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Actor actor = ctxt.getActorOrThrow(selectTarget(ctxt));

        if (ctxt.hasInput(IN_ITEM))
            actor.getPlayer().getInventory().setItemInMainHand(ctxt.input(IN_ITEM).create());

        ItemStack item = actor.getPlayer().getInventory().getItemInMainHand();
        if (!isConsumable(item))
            throw new IllegalArgumentException("The item in the main hand is not consumable.");

        this.makeOutputs(ctxt, actor.getPlayer(), item, getReplacement(item));
        // 食べ初めをトリガするので、シナリオタイムアウトになるかもしれない。
        actor.consume(EquipmentSlot.HAND);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerItemConsumeEvent;
        PlayerItemConsumeEvent e = (PlayerItemConsumeEvent) event;

        ItemStack item = e.getItem();
        ItemStack replacement = e.getReplacement();

        boolean result = ctxt.ifHasInput(IN_ITEM, i -> i.isAdequate(item))
                && ctxt.ifHasInput(IN_REPLACEMENT, r -> r.isAdequate(replacement));
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), item, replacement);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull ItemStack item, @Nullable ItemStack replacement)
    {
        ctxt.output(KEY_OUT_ITEM, item);
        if (replacement != null)
            ctxt.output(KEY_OUT_REPLACEMENT, replacement);

        super.makeOutputs(ctxt, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerItemConsumeEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_ITEM);
        if (type != ScenarioType.ACTION_EXECUTE)
            board.register(IN_REPLACEMENT);

        return board;
    }
}
