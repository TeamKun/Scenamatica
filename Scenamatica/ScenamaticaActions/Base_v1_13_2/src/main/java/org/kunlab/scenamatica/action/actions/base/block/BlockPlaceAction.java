package org.kunlab.scenamatica.action.actions.base.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.nms.enums.NMSHand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Action("block_place")
@ActionDoc(
        name = "ブロックの設置",
        description = "指定されたブロックを設置します。",
        events = BlockPlaceEvent.class,

        executable = "指定されたブロックを設置します。",
        expectable = "指定されたブロックが設置されることを期待します。",
        requireable = "指定されたブロックが指定されたものであることを検証します。",

        admonitions = {
                @Admonition(
                        type = AdmonitionType.WARNING,
                        content = "指定するブロックの種類は空にできません。\n" +
                                "また, Minecraft 1.14.4.x においては, 手に持っているブロックの種類と上記の値が異なる場合は自動的に手に持っているアイテムを置換します。",
                        on = ActionMethod.EXECUTE
                )
        }
)
public class BlockPlaceAction extends AbstractBlockAction
        implements Executable, Requireable, Expectable
{
    @InputDoc(
            name = "actor",
            description = "ブロックを設置するアクタです。",
            type = PlayerSpecifier.class,

            availableFor = {ActionMethod.EXECUTE, ActionMethod.EXPECT}
    )
    public static final InputToken<PlayerSpecifier> IN_ACTOR = ofInput("actor", PlayerSpecifier.class, ofPlayer());

    @InputDoc(
            name = "hand",
            description = "設置するブロックのアイテムを持っている手です。",
            type = NMSHand.class,
            availableFor = {ActionMethod.EXECUTE}
    )
    public static final InputToken<NMSHand> IN_HAND = ofEnumInput("hand", NMSHand.class);

    private static final BlockFace[] ALLOWED_FACES = {
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    @InputDoc(
            name = "direction",
            description = "設置するブロックの向きです。",
            type = BlockFace.class,
            availableFor = {ActionMethod.EXECUTE}
    )
    public static final InputToken<BlockFace> IN_DIRECTION = ofEnumInput("direction", BlockFace.class)
            .validator(
                    face -> Arrays.stream(ALLOWED_FACES).anyMatch(f -> f == face),
                    "Invalid direction: %s, allowed: " + Arrays.toString(ALLOWED_FACES)
            )
            .defaultValue(BlockFace.EAST);

    private static boolean canBuild(Player player, Location location)
    {
        World world = location.getWorld();
        int spawnRadius = Bukkit.getServer().getSpawnRadius();
        if (world.getEnvironment() != World.Environment.NORMAL)
            return true;
        else if (spawnRadius <= 0)
            return true;
        else if (Bukkit.getOperators().isEmpty())
            return true;
        else if (player.isOp())
            return true;

        Location spawn = world.getSpawnLocation();
        int distance = Math.max(Math.abs(location.getBlockX() - spawn.getBlockX()), Math.abs(location.getBlockZ() - spawn.getBlockZ()));
        return distance > spawnRadius;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        BlockStructure blockDef = ctxt.input(IN_BLOCK);
        Location location = this.getBlockLocationWithWorld(blockDef, ctxt);

        Block block = location.getBlock();
        if (ctxt.hasInput(IN_ACTOR))
        {
            BlockFace direction = ctxt.orElseInput(IN_DIRECTION, () -> BlockFace.UP);
            NMSHand hand = ctxt.orElseInput(IN_HAND, () -> NMSHand.MAIN_HAND);

            Player player = ctxt.input(IN_ACTOR).selectTarget(ctxt.getContext())
                    .orElseThrow(() -> new IllegalStateException("Cannot find player"));
            Actor scenarioActor = ctxt.getActorOrThrow(player);
            this.makeOutputs(ctxt, location.getBlock(), player);
            if (MinecraftVersion.current().isInRange(MinecraftVersion.V1_14, MinecraftVersion.V1_15_2) &&
                    player.getInventory().getItemInMainHand().getType() != blockDef.getType())// 1.14.4でのエラー回避
                player.getInventory().setItemInMainHand(new ItemStack(blockDef.getType()));

            scenarioActor.placeBlock(
                    location,
                    new ItemStack(blockDef.getType()),  // assert blockDef.getType() != null
                    hand,
                    direction
            );
            if (this.shouldFirePlaceEvent(block, blockDef.getType()))
                this.firePlaceEvent(block, player, hand, direction);
        }
        else
            this.makeOutputs(ctxt, location.getBlock(), null);

        blockDef.applyTo(block);
    }

    private boolean shouldFirePlaceEvent(Block at, Material typeToPlace)
    {
        return at.getType() == typeToPlace;
    }

    private void firePlaceEvent(Block block, Player player, NMSHand hand, BlockFace against)
    {
        BlockPlaceEvent event = new BlockPlaceEvent(
                block,
                block.getState(),
                block.getRelative(against),
                new ItemStack(block.getType()),
                player,
                canBuild(player, block.getLocation()),
                hand.toEquipmentSlot()
        );
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedBlockEvent(ctxt, event))
            return false;

        assert event instanceof BlockPlaceEvent;
        BlockPlaceEvent e = (BlockPlaceEvent) event;

        return ctxt.ifHasInput(IN_ACTOR, actor -> actor.checkMatchedPlayer(e.getPlayer()))
                && ctxt.ifHasInput(IN_HAND, hand -> hand == NMSHand.fromEquipmentSlot(e.getHand()))
                && ctxt.ifHasInput(IN_BLOCK, block -> block.isAdequate(e.getBlockPlaced()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                BlockPlaceEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        BlockStructure blockDef = ctxt.input(IN_BLOCK);
        Block block = this.getBlockLocationWithWorld(blockDef, ctxt).getBlock();

        return blockDef.isAdequate(block);
    }

    @Override
    public InputBoard getInputBoard(@NotNull ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_ACTOR, IN_HAND, IN_DIRECTION);

        switch (type)
        {
            case CONDITION_REQUIRE:
                board.requirePresent(IN_BLOCK)
                        .validator(
                                b -> !b.isPresent(IN_ACTOR),
                                "Cannot specify the actor in the condition requiring mode."
                        );
                break;
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
