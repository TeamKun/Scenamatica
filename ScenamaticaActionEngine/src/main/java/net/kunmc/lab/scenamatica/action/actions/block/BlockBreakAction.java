package net.kunmc.lab.scenamatica.action.actions.block;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.action.utils.BeanUtils;
import net.kunmc.lab.scenamatica.action.utils.EntityUtils;
import net.kunmc.lab.scenamatica.action.utils.PlayerUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.context.Actor;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.BeanSerializer;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BlockBreakAction extends AbstractBlockAction<BlockBreakAction.Argument>
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
        Location location = blockDef.getLocation().clone();
        this.setWorldIfNull(location, engine.getContext().getStage());

        Block block = location.getBlock();

        Player player = argument.getActor();
        if (player == null)
        {
            block.breakNaturally();  // 自然に壊れたことにする
            return;
        }

        this.validateBreakable(block, player);

        Actor actor = EntityUtils.getActorOrThrow(engine, player); // アクタ以外は破壊シミュレートできない。
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

    private void setWorldIfNull(@NotNull Location location, @NotNull World world)
    {
        if (location.getWorld() != null)
            return;

        location.setWorld(world);
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
            StringBuilder builder = new StringBuilder(super.getArgumentString());

            if (this.actor != null)
                builder.append(", ").append(KEY_ACTOR).append("=").append(this.actor);

            return builder.toString();
        }
    }
}
