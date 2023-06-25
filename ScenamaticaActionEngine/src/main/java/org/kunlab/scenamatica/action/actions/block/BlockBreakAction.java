package org.kunlab.scenamatica.action.actions.block;

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
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.Requireable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BlockBreakAction extends AbstractBlockAction<BlockBreakAction.Argument> implements Requireable<BlockBreakAction.Argument>
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
        if (!super.isFired(argument, engine, event))
            return false;

        BlockBreakEvent e = (BlockBreakEvent) event;
        Block block = e.getBlock();

        if (!BeanUtils.isSame(argument.getBlock(), block, engine))
            return false;

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
                super.deserializeBlock(map, serializer),
                map.containsKey(Argument.KEY_ACTOR) ? (String) map.get(Argument.KEY_ACTOR): null,
                MapUtils.getOrNull(map, Argument.KEY_DROP_ITEMS)
        );
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        if (type != ScenarioType.CONDITION_REQUIRE)
            return;

        this.throwIfPresent(Argument.KEY_ACTOR, argument.getActor());
        this.throwIfPresent(Argument.KEY_DROP_ITEMS, argument.getDropItems());

        BlockBean block = argument.getBlock();
        this.throwIfPresent(Argument.KEY_BLOCK + "." + BlockBean.KEY_BLOCK_TYPE, block.getType());
        this.throwIfPresent(Argument.KEY_BLOCK + "." + BlockBean.KEY_BIOME, block.getBiome());
        this.throwIfNotEquals(Argument.KEY_BLOCK + "." + BlockBean.KEY_LIGHT_LEVEL, block.getLightLevel(), 0);

        if (!block.getMetadata().isEmpty())
            throw new IllegalArgumentException("The block metadata must be empty.");
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        Location loc = this.getBlockLocationWithWorld(argument.getBlock(), engine);

        return loc.getBlock().getType() == Material.AIR;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractBlockActionArgument
    {
        public static final String KEY_ACTOR = "actor";
        public static final String KEY_DROP_ITEMS = "drop_items";

        @Nullable
        String actor;
        @Nullable
        Boolean dropItems;

        public Argument(@NotNull BlockBean block, @Nullable String actor, @Nullable Boolean dropItems)
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
