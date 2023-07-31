package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.action.utils.Utils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
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

public class EntityPlaceAction extends AbstractEntityAction<EntityPlaceAction.Argument>
        implements Watchable<EntityPlaceAction.Argument>
{
    public static final String KEY_ACTION_NAME = "entity_place";

    // armor stands, boats, minecarts, and end crystals. しか呼ばれないらしい
    @SuppressWarnings("deprecation")
    public static final Material[] PLACEABLE_ITEMS = {
            Material.MINECART,
            Material.CHEST_MINECART,
            Material.FURNACE_MINECART,
            Material.TNT_MINECART,
            Material.HOPPER_MINECART,
            Material.COMMAND_BLOCK_MINECART,

            Material.ARMOR_STAND,
            Material.LEGACY_ARMOR_STAND,

            Material.ACACIA_BOAT,
            Material.BIRCH_BOAT,
            Material.DARK_OAK_BOAT,
            Material.JUNGLE_BOAT,
            Material.OAK_BOAT,
            Material.LEGACY_BOAT,
            Material.LEGACY_BOAT_ACACIA,
            Material.LEGACY_BOAT_BIRCH,
            Material.LEGACY_BOAT_DARK_OAK,
            Material.LEGACY_BOAT_JUNGLE,
            Material.LEGACY_BOAT_SPRUCE,
            Material.SPRUCE_BOAT,

            Material.END_CRYSTAL,
            Material.LEGACY_END_CRYSTAL,
    };

    public static boolean isPlaceable(Material material)
    {
        for (Material m : PLACEABLE_ITEMS)
            if (m == material)
                return true;
        return false;
    }

    private static boolean isNotOnlyLocationAvailable(@Nullable BlockBean bean)
    {
        if (bean == null)
            return false;

        return bean.getType() != null
                || bean.getMetadata() != null
                // || bean.getLightLevel() != null  // LightLevel は関係ない。
                || bean.getBiome() != null;
    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);
        Location location = argument.getBlock().getLocation();
        Actor actor = PlayerUtils.getActorByStringOrThrow(engine, argument.getPlayerSpecifier());
        if (location.getWorld() == null)
            location.setWorld(actor.getPlayer().getWorld());  // Engine による推定はしない(Actor のワールド依存のアクションなため)

        if (argument.getBlockFace() != null)
            location = location.getBlock().getRelative(argument.getBlockFace()).getLocation();

        if (isNotOnlyLocationAvailable(argument.getBlock()))
            BeanUtils.applyBlockBeanData(argument.getBlock(), location, engine);

        Material material = Utils.searchMaterial(argument.getTargetString());
        if (material == null)
            throw new IllegalArgumentException("Material is not specified.");

        if (!isPlaceable(material))
            throw new IllegalArgumentException("Material is not placable.");

        actor.placeItem(location, new ItemStack(material), argument.getBlockFace());
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);
        if (type != ScenarioType.ACTION_EXECUTE)
            return;

        if (argument.getBlock() == null)
            throw new IllegalArgumentException("Block is not specified.");
        else if (argument.getBlock().getLocation() == null)
            throw new IllegalArgumentException("Block location is not specified.");

        if (argument.getPlayerSpecifier() == null)
            throw new IllegalArgumentException("Player specifier is not specified.");

        String entitySpecifier = argument.getTargetString();  // EXECUTE の場合は, 置く Material として扱う
        if (entitySpecifier == null)
            throw new IllegalArgumentException("Entity specifier is not specified.");

        for (Material material : PLACEABLE_ITEMS)
            if (material.name().equalsIgnoreCase(entitySpecifier) || material.name().equalsIgnoreCase("LEGACY_" + entitySpecifier))
                return;

        throw new IllegalArgumentException("Unknown entity type: " + entitySpecifier);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(argument, engine, event))
            return false;

        EntityPlaceEvent e = (EntityPlaceEvent) event;
        Player placer = e.getPlayer();
        BlockFace blockFace = e.getBlockFace();

        if (argument.getPlayerSpecifier() != null)
        {
            Player player = PlayerUtils.getPlayerOrThrow(argument.getPlayerSpecifier());
            if (!Objects.equals(placer, player))
                return false;
        }

        if (argument.getBlock() != null)
        {
            BlockBean block = argument.getBlock();
            if (!BeanUtils.isSame(block, e.getBlock(), engine))
                return false;
        }

        return argument.getBlockFace() == null || argument.getBlockFace() == blockFace;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityPlaceEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        BlockBean blockBean = null;
        if (map.containsKey(Argument.KEY_BLOCK))
            blockBean = serializer.deserializeBlock(
                    MapUtils.checkAndCastMap(
                            map.get(Argument.KEY_BLOCK),
                            String.class,
                            Object.class
                    )
            );

        return new Argument(
                super.deserializeTarget(map),
                MapUtils.getOrNull(map, Argument.KEY_PLAYER),
                blockBean,
                MapUtils.getAsEnumOrNull(map, Argument.KEY_BLOCK_FACE, BlockFace.class)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument
    {
        public static final String KEY_PLAYER = "player";
        public static final String KEY_BLOCK = "block";
        public static final String KEY_BLOCK_FACE = "direction";

        String playerSpecifier;
        BlockBean block;
        BlockFace blockFace;

        public Argument(@NotNull String target, String playerSpecifier, BlockBean block, BlockFace blockFace)
        {
            super(target);
            this.playerSpecifier = playerSpecifier;
            this.block = block;
            this.blockFace = blockFace;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(argument)
                    && Objects.equals(this.playerSpecifier, arg.playerSpecifier)
                    && Objects.equals(this.block, arg.block)
                    && this.blockFace == arg.blockFace;
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_PLAYER, this.playerSpecifier,
                    KEY_BLOCK, this.block,
                    KEY_BLOCK_FACE, this.blockFace
            );
        }
    }
}