package org.kunlab.scenamatica.context.actor.nms.v1_13_R1;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import io.netty.handler.timeout.TimeoutException;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.ChatComponentText;
import net.minecraft.server.v1_13_R1.DamageSource;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.EnumDirection;
import net.minecraft.server.v1_13_R1.EnumHand;
import net.minecraft.server.v1_13_R1.InventoryClickType;
import net.minecraft.server.v1_13_R1.ItemActionContext;
import net.minecraft.server.v1_13_R1.MinecraftServer;
import net.minecraft.server.v1_13_R1.NetworkManager;
import net.minecraft.server.v1_13_R1.PacketDataSerializer;
import net.minecraft.server.v1_13_R1.PacketPlayInArmAnimation;
import net.minecraft.server.v1_13_R1.PacketPlayInUseEntity;
import net.minecraft.server.v1_13_R1.PacketPlayInWindowClick;
import net.minecraft.server.v1_13_R1.PlayerInteractManager;
import net.minecraft.server.v1_13_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_13_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
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
import org.kunlab.scenamatica.context.actor.nms.v1_13_R1.packets.MockedPacketPlayInSetCreativeSlot;
import org.kunlab.scenamatica.events.actor.ActorPostJoinEvent;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;
import org.kunlab.scenamatica.nms.enums.voxel.NMSDirection;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Random;
import java.util.UUID;

class MockedPlayer extends EntityPlayer implements Actor
{
    private final ActorManager manager;
    private final PlayerMocker mocker;
    @Getter
    private final Location initialLocation;
    @Getter
    private final NetworkManager networkManager;
    @Getter
    private final PlayerStructure initialStructure;

