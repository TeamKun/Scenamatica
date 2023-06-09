package org.kunlab.scenamatica.context.actor.nms.v_1_16_R3;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import io.netty.handler.timeout.TimeoutException;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumDirection;
import net.minecraft.server.v1_16_R3.EnumHand;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.EnumMoveType;
import net.minecraft.server.v1_16_R3.InventoryClickType;
import net.minecraft.server.v1_16_R3.Item;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.MovingObjectPositionBlock;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import net.minecraft.server.v1_16_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_16_R3.PacketPlayInWindowClick;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_16_R3.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.events.actor.ActorPostJoinEvent;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Random;
import java.util.UUID;

class MockedPlayer extends EntityPlayer implements Actor
{
    private final ActorManager manager;
    private final PlayerMocker mocker;
    @Getter
    private final NetworkManager networkManager;

    public MockedPlayer(
            ActorManager manager,
            PlayerMocker mocker,
            NetworkManager networkManager,
            MinecraftServer minecraftserver,
            WorldServer worldserver,
            GameProfile gameprofile)
    {
        super(minecraftserver, worldserver, gameprofile, new PlayerInteractManager(worldserver));
        this.manager = manager;
        this.mocker = mocker;
        this.networkManager = networkManager;

        this.setNoGravity(false);
        this.G = 0.5f; // ブロックのぼれるたかさ
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

    @Override
    public void breakBlock(Block block)
    {
        this.playerInteractManager.breakBlock(new BlockPosition(block.getX(), block.getY(), block.getZ()));
    }

    @Override
    public void placeBlock(@NotNull Location location, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull BlockFace direction)
    {
        net.minecraft.server.v1_16_R3.ItemStack nmsTack = CraftItemStack.asNMSCopy(stack);
        EnumHand hand = slot == EquipmentSlot.HAND ? EnumHand.MAIN_HAND: EnumHand.OFF_HAND;
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

        this.playerInteractManager.a(
                /* entityPlayer: */ this,
                /* world: */ this.world,
                /* itemStack: */ CraftItemStack.asNMSCopy(stack),
                /* enumHand: */ hand,
                /* movingObjectPositionBlock: */ position
        );
    }

    @Override
    public void joinServer()
    {
        this.mocker.doLogin(this.server, this);
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
                /* throwable: */ new IllegalStateException("Erroneous packet for " + this.getName() + " [E]!")
        );
        this.actualLeave("Internal server error");
    }

    @Override
    public void consume(@NotNull EquipmentSlot slot)
    {
        if (!(slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND))
            throw new IllegalArgumentException("slot must be HAND or OFF_HAND");

        EnumHand hand = slot == EquipmentSlot.HAND ? EnumHand.MAIN_HAND: EnumHand.OFF_HAND;

        net.minecraft.server.v1_16_R3.ItemStack nmsStack = this.b(hand);
        if (!(nmsStack == null || nmsStack.getItem().isFood()))
            throw new IllegalStateException("Item in " + slot.name() + " is not food");

        this.c(hand);
    }

    @Override
    public void breakItem(@NotNull EquipmentSlot slot)
    {
        EnumItemSlot nmsSlot = CraftEquipmentSlot.getNMS(slot);
        net.minecraft.server.v1_16_R3.ItemStack st = this.getEquipment(nmsSlot);
        Item item = st.getItem();

        st.damage(item.getMaxDurability() - st.getDamage(), this, (entity) -> entity.broadcastItemBreak(nmsSlot));
    }

    @Override
    public @NotNull Player getPlayer()
    {
        return this.getBukkitEntity();
    }

    @Override
    public @NotNull UUID getUUID()
    {
        return this.getUniqueID();
    }

    @Override
    @NotNull
    public String getName()
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
    public boolean doAITick()
    {
        return true;  // ノックバック用
    }

    @Override
    public void playerTick()
    {
        super.entityBaseTick();
        super.playerTick();
        this.noclip = this.isSpectator();

    }

    @Override
    public void tick()
    {
        if (this.joining)  // 以下の tick によって false になるので, 1 回のみ実行される。
            Bukkit.getPluginManager().callEvent(new ActorPostJoinEvent(this));

        super.tick();
        this.processGravity();
    }

    private void processGravity()
    {
        Vec3D vec = this.getMot();

        if (!this.onGround)
        {
            if (this.inWater)
                vec = vec.add(0, -0.0252f, 0);
            else
                vec = vec.add(0, -0.4, 0);
        }

        this.move(EnumMoveType.SELF, vec);
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float f)
    {
        Entity damager = damageSource.getEntity();

        boolean damaged = super.damageEntity(damageSource, f);
        if (damaged && damager != null)
            this.processKnockBack(damager);

        return damaged;
    }

    private void processKnockBack(Entity damager)
    {
        float knockbackDepth = 1.2F;

        Runner.run(() -> this.setMot(new Vec3D(
                        -Math.sin(damager.yaw * Math.PI / 180.0F) * knockbackDepth * 0.5F,
                        0.8F,
                        Math.cos(damager.yaw * Math.PI / 180.0F) * knockbackDepth * 0.5F
                )
        ));
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
