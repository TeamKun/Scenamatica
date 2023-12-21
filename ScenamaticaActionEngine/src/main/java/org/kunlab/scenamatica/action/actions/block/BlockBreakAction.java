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
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
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

        BlockStructure blockDef = argument.getBlock();
        Location location = this.getBlockLocationWithWorld(blockDef, engine);
        Block block = location.getBlock();

        Player player = argument.getActorSpecifier().selectTarget(engine.getContext())
                .orElse(null);
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

        return (!argument.getActorSpecifier().canProvideTarget() || argument.getActorSpecifier().checkMatchedPlayer(e.getPlayer()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                BlockBreakEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(
                super.deserializeBlockOrNull(map, serializer),
                serializer.tryDeserializePlayerSpecifier(map.get(Argument.KEY_ACTOR)),
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

        @NotNull
        PlayerSpecifier actorSpecifier;
        Boolean dropItems;

        public Argument(@Nullable BlockStructure block, @NotNull PlayerSpecifier actorSpecifier, Boolean dropItems)
        {
            super(block);
            this.actorSpecifier = actorSpecifier;
            this.dropItems = dropItems;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(argument)
                    && Objects.equals(this.actorSpecifier, arg.actorSpecifier)
                    && Objects.equals(this.dropItems, arg.dropItems);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            switch (type)
            {
                case ACTION_EXECUTE:
                    ensurePresent(Argument.KEY_BLOCK, this.block);
                    break;
                case CONDITION_REQUIRE:
                    if (this.actorSpecifier.canProvideTarget())
                        throw new IllegalArgumentException("Cannot specify the actor in the condition requiring mode.");
                    ensureNotPresent(Argument.KEY_DROP_ITEMS, this.dropItems);

                    BlockStructure block = this.block;
                    ensureNotPresent(Argument.KEY_BLOCK + "." + BlockStructure.KEY_BLOCK_TYPE, block.getType());
                    ensureNotPresent(Argument.KEY_BLOCK + "." + BlockStructure.KEY_BIOME, block.getBiome());
                    ensureNotPresent(Argument.KEY_BLOCK + "." + BlockStructure.KEY_LIGHT_LEVEL, block.getLightLevel());

                    if (!block.getMetadata().isEmpty())
                        throw new IllegalArgumentException("The block metadata must be empty.");
            }

        }

        @Override
        public String getArgumentString()
        {
            return AbstractActionArgument.appendArgumentString(
                    super.getArgumentString(),
                    KEY_ACTOR, this.actorSpecifier
            );
        }
    }
}