    public MockedPlayer(
            ActorManager manager,
            PlayerMocker mocker,
            NetworkManager networkManager,
            MinecraftServer minecraftserver,
            WorldServer worldserver,
            GameProfile gameprofile,
            Location initialLocation,
            PlayerStructure initialStructure)
    {
        super(minecraftserver, worldserver, gameprofile, new PlayerInteractManager(worldserver));
        this.manager = manager;
        this.mocker = mocker;
        this.networkManager = networkManager;
        this.initialLocation = initialLocation;
        this.initialStructure = initialStructure;

        this.setNoGravity(false);
        this.Q = 0.5f; // ブロックをのぼれるたかさ
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
        assert animation == PlayerAnimationType.ARM_SWING;  // Bukkit はこれしかない。

        PacketPlayInArmAnimation packet = new PacketPlayInArmAnimation(EnumHand.MAIN_HAND);

        this.playerConnection.a(packet);
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
                        this.getItemInMainHand(),
                        EnumHand.MAIN_HAND
                );
                break;
            case LEFT_CLICK_BLOCK:
            case RIGHT_CLICK_BLOCK:
                CraftEventFactory.callPlayerInteractEvent(
                        this,
                        action,
                        new BlockPosition(block.getX(), block.getY(), block.getZ()),
                        EnumDirection.NORTH,
                        this.getItemInMainHand(),
                        EnumHand.MAIN_HAND
                );
                break;
        }
    }

    @SneakyThrows(IOException.class)
    @Override
    public void interactEntity(@NotNull org.bukkit.entity.Entity entity, @NotNull NMSEntityUseAction type,
                               @Nullable NMSHand hand, @Nullable Location location)
    {
        int entityId = entity.getEntityId();
        EnumHand enumHand = NMSProvider.getTypeSupport().toNMS(hand, EnumHand.class);
        PacketPlayInUseEntity.EnumEntityUseAction nmsAction = NMSProvider.getTypeSupport().toNMS(type, PacketPlayInUseEntity.EnumEntityUseAction.class);

        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());
        serializer.d(entityId);
        serializer.a(nmsAction);
        if (nmsAction == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT)
        {
            if (location == null)
                throw new IllegalArgumentException("location must not be null when type is INTERACT_AT");

            serializer.writeFloat((float) location.getX());
            serializer.writeFloat((float) location.getY());
            serializer.writeFloat((float) location.getZ());
        }

        if (nmsAction == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT
                || nmsAction == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT)
            serializer.a(enumHand);

        boolean isSneaking = this.getBukkitEntity().isSneaking();
        serializer.writeBoolean(isSneaking);

        PacketPlayInUseEntity packet = new PacketPlayInUseEntity();
        packet.a(serializer);

        this.playerConnection.a(packet);
    }

    @Override
    public void placeItem(@NotNull Location location, @NotNull ItemStack item)
    {
        this.placeItem(location, item, null);
    }

    @Override
    public void placeItem(@NotNull Location location, @NotNull ItemStack item, @Nullable BlockFace face)
    {
        final EnumHand HAND = EnumHand.MAIN_HAND;

        net.minecraft.server.v1_13_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        if (face == null)
            face = VoxelUtils.traceDirection(this.getPlayer().getLocation(), location);
        EnumDirection direction = NMSProvider.getTypeSupport().toNMS(NMSDirection.fromBlockFace(face), EnumDirection.class);

        BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockCornerTracer.TraceResult trace = BlockCornerTracer.trace(this, location);
        ItemActionContext ctx = new ItemActionContext(
                /* player: */ this,
                /* itemStack: */ nmsItem,
                /* ObjectPositionBlock: */ position,
                /* direction: */ direction,
                /* cursorPositionX: */ trace.getX(),
                /* cursorPositionY: */ trace.getY(),
                /* cursorPositionZ: */ trace.getZ()
        );


        nmsItem.placeItem(ctx, HAND);
    }

    @Override
    public void breakBlock(Block block)
    {
        this.playerInteractManager.breakBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()));
    }

    @Override
    public void placeBlock(@NotNull Location location, @NotNull ItemStack stack, @NotNull NMSHand nmsHand, @NotNull BlockFace direction)
    {
        net.minecraft.server.v1_13_R1.ItemStack nmsTack = CraftItemStack.asNMSCopy(stack);
        EnumHand hand = NMSProvider.getTypeSupport().toNMS(nmsHand, EnumHand.class);
        BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        EnumDirection nmsDirection = NMSProvider.getTypeSupport().toNMS(NMSDirection.fromBlockFace(direction), EnumDirection.class);
        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this, Action.RIGHT_CLICK_BLOCK,
                position,
                nmsDirection,
                nmsTack,
                true,
                hand
        );
        boolean cancelled = event.useItemInHand() == Event.Result.DENY;
        if (cancelled)
            return;

        BlockCornerTracer.TraceResult trace = BlockCornerTracer.trace(this, location);
        this.playerInteractManager.a(
                /* entityPlayer: */ this,
                /* world: */ this.world,
                /* itemStack: */ CraftItemStack.asNMSCopy(stack),
                /* enumHand: */ hand,
                /* movingObjectPositionBlock: */ position,
                /* enumDirection: */ nmsDirection,
                /* cursorPositionX: */ trace.getX(),
                /* cursorPositionY: */ trace.getY(),
                /* cursorPositionZ: */ trace.getZ()
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

        this.playerConnection.a(new ChatComponentText(message));
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

        this.networkManager.exceptionCaught(
                /* channelhandlercontext: */ null,  // なんでもいい。
                /* throwable: */ createNIOTimeoutException()
        );
        this.actualLeave("Timed out");
    }

    @Override
    public void kickErroneous()
    {
        this.networkManager.exceptionCaught(
                /* channelhandlercontext: */ null,  // なんでもいい。
                /* throwable: */ new IllegalStateException("Erroneous packet for " + this.getActorName() + " [E]!")
        );
        this.actualLeave("Internal server error");
    }

    public void giveCreativeItem(int slot, @NotNull ItemStack item)
    {
        this.playerConnection.a(new MockedPacketPlayInSetCreativeSlot(slot, item));
    }

    @Override
    public @NotNull Player getPlayer()
    {
        return this.getBukkitEntity();
    }

    @Override
    public @NotNull UUID getUniqueID()
    {
        return this.uniqueID;
    }

    @Override
    @NotNull
    public String getActorName()
    {
        return super.getName();
    }

    @Override
    @SneakyThrows(IOException.class)
    public void clickInventory(@NotNull ClickType type,
                               int slot,
                               int button,
                               @Nullable ItemStack itemStack)
    {
        InventoryClickType nmsType;
        switch (type)
        {
            case LEFT:
            case RIGHT:
                nmsType = InventoryClickType.PICKUP;
                break;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                nmsType = InventoryClickType.QUICK_MOVE;
                break;
            case NUMBER_KEY:
                nmsType = InventoryClickType.SWAP;
                break;
            case MIDDLE:
                nmsType = InventoryClickType.CLONE;
                break;
            case DROP:
            case CONTROL_DROP:
                nmsType = InventoryClickType.THROW;
                break;
            case DOUBLE_CLICK:
                nmsType = InventoryClickType.PICKUP_ALL;
                break;
            default:
                throw new IllegalArgumentException("Unknown ClickType: " + type.name());
        }

        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());

        serializer.writeByte(this.activeContainer.windowId);
        serializer.writeShort(slot);
        serializer.writeByte(button);
        serializer.writeShort(new Random().nextInt());  // 使わないトランザクション ID (重複するとヤバい)
        serializer.a(nmsType);
        serializer.a(CraftItemStack.asNMSCopy(itemStack));

        PacketPlayInWindowClick windowClick = new PacketPlayInWindowClick();
        windowClick.a(serializer);

        this.playerConnection.a(windowClick);
    }

    @Override
    public void playerTick()
    {
        super.playerTick();
        this.noclip = this.isSpectator();
    }

    @Override
    public void tick()
    {
        if (this.joining)  // 以下の tick によって false になるので, 1 回のみ実行される。
            Bukkit.getPluginManager().callEvent(new ActorPostJoinEvent(this));

        super.tick();
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float f)
    {
        boolean damaged = super.damageEntity(damageSource, f);
        if (damaged && this.velocityChanged)
        {
            this.velocityChanged = false;
            Runner.run(() -> this.velocityChanged = true);
        }
        return damaged;

    }

    @Override
    public void die(DamageSource damagesource)
    {
        if (this.dead)
            return;

        super.die(damagesource);
        Runner.runLater(() -> this.getWorldServer().removeEntity(this), 15L);
        // 15L 遅らせるのは, アニメーションのため
    }
}
