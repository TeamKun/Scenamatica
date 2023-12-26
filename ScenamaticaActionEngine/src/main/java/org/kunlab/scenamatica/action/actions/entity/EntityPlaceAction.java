package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")
public class EntityPlaceAction extends AbstractGeneralEntityAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "entity_place";

    // armor stands, boats, minecarts, and end crystals. しか呼ばれないらしい
    public static final Map<Material, EntityType> PLACEABLE_ENTITIES_MAP;

    public static final InputToken<PlayerSpecifier> IN_PLAYER = ofInput(
            "player",
            PlayerSpecifier.class,
            ofPlayer()
    );
    public static final InputToken<BlockStructure> IN_BLOCK = ofInput(
            "block",
            BlockStructure.class,
            ofDeserializer(BlockStructure.class)
    )
            .validator(
                    ScenarioType.ACTION_EXECUTE,
                    block -> block.getLocation() != null, "Block location is not specified."
            );
    public static final InputToken<BlockFace> IN_BLOCK_FACE = ofInput(
            "direction",
            BlockFace.class,
            ofEnum(BlockFace.class)
    );
    public static final InputToken<Material> IN_MATERIAL = ofInput(
            "material",
            Material.class,
            ofEnum(Material.class)
    );

    static
    {
        Map<Material, EntityType> map = new EnumMap<>(Material.class);
        // <editor-fold desc="PLACEABLE_ENTITIES_MAP">
        map.put(Material.MINECART, EntityType.MINECART);
        map.put(Material.CHEST_MINECART, EntityType.MINECART_CHEST);
        map.put(Material.FURNACE_MINECART, EntityType.MINECART_FURNACE);
        map.put(Material.TNT_MINECART, EntityType.MINECART_TNT);
        map.put(Material.HOPPER_MINECART, EntityType.MINECART_HOPPER);
        map.put(Material.COMMAND_BLOCK_MINECART, EntityType.MINECART_COMMAND);

        map.put(Material.ARMOR_STAND, EntityType.ARMOR_STAND);
        map.put(Material.LEGACY_ARMOR_STAND, EntityType.ARMOR_STAND);

        map.put(Material.ACACIA_BOAT, EntityType.BOAT);
        map.put(Material.BIRCH_BOAT, EntityType.BOAT);
        map.put(Material.DARK_OAK_BOAT, EntityType.BOAT);
        map.put(Material.JUNGLE_BOAT, EntityType.BOAT);
        map.put(Material.OAK_BOAT, EntityType.BOAT);
        map.put(Material.LEGACY_BOAT, EntityType.BOAT);
        map.put(Material.LEGACY_BOAT_ACACIA, EntityType.BOAT);
        map.put(Material.LEGACY_BOAT_BIRCH, EntityType.BOAT);
        map.put(Material.LEGACY_BOAT_DARK_OAK, EntityType.BOAT);
        map.put(Material.LEGACY_BOAT_JUNGLE, EntityType.BOAT);
        map.put(Material.LEGACY_BOAT_SPRUCE, EntityType.BOAT);
        map.put(Material.SPRUCE_BOAT, EntityType.BOAT);

        map.put(Material.END_CRYSTAL, EntityType.ENDER_CRYSTAL);
        map.put(Material.LEGACY_END_CRYSTAL, EntityType.ENDER_CRYSTAL);
        // </editor-fold>
        PLACEABLE_ENTITIES_MAP = Collections.unmodifiableMap(map);
    }

    public static boolean isPlaceable(Material material)
    {
        for (Material m : PLACEABLE_ENTITIES_MAP.keySet())
            if (m == material)
                return true;
        return false;
    }

    public static EntityType toEntityType(Material material)
    {
        return PLACEABLE_ENTITIES_MAP.get(material);
    }

    public static Material toMaterial(EntityType entityType)
    {
        for (Map.Entry<Material, EntityType> entry : PLACEABLE_ENTITIES_MAP.entrySet())
            if (entry.getValue() == entityType)
                return entry.getKey();
        return null;
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
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Location location = argument.get(IN_BLOCK).getLocation().create();
        Actor actor = PlayerUtils.getActorOrThrow(engine, argument.get(IN_PLAYER).selectTarget(engine.getContext())
                .orElseThrow(() -> new IllegalArgumentException("Player is not specified.")));
        if (location.getWorld() == null)
        {
            location = location.clone();
            location.setWorld(actor.getPlayer().getWorld());  // Engine による推定はしない(Actor のワールド依存のアクションなため)
        }

        if (isNotOnlyLocationAvailable(argument.get(IN_BLOCK)))
            argument.get(IN_BLOCK).applyTo(location.getBlock());

        Material material = argument.get(IN_MATERIAL);

        if (!isPlaceable(material))
            throw new IllegalArgumentException("Material is not placable.");

        actor.placeItem(location, new ItemStack(material), argument.get(IN_BLOCK_FACE));
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(argument, engine, event))
            return false;

        EntityPlaceEvent e = (EntityPlaceEvent) event;
        BlockFace blockFace = e.getBlockFace();
        Player placer = e.getPlayer();
        Block block = e.getBlock();
        EntityType entityType = e.getEntityType();

        return argument.ifPresent(IN_PLAYER, player -> player.checkMatchedPlayer(placer))
                && argument.ifPresent(IN_BLOCK_FACE, face -> face == blockFace)
                && argument.ifPresent(IN_MATERIAL, material -> material == toMaterial(entityType))
                && argument.ifPresent(IN_BLOCK, blockStructure -> blockStructure.isAdequate(block));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityPlaceEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, this.IN_TARGET_ENTITY, IN_PLAYER, IN_BLOCK, IN_BLOCK_FACE, IN_MATERIAL);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_PLAYER, IN_BLOCK, IN_BLOCK_FACE, IN_MATERIAL);

        return board;
    }
}
