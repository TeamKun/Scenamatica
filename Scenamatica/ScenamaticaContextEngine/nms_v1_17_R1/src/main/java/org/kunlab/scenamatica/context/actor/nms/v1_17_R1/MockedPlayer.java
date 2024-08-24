package org.kunlab.scenamatica.context.actor.nms.v1_17_R1;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import io.netty.handler.timeout.TimeoutException;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.protocol.game.PacketPlayInArmAnimation;
import net.minecraft.network.protocol.game.PacketPlayInSetCreativeSlot;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayInWindowClick;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.EnumHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryClickType;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
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
        super(minecraftserver, worldserver, gameprofile);
        this.manager = manager;
        this.mocker = mocker;
        this.networkManager = networkManager;
        this.initialLocation = initialLocation;
        this.initialStructure = initialStructure;

        this.setNoGravity(false);
        this.G = 0.5f; // ブロックをのぼれるたかさ
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

        PacketPlayInArmAnimation packet = new PacketPlayInArmAnimation(EnumHand.b);

        this.b.a(packet);
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
                        EnumHand.a
                );
                break;
            case LEFT_CLICK_BLOCK:
            case RIGHT_CLICK_BLOCK:
                CraftEventFactory.callPlayerInteractEvent(
                        this,
                        action,
                        new BlockPosition(block.getX(), block.getY(), block.getZ()),
                        EnumDirection.c,
                        this.getItemInMainHand(),
                        EnumHand.a
                );
                break;
        }
    }

    @Override
    public void interactEntity(@NotNull org.bukkit.entity.Entity entity, @NotNull NMSEntityUseAction type,
                               @Nullable NMSHand hand, @Nullable Location location)
    {
        Entity nmsEntity = ((CraftEntity) entity).getHandle();
        EnumHand enumHand = NMSProvider.getTypeSupport().toNMS(hand, EnumHand.class);

        PacketPlayInUseEntity packet;
        switch (type)
        {
            case ATTACK:
                packet = PacketPlayInUseEntity.a(nmsEntity, this.isSneaking());
                break;
            case INTERACT:
                packet = PacketPlayInUseEntity.a(nmsEntity, this.isSneaking(), enumHand);
                break;
            case INTERACT_AT:
                if (location == null)
                    throw new IllegalArgumentException("location must not be null when type is INTERACT_AT");
                packet = PacketPlayInUseEntity.a(nmsEntity, this.isSneaking(), enumHand, new Vec3D(location.getX(), location.getY(), location.getZ()));
                break;
            default:
                throw new IllegalArgumentException("Unknown NMSEntityUseAction: " + type.name());
        }

        this.b.a(packet);
    }

    @Override
    public void placeItem(@NotNull Location location, @NotNull ItemStack item)
    {
        this.placeItem(location, item, null);
    }

    @Override
    public void placeItem(@NotNull Location location, @NotNull ItemStack item, @Nullable BlockFace face)
    {
        final EnumHand HAND = EnumHand.a;

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        if (face == null)
            face = VoxelUtils.traceDirection(this.getPlayer().getLocation(), location);
        EnumDirection direction = NMSProvider.getTypeSupport().toNMS(NMSDirection.fromBlockFace(face), EnumDirection.class);

        MovingObjectPositionBlock position = new MovingObjectPositionBlock(
                /* vec3D: */ new Vec3D(location.getX(), location.getY(), location.getZ()),
                /* enumDirection: */ direction,
                /* blockPosition: */ new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                /* flag: */ false
        );

        ItemActionContext ctx = new ItemActionContext(
                /* world: */ this.t,
                /* player: */ this,
                /* enumHand: */ HAND,
                /* itemStack: */ nmsItem,
                /* movingObjectPositionBlock: */ position
        );


        nmsItem.placeItem(ctx, HAND);
    }

    @Override
    public void breakBlock(Block block)
    {
        this.d.breakBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()));
    }

    @Override
    public void placeBlock(@NotNull Location location, @NotNull ItemStack stack, @NotNull NMSHand nmsHand, @NotNull BlockFace direction)
    {
        net.minecraft.world.item.ItemStack nmsTack = CraftItemStack.asNMSCopy(stack);
        EnumHand hand = NMSProvider.getTypeSupport().toNMS(nmsHand, EnumHand.class);
        MovingObjectPositionBlock position = new MovingObjectPositionBlock(
                /* vec3D: */ new Vec3D(location.getX(), location.getY(), location.getZ()),
                /* enumDirection: */ EnumDirection.valueOf(direction.name()),  // 互換性あり
                /* blockPosition: */ new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
                /* inside: */ false
        );

        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this, Action.RIGHT_CLICK_BLOCK,
                position.getBlockPosition(),
                position.getDirection(),
                nmsTack,
                true,
                hand
        );
        boolean cancelled = event.useItemInHand() == Event.Result.DENY;
        if (cancelled)
            return;

        this.d.a(
                /* entityPlayer: */ this,
                /* world: */ this.t,
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

        this.b.a(new ChatComponentText(message));
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

    @Override
    public void giveCreativeItem(int slot, @NotNull ItemStack item)
    {
        this.b.a(new PacketPlayInSetCreativeSlot(slot, CraftItemStack.asNMSCopy(item)));
    }

    @Override
    public @NotNull Player getPlayer()
    {
        return this.getBukkitEntity();
    }

    @Override
    public @NotNull UUID getUniqueID()
    {
        return this.getUniqueID();
    }

    @Override
    @NotNull
    public String getActorName()
    {
        return super.getName();
    }

    @Override
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
                nmsType = InventoryClickType.a;
                break;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                nmsType = InventoryClickType.b;
                break;
            case NUMBER_KEY:
                nmsType = InventoryClickType.c;
                break;
            case MIDDLE:
                nmsType = InventoryClickType.d;
                break;
            case DROP:
            case CONTROL_DROP:
                nmsType = InventoryClickType.e;
                break;
            case DOUBLE_CLICK:
                nmsType = InventoryClickType.g;
                break;
            default:
                throw new IllegalArgumentException("Unknown ClickType: " + type.name());
        }

        PacketDataSerializer serializer = new PacketDataSerializer(Unpooled.buffer());

        int stateId = 0;
        if (this.b instanceof MockedPlayerConnection)
        {
            MockedPlayerConnection connection = (MockedPlayerConnection) this.b;
            stateId = connection.getWindowStateId();
        }

        PacketPlayInWindowClick windowClick = new PacketPlayInWindowClick(
                /* windowId: */ this.bV.j,
                /* stateId: */ stateId,
                /* slot: */ slot,
                /* button: */ button,
                /* clickType: */ nmsType,
                /* itemStack: */ CraftItemStack.asNMSCopy(itemStack),
                /* arrayof slots */ Int2ObjectMaps.emptyMap()
        );
        windowClick.a(serializer);

        this.b.a(windowClick);
    }

    @Override
    public boolean doAITick()
    {
        return true;  // ノックバック用
    }

    @Override
    public void playerTick()
    {
        super.entityBaseTick();
        super.playerTick();
        this.P = this.isSpectator();
    }

    @Override
    public void tick()
    {
        if (this.joining)  // 以下の tick によって false になるので, 1 回のみ実行される。
            Bukkit.getPluginManager().callEvent(new ActorPostJoinEvent(this));

        super.tick();
        // this.processGravity();
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float f)
    {
        boolean damaged = super.damageEntity(damageSource, f);
        if (damaged && this.C)
        {
            this.C = false;
            Runner.run(() -> this.C = true);
        }
        return damaged;

    }
}
