package org.kunlab.scenamatica.action.actions.block;

import javax.annotation.ParametersAreNullableByDefault;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BlockBreakAction extends AbstractBlockAction<BlockBreakAction.Argument>
        implements Executable<BlockBreakAction.Argument>, Requireable<BlockBreakAction.Argument>, Watchable<BlockBreakAction.Argument>
{
    public static final String KEY_ACTION_NAME = "block_break";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        BlockBean blockDef = argument.getBlock();
        Location location = this.getBlockLocationWithWorld(blockDef, engine);
        Block block = location.getBlock();

        Player player = argument.getActor();
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
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedBlockEvent(argument, engine, event))
            return false;

        BlockBreakEvent e = (BlockBreakEvent) event;
        if (argument.getDropItems() != null)
        {
            boolean isDropItems = e.isDropItems();
            if (argument.getDropItems() != isDropItems)
                return false;
        }

        if (argument.getActor() != null)
        {
            Player player = argument.getActor();
            Player actualPlayer = e.getPlayer();

            return player.getUniqueId().equals(actualPlayer.getUniqueId());
        }

        return true;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                BlockBreakEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeBlockOrNull(map, serializer),
                map.containsKey(Argument.KEY_ACTOR) ? (String) map.get(Argument.KEY_ACTOR): null,
                MapUtils.getOrNull(map, Argument.KEY_DROP_ITEMS)
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        Location loc = this.getBlockLocationWithWorld(argument.getBlock(), engine);

        return loc.getBlock().getType() == Material.AIR;
    }

    @Value
    @ParametersAreNullableByDefault
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractBlockActionArgument
    {
        public static final String KEY_ACTOR = "actor";
        public static final String KEY_DROP_ITEMS = "drop_items";

        String actor;
        Boolean dropItems;

        public Argument(@NotNull BlockBean block, String actor, Boolean dropItems)
        {
            super(block);
            this.actor = actor;
            this.dropItems = dropItems;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(argument)
                    && Objects.equals(this.actor, arg.actor)
                    && Objects.equals(this.dropItems, arg.dropItems);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            switch (type)
            {
                case ACTION_EXECUTE:
                    throwIfNotPresent(Argument.KEY_BLOCK, this.block);
                    break;
                case CONDITION_REQUIRE:
                    throwIfPresent(Argument.KEY_ACTOR, this.actor);
                    throwIfPresent(Argument.KEY_DROP_ITEMS, this.dropItems);

                    BlockBean block = this.block;
                    throwIfPresent(Argument.KEY_BLOCK + "." + BlockBean.KEY_BLOCK_TYPE, block.getType());
                    throwIfPresent(Argument.KEY_BLOCK + "." + BlockBean.KEY_BIOME, block.getBiome());
                    throwIfPresent(Argument.KEY_BLOCK + "." + BlockBean.KEY_LIGHT_LEVEL, block.getLightLevel());

                    if (!block.getMetadata().isEmpty())
                        throw new IllegalArgumentException("The block metadata must be empty.");
            }

        }

        @Nullable
        public Player getActor()
        {
            if (this.actor == null)
                return null;

            return PlayerUtils.getPlayerOrNull(this.actor);
        }

        @Override
        public String getArgumentString()
        {
            return AbstractActionArgument.appendArgumentString(
                    super.getArgumentString(),
                    KEY_ACTOR, this.actor
            );
        }
    }
}
