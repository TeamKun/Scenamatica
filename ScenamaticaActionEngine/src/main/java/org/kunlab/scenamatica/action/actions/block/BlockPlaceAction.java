package org.kunlab.scenamatica.action.actions.block;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
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
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BlockPlaceAction extends AbstractBlockAction
        implements Executable, Requireable, Watchable
{
    public static final String KEY_ACTION_NAME = "block_place";
    public static final InputToken<PlayerSpecifier> IN_ACTOR = ofInput("actor", PlayerSpecifier.class, ofPlayer());
    public static final InputToken<EquipmentSlot> IN_HAND = ofEnumInput("hand", EquipmentSlot.class)
            .validator(
                    hand -> hand == EquipmentSlot.HAND || hand == EquipmentSlot.OFF_HAND,
                    "Invalid hand: %s, allowed: " + EquipmentSlot.HAND + ", " + EquipmentSlot.OFF_HAND
            );

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
                    face -> Arrays.stream(ALLOWED_FACES).parallel().anyMatch(f -> f == face),
                    "Invalid direction: %s, allowed: " + Arrays.toString(ALLOWED_FACES)
            )
            .defaultValue(BlockFace.EAST);

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Player actor = argument.get(IN_ACTOR).selectTarget(engine.getContext()).orElse(null);

        BlockStructure blockDef = argument.get(IN_BLOCK);
        Location location = this.getBlockLocationWithWorld(blockDef, engine);

        Block block;
        if (actor == null)
        {
            block = location.getBlock();
            block.setType(blockDef.getType());
        }
        else
        {
            BlockFace direction = argument.get(IN_DIRECTION);
            EquipmentSlot hand = argument.has(IN_HAND) ? argument.get(IN_HAND): EquipmentSlot.HAND;

            Actor scenarioActor = PlayerUtils.getActorOrThrow(engine, actor);
            scenarioActor.placeBlock(
                    location,
                    new ItemStack(blockDef.getType()),  // assert blockDef.getType() != null
                    hand,
                    direction
            );

            block = location.getBlock();  // 更新の必要があるため、共通化できない。
        }

        if (blockDef.getBiome() != null)
            block.setBiome(blockDef.getBiome());
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedBlockEvent(argument, engine, event))
            return false;

        assert event instanceof BlockPlaceEvent;
        BlockPlaceEvent e = (BlockPlaceEvent) event;

        return argument.ifPresent(IN_ACTOR, actor -> actor.checkMatchedPlayer(e.getPlayer()))
                && argument.ifPresent(IN_HAND, hand -> hand == e.getHand())
                && argument.ifPresent(IN_BLOCK, block -> this.isConditionFulfilled(argument, engine));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                BlockPlaceEvent.class
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        BlockStructure blockDef = argument.get(IN_BLOCK);
        Block block = this.getBlockLocationWithWorld(blockDef, engine).getBlock();

        return blockDef.isAdequate(block);
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = this.createBaseInput(type);

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
