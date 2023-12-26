package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
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

    private static boolean isConsumable(@NotNull ItemStack item)
    {
        Material type = item.getType();
        return type.isEdible() || type == Material.POTION || type == Material.MILK_BUCKET;
    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Actor actor = PlayerUtils.getActorOrThrow(engine, selectTarget(argument, engine));

        if (argument.isPresent(IN_ITEM))
            actor.getPlayer().getInventory().setItemInMainHand(argument.get(IN_ITEM).create());
        else if (!isConsumable(actor.getPlayer().getInventory().getItemInMainHand()))
            throw new IllegalArgumentException("The item in the main hand is not consumable.");

        // 食べ初めをトリガするので、シナリオタイムアウトになるかもしれない。
        actor.consume(EquipmentSlot.HAND);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerItemConsumeEvent;
        PlayerItemConsumeEvent e = (PlayerItemConsumeEvent) event;

        ItemStack item = e.getItem();
        ItemStack replacement = e.getReplacement();

        return argument.ifPresent(IN_ITEM, i -> i.isAdequate(item))
                && argument.ifPresent(IN_REPLACEMENT, r -> r.isAdequate(replacement));
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
        if (type == ScenarioType.ACTION_EXECUTE)
            board.register(IN_REPLACEMENT);

        return board;
    }
}
