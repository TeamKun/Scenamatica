package org.kunlab.scenamatica.action.actions.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
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

import java.util.Collections;
import java.util.List;

public class BlockBreakAction extends AbstractBlockAction
        implements Executable, Requireable, Watchable
{
    public static final String KEY_ACTION_NAME = "block_break";

    public static final InputToken<PlayerSpecifier> IN_ACTOR = ofInput("actor", PlayerSpecifier.class, ofPlayer());
    public static final InputToken<Boolean> IN_DROP_ITEMS = ofInput("drop_items", Boolean.class);

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        BlockStructure blockDef = argument.get(IN_BLOCK);
        Location location = this.getBlockLocationWithWorld(blockDef, engine);
        Block block = location.getBlock();

        Player player = argument.get(IN_ACTOR).selectTarget(engine.getContext()).orElse(null);
        if (player == null)
        {
            block.breakNaturally();  // 自然に壊れたことにする
            return;
        }

        this.validateBreakable(block, player);

        Actor actor = PlayerUtils.getActorOrThrow(engine, player); // アクタ以外は破壊シミュレートできない。
        actor.breakBlock(block);
    }

    private void validateBreakable(@NotNull Block block, @NotNull Player player)
    {
        World world = block.getWorld();
        World playerWorld = player.getWorld();

        if (!world.getKey().equals(playerWorld.getKey()))
            throw new IllegalArgumentException("The block and the player must be in the same world.");
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedBlockEvent(argument, engine, event))
            return false;

        BlockBreakEvent e = (BlockBreakEvent) event;

        return argument.ifPresent(IN_ACTOR, player -> player.checkMatchedPlayer(e.getPlayer()))
                && argument.ifPresent(IN_DROP_ITEMS, dropItems -> dropItems == e.isDropItems());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                BlockBreakEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(@NotNull ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_ACTOR);

        switch (type)
        {
            case ACTION_EXECUTE:
            case ACTION_EXPECT:
                board.register(IN_DROP_ITEMS);
                break;
            case CONDITION_REQUIRE:
                board.validator(
                                b -> b.ifPresent(IN_BLOCK, block -> block.getType() == null),
                                "The block must be specified in the condition requiring mode."
                        )
                        .validator(
                                b -> b.ifPresent(IN_BLOCK, block -> block.getBiome() == null),
                                "Cannot specify the biome in the condition requiring mode."
                        )
                        .validator(
                                b -> b.ifPresent(IN_BLOCK, block -> block.getLightLevel() == null),
                                "Cannot specify the light level in the condition requiring mode."
                        );
                break;
        }

        return board;
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        Location loc = this.getBlockLocationWithWorld(argument.get(IN_BLOCK), engine);

        return loc.getBlock().getType() == Material.AIR;
    }
}
