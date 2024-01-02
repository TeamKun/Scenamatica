package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
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

    public static final String OUT_KEY_PLAYER = "player";
    public static final String OUT_KEY_BLOCK = "block";
    public static final String OUT_KEY_BLOCK_FACE = "direction";
    public static final String OUT_KEY_MATERIAL = "material";


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
        throw new IllegalArgumentException("EntityType" + entityType + " is not placeable.");
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
    public void execute(@NotNull ActionContext ctxt)
    {
        Location location = ctxt.input(IN_BLOCK).getLocation().create();
        Actor actor = ctxt.getActorOrThrow(ctxt.input(IN_PLAYER).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalArgumentException("Player is not specified."))
        );
        if (location.getWorld() == null)
        {
            location = location.clone();
            location.setWorld(actor.getPlayer().getWorld());  // Engine による推定はしない(Actor のワールド依存のアクションなため)
        }

        if (isNotOnlyLocationAvailable(ctxt.input(IN_BLOCK)))
            ctxt.input(IN_BLOCK).applyTo(location.getBlock());

        Material material = ctxt.input(IN_MATERIAL);

        if (!isPlaceable(material))
            throw new IllegalArgumentException("Material is not placable.");

        this.makeOutputs(ctxt, null, actor.getPlayer(), location.getBlock(), ctxt.input(IN_BLOCK_FACE), material);
        actor.placeItem(location, new ItemStack(material), ctxt.input(IN_BLOCK_FACE));
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(ctxt, event))
            return false;

        EntityPlaceEvent e = (EntityPlaceEvent) event;
        BlockFace blockFace = e.getBlockFace();
        Player placer = e.getPlayer();
        Block block = e.getBlock();
        EntityType entityType = e.getEntityType();

        boolean result = ctxt.ifHasInput(IN_PLAYER, player -> player.checkMatchedPlayer(placer))
                && ctxt.ifHasInput(IN_BLOCK_FACE, face -> face == blockFace)
                && ctxt.ifHasInput(IN_MATERIAL, material -> material == toMaterial(entityType))
                && ctxt.ifHasInput(IN_BLOCK, blockStructure -> blockStructure.isAdequate(block));
        if (result)
            this.makeOutputs(ctxt, e.getEntity(), placer, block, blockFace, toMaterial(entityType));

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @Nullable Entity entity, @Nullable Player player, @NotNull Block block, @Nullable BlockFace blockFace, @NotNull Material material)
    {
        if (player != null)
            ctxt.output(OUT_KEY_PLAYER, player);
        ctxt.output(OUT_KEY_BLOCK, ctxt.getSerializer().toStructure(block, BlockStructure.class));
        if (blockFace != null)
            ctxt.output(OUT_KEY_BLOCK_FACE, blockFace);
        ctxt.output(OUT_KEY_MATERIAL, material);

        super.makeOutputs(ctxt, entity);
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
