package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.kunlab.scenamatica.selector.compiler.SelectorCompilationErrorException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntityPlaceAction extends AbstractEntityAction<EntityPlaceAction.Argument>
        implements Executable<EntityPlaceAction.Argument>, Watchable<EntityPlaceAction.Argument>
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

    private static boolean isNotOnlyLocationAvailable(@Nullable BlockStructure structure)
    {
        if (structure == null)
            return false;

        return structure.getType() != null
                || structure.getMetadata().isEmpty()
                // || structure.getLightLevel() != null  // LightLevel は関係ない。
                || structure.getBiome() != null;
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
        Location location = argument.getBlock().getLocation().create();
        Actor actor = PlayerUtils.getActorOrThrow(engine, argument.getPlayerSpecifier().selectTarget(engine.getContext())
                .orElseThrow(() -> new IllegalArgumentException("Player is not specified.")));
        if (location.getWorld() == null)
        {
            location = location.clone();
            location.setWorld(actor.getPlayer().getWorld());  // Engine による推定はしない(Actor のワールド依存のアクションなため)
        }

        if (isNotOnlyLocationAvailable(argument.getBlock()))
            argument.getBlock().applyTo(location.getBlock());

        Material material = argument.getMaterialToPlace();

        if (!isPlaceable(material))
            throw new IllegalArgumentException("Material is not placable.");

        actor.placeItem(location, new ItemStack(material), argument.getBlockFace());
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(argument, engine, event))
            return false;

        EntityPlaceEvent e = (EntityPlaceEvent) event;
        BlockFace blockFace = e.getBlockFace();

        if (argument.getBlock() != null)
        {
            BlockStructure block = argument.getBlock();
            if (!block.isAdequate(e.getBlock()))
                return false;
        }

        Player placer = e.getPlayer();
        return (argument.getBlockFace() == null || argument.getBlockFace() == blockFace)
                && (argument.getPlayerSpecifier() == null || argument.getPlayerSpecifier().checkMatchedPlayer(placer))
                && (argument.getMaterialToPlace() == null || argument.getMaterialToPlace() == e.getBlock().getType());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityPlaceEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        BlockStructure blockStructure = null;
        if (map.containsKey(Argument.KEY_BLOCK))
            blockStructure = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(Argument.KEY_BLOCK)),
                    BlockStructure.class
            );

        EntitySpecifier<Entity> target = target = serializer.tryDeserializeEntitySpecifier(null);
        Material materialToPlace = null;
        if (map.containsKey(AbstractEntityActionArgument.KEY_TARGET_ENTITY))
        {
            try
            {
                target = super.deserializeTarget(map, serializer);
            }
            catch (SelectorCompilationErrorException ignored)
            {
                String materialName = (String) map.get(Argument.KEY_TARGET_ENTITY);
                materialToPlace = Utils.searchMaterial(materialName);
            }
        }

        return new Argument(
                target,
                serializer.tryDeserializePlayerSpecifier(map.get(Argument.KEY_PLAYER)),
                blockStructure,
                MapUtils.getAsEnumOrNull(map, Argument.KEY_BLOCK_FACE, BlockFace.class),
                materialToPlace
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument<Entity>
    {
        public static final String KEY_PLAYER = "player";
        public static final String KEY_BLOCK = "block";
        public static final String KEY_BLOCK_FACE = "direction";

        PlayerSpecifier playerSpecifier;
        BlockStructure block;
        BlockFace blockFace;

        Material materialToPlace;

        public Argument(EntitySpecifier<Entity> target, PlayerSpecifier playerSpecifier, BlockStructure block, BlockFace blockFace, Material materialToPlace)
        {
            super(target);
            this.playerSpecifier = playerSpecifier;
            this.block = block;
            this.blockFace = blockFace;
            this.materialToPlace = materialToPlace;
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
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type != ScenarioType.ACTION_EXECUTE)
                return;

            ensurePresent(KEY_BLOCK, this.block);
            if (this.block.getLocation() == null)
                throw new IllegalArgumentException("Block location is not specified.");

            ensurePresent(KEY_PLAYER, this.playerSpecifier);
            ensurePresent(KEY_BLOCK_FACE, this.blockFace);

            ensurePresent(KEY_TARGET_ENTITY, this.materialToPlace);
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
