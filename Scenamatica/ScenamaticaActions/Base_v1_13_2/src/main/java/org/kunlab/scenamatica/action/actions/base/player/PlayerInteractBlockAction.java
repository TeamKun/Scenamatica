package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;

import java.util.Collections;
import java.util.List;

@Action("player_interact_block")
@ActionDoc(
        name = "プレイヤによるブロックの操作",
        description = "プレイヤがブロックをクリックします。",
        events = {
                PlayerInteractEvent.class
        },

        executable = "プレイヤがブロックをクリックします。",
        expectable = "プレイヤがブロックをクリックすることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = PlayerInteractBlockAction.KEY_OUT_ACTION,
                        description = "クリックされたブロックです。",
                        type = Block.class
                ),
                @OutputDoc(
                        name = PlayerInteractBlockAction.KEY_OUT_BLOCK,
                        description = "クリックされたブロックです。",
                        type = Block.class
                ),
                @OutputDoc(
                        name = PlayerInteractBlockAction.KEY_OUT_BLOCK_FACE,
                        description = "クリックされたブロックの面です。",
                        type = BlockFace.class
                ),
                @OutputDoc(
                        name = PlayerInteractBlockAction.KEY_OUT_HAND,
                        description = "クリックをした手です。",
                        type = EquipmentSlot.class
                )
        }

)
public class PlayerInteractBlockAction extends AbstractPlayerAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "action",
            description = "プレイヤの行動を指定します。",
            type = org.bukkit.event.block.Action.class
    )
    public static final InputToken<org.bukkit.event.block.Action> IN_ACTION = ofEnumInput(
            "action",
            org.bukkit.event.block.Action.class
    );

    @InputDoc(
            name = "hand",
            description = "クリックをする手を指定します。",
            type = EquipmentSlot.class
    )
    public static final InputToken<EquipmentSlot> IN_HAND = ofEnumInput(
            "hand",
            EquipmentSlot.class
    ).validator(
            (slot) -> slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND,
            "The hand must be either hand or off hand"
    );

    @InputDoc(
            name = "block",
            description = "クリックするブロックを指定します。",
            type = BlockStructure.class
    )
    public static final InputToken<BlockStructure> IN_BLOCK = ofInput(
            "block",
            BlockStructure.class,
            ofDeserializer(BlockStructure.class)
    );

    @InputDoc(
            name = "blockFace",
            description = "クリックするブロックの面を指定します。",
            type = BlockFace.class
    )
    public static final InputToken<BlockFace> IN_BLOCK_FACE = ofEnumInput(
            "blockFace",
            BlockFace.class
    );
    public static final String KEY_OUT_ACTION = "action";
    public static final String KEY_OUT_BLOCK = "block";
    public static final String KEY_OUT_BLOCK_FACE = "blockFace";
    public static final String KEY_OUT_HAND = "hand";

    private static Block getClickBlock(ActionContext ctxt)
    {
        Location clickPos;
        if (ctxt.hasInput(IN_BLOCK) && ctxt.ifHasInput(IN_BLOCK, b -> b.getLocation() != null, false))  // 指定があったらその位置をクリックする
            clickPos = ctxt.input(IN_BLOCK).getLocation().create();
        else  // 指定がなかったら自身の位置をクリックする(しかない)
            clickPos = selectTarget(ctxt).getLocation().toBlockLocation();

        Location normalizedPos = Utils.assignWorldToLocation(clickPos, ctxt.getEngine());
        return normalizedPos.getWorld().getBlockAt(normalizedPos);
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        org.bukkit.event.block.Action action = ctxt.input(IN_ACTION);
        Player player = selectTarget(ctxt);

        // 引数の検証を行う( validateArgument() はランタイムではないのでこちら側でやるしかない。)
        Block clickBlock = getClickBlock(ctxt);
        boolean isAir = clickBlock.getType() == Material.AIR;
        if ((action == org.bukkit.event.block.Action.LEFT_CLICK_AIR || action == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) && !isAir)
            throw new IllegalArgumentException("Argument action is not allowed to be LEFT_CLICK_AIR or RIGHT_CLICK_AIR when the target block is not air");
        else if ((action == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK || action == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) && isAir)
            throw new IllegalArgumentException("Argument action is not allowed to be LEFT_CLICK_BLOCK or RIGHT_CLICK_BLOCK when the target block is air");

        this.makeOutputs(ctxt, player, clickBlock, null, EquipmentSlot.HAND);
        ctxt.getActorOrThrow(player)
                .interactAt(
                        action,
                        getClickBlock(ctxt)
                );
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerInteractEvent;
        PlayerInteractEvent e = (PlayerInteractEvent) event;

        boolean result = ctxt.ifHasInput(IN_ACTION, action -> action == e.getAction())
                && ctxt.ifHasInput(IN_HAND, hand -> hand == e.getHand())
                && ctxt.ifHasInput(IN_BLOCK, block -> block.isAdequate(e.getClickedBlock()))
                && ctxt.ifHasInput(IN_BLOCK_FACE, face -> face == e.getBlockFace());
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getClickedBlock(), e.getBlockFace(), e.getHand());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @Nullable Block block, @Nullable BlockFace face, @Nullable EquipmentSlot hand)
    {
        ctxt.output(KEY_OUT_ACTION, block);
        if (block != null)
            ctxt.output(KEY_OUT_BLOCK, block);
        if (face != null)
            ctxt.output(KEY_OUT_BLOCK_FACE, face);
        if (hand != null)
            ctxt.output(KEY_OUT_HAND, hand);

        super.makeOutputs(ctxt, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerInteractEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_ACTION, IN_HAND, IN_BLOCK);

        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_ACTION);
        else
            board.register(IN_BLOCK_FACE);

        return board;
    }
}
