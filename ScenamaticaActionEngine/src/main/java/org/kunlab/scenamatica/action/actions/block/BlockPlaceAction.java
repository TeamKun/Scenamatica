package org.kunlab.scenamatica.action.actions.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.nms.enums.NMSHand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BlockPlaceAction extends AbstractBlockAction
        implements Executable, Requireable, Watchable
{
    public static final String KEY_ACTION_NAME = "block_place";
    public static final InputToken<PlayerSpecifier> IN_ACTOR = ofInput("actor", PlayerSpecifier.class, ofPlayer());
    public static final InputToken<NMSHand> IN_HAND = ofEnumInput("hand", NMSHand.class);

    private static final BlockFace[] ALLOWED_FACES = {
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };
    public static final InputToken<BlockFace> IN_DIRECTION = ofEnumInput("direction", BlockFace.class)
            .validator(
                    face -> Arrays.stream(ALLOWED_FACES).anyMatch(f -> f == face),
                    "Invalid direction: %s, allowed: " + Arrays.toString(ALLOWED_FACES)
            )
            .defaultValue(BlockFace.EAST);

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        BlockStructure blockDef = ctxt.input(IN_BLOCK);
        Location location = this.getBlockLocationWithWorld(blockDef, ctxt);

        Block block = location.getBlock();
        if (ctxt.hasInput(IN_ACTOR))
        {
            BlockFace direction = ctxt.orElseInput(IN_DIRECTION, () -> BlockFace.EAST);
            NMSHand hand = ctxt.orElseInput(IN_HAND, () -> NMSHand.MAIN_HAND);

            Player player = ctxt.input(IN_ACTOR).selectTarget(ctxt.getContext())
                    .orElseThrow(() -> new IllegalStateException("Cannot find player"));
            Actor scenarioActor = ctxt.getActorOrThrow(player);
            this.makeOutputs(ctxt, location.getBlock(), player);
            scenarioActor.placeBlock(
                    location,
                    new ItemStack(blockDef.getType()),  // assert blockDef.getType() != null
                    hand,
                    direction
            );
            if (this.shouldFirePlaceEvent(block, blockDef.getType()))
                this.firePlaceEvent(block, player, hand, direction);
        }
        else
            this.makeOutputs(ctxt, location.getBlock(), null);

        blockDef.applyTo(block);
    }

    private boolean shouldFirePlaceEvent(Block at, Material typeToPlace)
    {
        return at.getType() == typeToPlace;
    }

    private void firePlaceEvent(Block block, Player player, NMSHand hand, BlockFace against)
    {
        BlockPlaceEvent event = new BlockPlaceEvent(
                block,
                block.getState(),
                block.getRelative(against),
                new ItemStack(block.getType()),
                player,
                block.isBuildable(),
                hand.toEquipmentSlot()
        );
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedBlockEvent(ctxt, event))
            return false;

        assert event instanceof BlockPlaceEvent;
        BlockPlaceEvent e = (BlockPlaceEvent) event;

        return ctxt.ifHasInput(IN_ACTOR, actor -> actor.checkMatchedPlayer(e.getPlayer()))
                && ctxt.ifHasInput(IN_HAND, hand -> hand == NMSHand.fromEquipmentSlot(e.getHand()))
                && ctxt.ifHasInput(IN_BLOCK, block -> block.isAdequate(e.getBlockPlaced()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                BlockPlaceEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        BlockStructure blockDef = ctxt.input(IN_BLOCK);
        Block block = this.getBlockLocationWithWorld(blockDef, ctxt).getBlock();

        return blockDef.isAdequate(block);
    }

    @Override
    public InputBoard getInputBoard(@NotNull ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_ACTOR, IN_HAND, IN_DIRECTION);

        switch (type)
        {
            case CONDITION_REQUIRE:
                board.requirePresent(IN_BLOCK)
                        .validator(
                                b -> !b.isPresent(IN_ACTOR),
                                "Cannot specify the actor in the condition requiring mode."
                        );
                /* fall through */
            case ACTION_EXECUTE:
                board.validator(
                        b -> b.ifPresent(IN_BLOCK, block -> block.getType() != null),
                        "Block type cannot be null"
                );
                break;
        }

        return board;
    }
}
