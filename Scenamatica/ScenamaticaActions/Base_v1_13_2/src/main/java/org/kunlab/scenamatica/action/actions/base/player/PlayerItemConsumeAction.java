package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.enums.NMSHand;

import java.util.Collections;
import java.util.List;

@Action("player_item_consume")
@ActionDoc(
        name = "プレイヤによるアイテムの消費",
        description = "プレイヤの食料などを消費させます。",
        events = {
                PlayerItemConsumeEvent.class
        },

        executable = "プレイヤがアイテムを消費します。",
        watchable = "プレイヤがアイテムを消費することを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = PlayerItemConsumeAction.KEY_OUT_ITEM,
                        description = "消費されたアイテムです。",
                        type = ItemStack.class
                ),
                @OutputDoc(
                        name = PlayerItemConsumeAction.KEY_OUT_REPLACEMENT,
                        description = "消費されたアイテムの代替品です。",
                        type = ItemStack.class
                )
        },

        admonitions = {
                @Admonition(
                        type = AdmonitionType.DANGER,
                        content = "このアクションは、実行の完了までに、アイテムの消費にかかる時間分だけ掛かります。\n" +
                                "そのため、適切なタイムアウト（約 `20ticks * 2sec = 40ticks`）を設定する必要があります。"
                )
        }
)
public class PlayerItemConsumeAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    @InputDoc(
            name = "item",
            description = "消費するアイテムを指定します。",
            type = ItemStack.class
    )
    public static final InputToken<ItemStackStructure> IN_ITEM = ofInput(
            "item",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );
    @InputDoc(
            name = "replacement",
            description = "アイテムの消費後にプレイヤに残るアイテムを指定します。",
            type = ItemStack.class
    )
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
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);

        if (ctxt.hasInput(IN_ITEM))
            player.getInventory().setItemInMainHand(ctxt.input(IN_ITEM).create());

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isConsumable(item))
            throw new IllegalArgumentException("The item in the main hand is not consumable.");

        this.makeOutputs(ctxt, player, item, getReplacement(item));
        // 食べ初めをトリガするので、シナリオタイムアウトになるかもしれない。
        NMSProvider.getProvider().wrap(player).consume(NMSHand.MAIN_HAND);
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
