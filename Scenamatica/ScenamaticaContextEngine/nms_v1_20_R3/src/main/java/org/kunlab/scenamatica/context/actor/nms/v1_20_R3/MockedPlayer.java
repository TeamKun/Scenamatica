package org.kunlab.scenamatica.context.actor.nms.v1_20_R3;

import com.mojang.authlib.GameProfile;
import io.netty.handler.timeout.TimeoutException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import lombok.Getter;
import lombok.SneakyThrows;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R4.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.VoxelUtils;
import org.kunlab.scenamatica.events.actor.ActorPostJoinEvent;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;
import org.kunlab.scenamatica.nms.enums.voxel.NMSDirection;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.UUID;

class MockedPlayer extends ServerPlayer implements Actor
{
    private final ActorManager manager;
    private final PlayerMocker mocker;
    @Getter
    private final Location initialLocation;
    @Getter
    private final PlayerStructure initialStructure;
    @Getter
    private final MockedConnection mockedConnection;

    public MockedPlayer(
            ActorManager manager,
            PlayerMocker mocker,
            MinecraftServer minecraftserver,
            ClientInformation clinfo,
            ServerLevel serverLevel,
            GameProfile gameprofile,
            Location initialLocation,
            PlayerStructure initialStructure)
    {
        super(minecraftserver, serverLevel, gameprofile, clinfo);
        this.manager = manager;
        this.mocker = mocker;
        this.initialLocation = initialLocation;
        this.initialStructure = initialStructure;
        this.mockedConnection = new MockedConnection(this, server);

        this.setNoGravity(false);
        Objects.requireNonNull(this.getAttribute(Attributes.STEP_HEIGHT)).setBaseValue(0.5d);
    }

