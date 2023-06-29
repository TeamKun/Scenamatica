package org.kunlab.scenamatica.context.actor.nms.v_1_16_R3;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBufAllocator;
import lombok.SneakyThrows;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumGamemode;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.LoginListener;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.MobEffect;
import net.minecraft.server.v1_16_R3.MobEffectList;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.OpListEntry;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import net.minecraft.server.v1_16_R3.PacketPlayInSettings;
import net.minecraft.server.v1_16_R3.PlayerList;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.context.actor.PlayerMockerBase;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryBean;
import org.kunlab.scenamatica.settings.ActorSettings;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class PlayerMocker extends PlayerMockerBase
{
    private final ScenamaticaRegistry registry;
    private final ActorManager manager;
    private final ActorSettings settings;

    public PlayerMocker(ScenamaticaRegistry registry, ActorManager manager)
    {
        super(registry, manager);
        this.registry = registry;
        this.manager = manager;

        this.settings = registry.getEnvironment().getActorSettings();
    }

    @SneakyThrows(IOException.class)
    private static void sendSettings(EntityPlayer player)
    {
        String locale = "en_US";
        int viewDistance = 0x02;
        int chatMode = 0;  // 0: enabled, 1: commands only, 2: hidden
        boolean chatColors = true;
        int displayedSkinParts = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;
        // 0x01: cape, 0x02: jacket, 0x04: left sleeve, 0x08: right sleeve,
        // 0x10: left pants leg, 0x20: right pants leg, 0x40: hat
        int mainHand = 1; // 0: left, 1: right

        PacketDataSerializer serializer = new PacketDataSerializer(ByteBufAllocator.DEFAULT.buffer());
        serializer.a(locale);
        serializer.d(viewDistance);
        serializer.d(chatMode);
        serializer.writeBoolean(chatColors);
        serializer.d(displayedSkinParts);
        serializer.d(mainHand);

        PacketPlayInSettings packet = new PacketPlayInSettings();
        packet.a(serializer);

        player.a(packet);
    }

    private void registerPlayer(MinecraftServer server, MockedPlayer player)
    {
        PlayerList list = server.getPlayerList();
        list.a(player.getNetworkManager(), player);
        sendSettings(player);
    }

    private void initializePlayer(MockedPlayer player, PlayerBean bean)
    {
        this.initHumanEntity(player, bean);
        this.initBasePlayer(player, bean);
        this.initEntity(player, bean);
    }

    private void initBasePlayer(MockedPlayer player, PlayerBean bean)
    {
        if (bean.getDisplayName() != null)
            player.displayName = bean.getDisplayName();
        if (bean.getPlayerListName() != null)
            player.listName = new ChatComponentText(bean.getPlayerListName());
        if (bean.getPlayerListHeader() != null)
            player.getBukkitEntity().setPlayerListHeader(bean.getPlayerListHeader());
        if (bean.getPlayerListFooter() != null)
            player.getBukkitEntity().setPlayerListFooter(bean.getPlayerListFooter());
        if (bean.getCompassTarget() != null)
            player.compassTarget = player.getBukkitEntity().getLocation();
        if (bean.getBedSpawnLocation() != null)
        {
            Location loc = bean.getBedSpawnLocation();
            World world = Bukkit.getWorld(loc.getWorld().getName());
            if (world == null)
                throw new IllegalArgumentException("World not found: " + loc.getWorld().getName());

            BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

            player.setRespawnPosition(
                    ((CraftWorld) world).getHandle().getDimensionKey(), pos,
                    0,
                    false,
                    true
            );
        }
        if (bean.getExp() != null)
            player.giveExp(bean.getExp());
        if (bean.getLevel() != null)
            player.expLevel = bean.getLevel();
        if (bean.getTotalExperience() != null)
            player.expTotal = bean.getTotalExperience();
        if (bean.isFlying())
            player.abilities.isFlying = bean.isFlying();
        if (bean.getWalkSpeed() != null)
            player.abilities.walkSpeed = bean.getWalkSpeed();
        if (bean.getFlySpeed() != null)
            player.abilities.flySpeed = bean.getFlySpeed();

        int opLevel = bean.getOpLevel() == -1 ? this.settings.getDefaultOPLevel(): bean.getOpLevel();
        boolean isOP = opLevel > 0;
        if (isOP)
        {
            OpListEntry entry = new OpListEntry(
                    player.getProfile(),
                    opLevel,
                    true  //
            );

            player.server.getPlayerList().getOPs().add(entry);
        }

        // permissions

        Stream.of(bean.getActivePermissions(), this.settings.getDefaultPermissions())
                .flatMap(List::stream)
                .distinct()
                .forEach(permission -> player.getBukkitEntity().addAttachment(this.registry.getPlugin(), permission, true));
    }

    @SuppressWarnings("deprecation")
    private void initEntity(MockedPlayer player, PlayerBean bean)
    {
        if (bean.getLocation() != null)
        {
            Location loc = bean.getLocation();
            World world = Bukkit.getWorld(loc.getWorld().getName());
            if (world == null)
                throw new IllegalArgumentException("World not found: " + loc.getWorld().getName());

            player.setLocation(
                    loc.getX(), loc.getY(), loc.getZ(),
                    loc.getYaw(), loc.getPitch()
            );
        }
        if (bean.getCustomName() != null)
            player.setCustomName(new ChatComponentText(bean.getCustomName()));
        if (bean.isGlowing())
            player.glowing = true;
        if (!bean.isGravity())  // 自動で true にされてる
            player.setNoGravity(true);

        Stream.of(bean.getTags(), this.settings.getDefaultScoreboardTags())
                .flatMap(List::stream)
                .distinct()
                .forEach(player::addScoreboardTag);

        if (bean.getMaxHealth() != null)
            Objects.requireNonNull(player.getAttributeInstance(GenericAttributes.MAX_HEALTH))
                    .setValue(bean.getMaxHealth());
        if (bean.getHealth() != null)
            player.setHealth(bean.getHealth());
        for (PotionEffect effect : bean.getPotionEffects())
            player.addEffect(new MobEffect(
                    MobEffectList.fromId(effect.getType().getId()),  // deprecated
                    effect.getDuration(),
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.hasParticles(),
                    effect.hasIcon()
            ));
        if (bean.getLastDamageCause() != null)
            player.getBukkitEntity().setLastDamageCause(new EntityDamageEvent(
                    player.getBukkitEntity(),
                    bean.getLastDamageCause().getCause(),
                    bean.getLastDamageCause().getDamage()
            ));
    }

    private void initHumanEntity(MockedPlayer player, PlayerBean bean)
    {
        if (bean.getInventory() != null)
        {
            PlayerInventoryBean inventory = bean.getInventory();
            for (Map.Entry<Integer, ItemStackBean> entry : inventory.getMainContents().entrySet())
                player.inventory.setItem(entry.getKey(), CraftItemStack.asNMSCopy(entry.getValue().toItemStack()));

            if (inventory.getArmorContents() != null)
                for (int i = 0; i < 4; i++)
                {
                    ItemStackBean item = inventory.getArmorContents()[i];
                    if (item != null)
                    {
                        int slot = 3 - i;  // 直感的に, 0 がヘルメットになるように逆順にする。
                        player.inventory.armor.set(slot, CraftItemStack.asNMSCopy(item.toItemStack()));
                    }
                }

            if (inventory.getOffHand() != null)
                player.getBukkitEntity().getInventory().setItemInOffHand(inventory.getOffHand().toItemStack());
            if (inventory.getMainHand() != null)
                player.getBukkitEntity().getInventory().setItemInMainHand(inventory.getMainHand().toItemStack());
        }
        if (player.getBukkitEntity().getGameMode() != bean.getGamemode())
            player.playerInteractManager.setGameMode(EnumGamemode.a(bean.getGamemode().name().toLowerCase(Locale.ROOT)));
        if (bean.getFoodLevel() != null)
            player.getFoodData().foodLevel = bean.getFoodLevel();
    }

    @Override
    @NotNull
    public Actor mock(@Nullable World world, @NotNull PlayerBean bean)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        NetworkManager mockedNetworkManager = new MockedNetworkManager(server);
        WorldServer worldServer = server.E();
        GameProfile profile = createGameProfile(bean);
        boolean doLogin = bean.isOnline();

        MockedPlayer player = new MockedPlayer(this.manager, this, mockedNetworkManager, server, worldServer, profile);
        this.initializePlayer(player, bean);

        if (doLogin)
            this.doLogin(server, player);

        return player;
    }

    /* non-public */ void doLogin(MinecraftServer server, MockedPlayer player)
    {
        if (!this.dispatchLoginEvent(player.getBukkitEntity()))
            throw new IllegalStateException("Login for " + player.getName() + " was denied.");

        this.registerPlayer(server, player);
    }

    @Override
    public void unmock(Actor actor)
    {
        Player player = actor.getPlayer();
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();

        if (!(entityPlayer instanceof MockedPlayer))
            return;

        MockedPlayer mockedPlayer = (MockedPlayer) entityPlayer;

        MinecraftServer server = mockedPlayer.getMinecraftServer();
        assert server != null;

        if (server.getPlayerList().isOp(mockedPlayer.getProfile()))
            server.getPlayerList().removeOp(mockedPlayer.getProfile());
        player.getEffectivePermissions().forEach(permissionAttachmentInfo -> {
            if (permissionAttachmentInfo.getAttachment() != null)
                player.removeAttachment(permissionAttachmentInfo.getAttachment());
        });

        mockedPlayer.playerConnection.disconnect("Unmocked");
    }

    @Override
    protected Class<?> getLoginListenerClass()
    {
        return LoginListener.class;
    }

    @Override
    public void postActorLogin(Player player)
    {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        assert entityPlayer instanceof MockedPlayer;
        MockedPlayer mockedPlayer = (MockedPlayer) entityPlayer;

        mockedPlayer.playerConnection = new MockedPlayerConnection(
                mockedPlayer.server,
                mockedPlayer.playerConnection.networkManager,
                mockedPlayer
        );

        WorldServer worldServer = mockedPlayer.getWorldServer();
        double x = mockedPlayer.locX();
        double y = mockedPlayer.locY();
        double z = mockedPlayer.locZ();
        float yaw = mockedPlayer.yaw;
        float pitch = mockedPlayer.pitch;
        mockedPlayer.a(worldServer, x, y, z, yaw, pitch, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

}
