package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;

import java.util.Collections;
import java.util.List;

@Action("player_hotbar")
@ActionDoc(
        name = "プレイヤのホットバースロット",
        description = "プレイヤのホットバースロットを変更します。",
        events = {
                PlayerItemHeldEvent.class
        },

        executable = "プレイヤのホットバースロットを変更します。",
        expectable = "プレイヤのホットバースロットが変更されることを期待します。",
        requireable = "プレイヤのホットバースロットが指定されたものであることを要求します。",

        outputs = {
                @OutputDoc(
                        name = PlayerHotbarSlotAction.KEY_OUTPUT_SLOT,
                        description = "変更されたスロットです。",
                        type = Integer.class
                ),
                @OutputDoc(
                        name = PlayerHotbarSlotAction.KEY_OUTPUT_PREVIOUS_SLOT,
                        description = "変更前のスロットです。",
                        type = Integer.class
                ),
                @OutputDoc(
                        name = PlayerHotbarSlotAction.KEY_OUTPUT_ITEM,
                        description = "変更されたアイテムです。",
                        type = ItemStackStructure.class
                )
        }
)
public class PlayerHotbarSlotAction extends AbstractPlayerAction
        implements Executable, Expectable, Requireable
{
    @InputDoc(
            name = "slot",
            description = "変更するスロットのインデックスを指定します。",
            type = int.class,
            min = 0,
            max = 8
    )
    public static final InputToken<Integer> IN_CURRENT_SLOT = ofInput(
            "slot",
            Integer.class
    ).validator(value -> value >= 0 && value <= 8, "Slot must be between 0 and 8");
    @InputDoc(
            name = "previous",
            description = "変更前のスロットのインデックスを指定します。",
            type = int.class,
            min = 0,
            max = 8
    )
    public static final InputToken<Integer> IN_PREVIOUS_SLOT = ofInput(
            "previous",
            Integer.class
    ).validator(value -> value >= 0 && value <= 8, "Previous slot must be between 0 and 8");
    @InputDoc(
            name = "item",
            description = "変更するアイテムを指定します。",
            type = ItemStack.class
    )
    public static final InputToken<ItemStackStructure> IN_CURRENT_ITEM = ofInput(
            "item",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );

    public static final String KEY_OUTPUT_SLOT = "slot";
    public static final String KEY_OUTPUT_PREVIOUS_SLOT = "previous";
    public static final String KEY_OUTPUT_ITEM = "item";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player p = selectTarget(ctxt);
        int slot = ctxt.input(IN_CURRENT_SLOT);

        ItemStack stack = ctxt.ifHasInput(IN_CURRENT_ITEM, (r) -> {
            ItemStack st = r.create();
            p.getInventory().setItem(slot, st);
            return st;
        }, p.getInventory().getItemInMainHand());

        this.makeOutputs(ctxt, p, p.getInventory().getHeldItemSlot(), slot, stack);
        p.getInventory().setHeldItemSlot(slot);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerItemHeldEvent;
        PlayerItemHeldEvent e = (PlayerItemHeldEvent) event;

        int currentSlot = e.getNewSlot();
        int previousSlot = e.getPreviousSlot();
        ItemStack currentItem = e.getPlayer().getInventory().getItem(currentSlot);

        boolean result = ctxt.ifHasInput(IN_CURRENT_SLOT, slot -> slot == currentSlot)
                && ctxt.ifHasInput(IN_PREVIOUS_SLOT, slot -> slot == previousSlot)
                && ctxt.ifHasInput(IN_CURRENT_ITEM, item -> item.isAdequate(currentItem));

        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), previousSlot, currentSlot, currentItem);

        return result;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerItemHeldEvent.class
        );
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @Nullable Integer prev, @Nullable Integer slot, @Nullable ItemStack item)
    {
        ctxt.output(KEY_OUTPUT_SLOT, slot);
        if (prev != null)
            ctxt.output(KEY_OUTPUT_PREVIOUS_SLOT, prev);
        ctxt.output(KEY_OUTPUT_PREVIOUS_SLOT, prev);
        if (item != null)
            ctxt.output(KEY_OUTPUT_ITEM, item);

        super.makeOutputs(ctxt, player);
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Player p = selectTarget(ctxt);
        boolean result = ctxt.ifHasInput(IN_CURRENT_SLOT, slot -> slot == p.getInventory().getHeldItemSlot())
                && ctxt.ifHasInput(IN_CURRENT_ITEM, item -> item.isAdequate(p.getInventory().getItemInMainHand()));
        if (result)
            this.makeOutputs(ctxt, p, null, p.getInventory().getHeldItemSlot(), p.getInventory().getItemInMainHand());

        return result;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_CURRENT_SLOT, IN_CURRENT_ITEM);

        if (type == ScenarioType.ACTION_EXPECT || type == ScenarioType.ACTION_EXECUTE)
            board.registerAll(IN_PREVIOUS_SLOT);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_CURRENT_SLOT);

        return board;
    }
}