    private static TimeoutException createNIOTimeoutException()
    {
        try
        {
            Constructor<TimeoutException> constructor = TimeoutException.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
        catch (ReflectiveOperationException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public @NotNull ActorManager getManager()
    {
        return this.manager;
    }

    @Override
    public void playAnimation(@NotNull PlayerAnimationType animation)
    {
        InteractionHand armToSwing;
        switch (animation)
        {
            case ARM_SWING:
                armToSwing = InteractionHand.MAIN_HAND;
                break;
            case OFF_ARM_SWING:
                armToSwing = InteractionHand.OFF_HAND;
                break;
            default:
                throw new IllegalArgumentException("Unknown PlayerAnimationType: " + animation.name());
        }

        ServerboundSwingPacket packet = new ServerboundSwingPacket(armToSwing);

        this.connection.handleAnimate(packet);
    }

    @Override
    public void interactAt(@NotNull Action action, Block block)
    {
        switch (action)
        {
            case LEFT_CLICK_AIR:
            case RIGHT_CLICK_AIR:
                CraftEventFactory.callPlayerInteractEvent(
                        this,
                        action,
                        this.getMainHandItem(),
                        InteractionHand.MAIN_HAND
                );
                break;
            case LEFT_CLICK_BLOCK:
            case RIGHT_CLICK_BLOCK:
                CraftEventFactory.callPlayerInteractEvent(
                        this,
                        action,
                        new BlockPos(block.getX(), block.getY(), block.getZ()),
                        Direction.NORTH,
                        this.getMainHandItem(),
                        InteractionHand.MAIN_HAND
                );
                break;
        }
    }

    @Override
    public void interactEntity(@NotNull org.bukkit.entity.Entity entity, @NotNull NMSEntityUseAction type,
                               @Nullable NMSHand hand, @Nullable Location location)
    {
        Entity nmsEntity = ((CraftEntity) entity).getHandle();
        InteractionHand enumHand = NMSProvider.getTypeSupport().toNMS(hand, InteractionHand.class);

        ServerboundInteractPacket packet;
        switch (type)
        {
            case ATTACK:
                packet = ServerboundInteractPacket.createAttackPacket(nmsEntity, this.isShiftKeyDown());
                break;
            case INTERACT:
                packet = ServerboundInteractPacket.createInteractionPacket(nmsEntity, this.isShiftKeyDown(), enumHand);
                break;
            case INTERACT_AT:
                if (location == null)
                    throw new IllegalArgumentException("location must not be null when type is INTERACT_AT");
                packet = ServerboundInteractPacket.createInteractionPacket(
                        nmsEntity,
                        this.isShiftKeyDown(),
                        enumHand,
                        new Vec3(location.getX(), location.getY(), location.getZ())
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown NMSEntityUseAction: " + type.name());
        }
        
        this.connection.handleInteract(packet);
    }

    @Override
    public void placeItem(@NotNull Location location, @NotNull ItemStack item)
    {
        this.placeItem(location, item, null);
    }

    @Override
    public void placeItem(@NotNull Location location, @NotNull ItemStack item, @Nullable BlockFace face)
    {
        final InteractionHand HAND = InteractionHand.MAIN_HAND;

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        if (face == null)
            face = VoxelUtils.traceDirection(this.getPlayer().getLocation(), location);
        Direction direction = NMSProvider.getTypeSupport().toNMS(NMSDirection.fromBlockFace(face), Direction.class);

        BlockHitResult position = new BlockHitResult(
                /* vec3D: */ new Vec3(location.getX(), location.getY(), location.getZ()),
                /* enumDirection: */ direction,
                /* blockPosition: */ new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                /* flag: */ false
        );

        UseOnContext ctx = new UseOnContext(
                /* world: */ this.level(),
                /* player: */ this,
                /* enumHand: */ HAND,
                /* itemStack: */ nmsItem,
                /* movingObjectPositionBlock: */ position
        );


        nmsItem.useOn(ctx);
    }

    @Override
    public void breakBlock(Block block)
    {
        this.gameMode.destroyBlock(new BlockPos(block.getX(), block.getY(), block.getZ()));
    }

    @Override
    public void placeBlock(@NotNull Location location, @NotNull ItemStack stack, @NotNull NMSHand nmsHand, @NotNull BlockFace direction)
    {
        net.minecraft.world.item.ItemStack nmsTack = CraftItemStack.asNMSCopy(stack);
        InteractionHand hand = NMSProvider.getTypeSupport().toNMS(nmsHand, InteractionHand.class);
        Vec3 vec3 = new Vec3(location.getX(), location.getY(), location.getZ());
        BlockHitResult position = new BlockHitResult(
                /* vec3D: */ vec3,
                /* enumDirection: */ Direction.byName(direction.name()),  // 互換性あり
                /* blockPosition: */ new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                /* inside: */ false
        );

        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(
                this,
                Action.RIGHT_CLICK_BLOCK,
                position.getBlockPos(),
                position.getDirection(),
                nmsTack,
                true,
                hand,
                vec3
        );
        boolean cancelled = event.useItemInHand() == Event.Result.DENY;
        if (cancelled)
            return;

        this.gameMode.useItemOn(
                /* entityPlayer: */ this,
                /* world: */ this.level(),
                /* itemStack: */ CraftItemStack.asNMSCopy(stack),
                /* enumHand: */ hand,
                /* movingObjectPositionBlock: */ position
        );
    }

    @Override
    public void joinServer()
    {
        this.mocker.doLogin(this);
    }

    private void actualLeave(String message)
    {
        // this.server.getPlayerList().disconnect(this);
        // ↑は, PaperMC の改悪により使用できない（定義の変更： String disconnectMessage -> advanture.Component disconnectMessage）

        this.connection.disconnect(message);
    }

    @Override
    @SneakyThrows
    public void leaveServer()
    {
        this.actualLeave("Disconnected");
    }

    @Override
    public void kickTimeout()
    {
        this.actualLeave("Timed out");
    }

    @Override
    public void kickErroneous()
    {
        this.actualLeave("Internal server error");
    }

    @Override
    public void giveCreativeItem(int slot, @NotNull ItemStack item)
    {
        this.connection.handleSetCreativeModeSlot(new ServerboundSetCreativeModeSlotPacket(slot, CraftItemStack.asNMSCopy(item)));
    }

    @Override
    public @NotNull Player getPlayer()
    {
        return this.getBukkitEntity();
    }

    @Override
    public @NotNull UUID getUniqueID()
    {
        return this.uuid;
    }

    @Override
    @NotNull
    public String getActorName()
    {
        return this.getGameProfile().getName();
    }

    @Override
    public void clickInventory(@NotNull ClickType type,
                               int slot,
                               int button,
                               @Nullable ItemStack itemStack)
    {
        net.minecraft.world.inventory.ClickType nmsType;
        switch (type)
        {
            case LEFT:
            case RIGHT:
                nmsType = net.minecraft.world.inventory.ClickType.PICKUP;
                break;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                nmsType = net.minecraft.world.inventory.ClickType.QUICK_MOVE;
                break;
            case NUMBER_KEY:
                nmsType = net.minecraft.world.inventory.ClickType.SWAP;
                break;
            case MIDDLE:
                nmsType = net.minecraft.world.inventory.ClickType.CLONE;
                break;
            case DROP:
            case CONTROL_DROP:
                nmsType = net.minecraft.world.inventory.ClickType.THROW;
                break;
            case DOUBLE_CLICK:
                nmsType = net.minecraft.world.inventory.ClickType.QUICK_CRAFT;
                break;
            default:
                throw new IllegalArgumentException("Unknown ClickType: " + type.name());
        }

        int stateId = this.mockedConnection.getWindowStateId();

        ServerboundContainerClickPacket windowClick = new ServerboundContainerClickPacket(
                /* windowId: */ this.containerMenu.containerId,
                /* stateId: */ stateId,
                /* slot: */ slot,
                /* button: */ button,
                /* clickType: */ nmsType,
                /* itemStack: */ CraftItemStack.asNMSCopy(itemStack),
                /* arrayof slots */ Int2ObjectMaps.emptyMap()
        );

        this.connection.handleContainerClick(windowClick);
    }

    @Override
    public void tick()
    {
        if (this.joining)  // 以下の tick によって false になるので, 1 回のみ実行される。
            Bukkit.getPluginManager().callEvent(new ActorPostJoinEvent(this));

        super.tick();
        // this.processGravity();
    }
}
