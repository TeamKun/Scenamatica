package net.kunmc.lab.scenamatica.context.actor.nms.v_1_16_R3;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBufAllocator;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.kunmc.lab.scenamatica.context.actor.PlayerMockerBase;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.ActorManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryBean;
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

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PlayerMocker extends PlayerMockerBase
{
    private final ScenamaticaRegistry registry;

    public PlayerMocker(ScenamaticaRegistry registry, ActorManager manager)
    {
        super(registry, manager);
        this.registry = registry;
    }

    private void registerPlayer(MinecraftServer server, MockedPlayer player, WorldServer worldServer)
    {
        PlayerList list = server.getPlayerList();
        NetworkManager mockedNetworkManager = new MockedNetworkManager(this, player, server);
        list.a(mockedNetworkManager, player);
        sendSettings(player);

        Runner.runLater(() -> {
            player.playerConnection = new MockedPlayerConnection(server, mockedNetworkManager, player);

            if (!Bukkit.getWorlds().get(0).getUID().equals(worldServer.getWorld().getUID()))
            {
                double x = player.locX();
                double y = player.locY();
                double z = player.locZ();
                player.a(worldServer, x, y, z, player.yaw, player.pitch, PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        }, 1);
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

        int opLevel = bean.getOpLevel();
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

        for (String permission : bean.getActivePermissions())
            player.getBukkitEntity().addAttachment(this.registry.getPlugin(), permission, true);
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
        bean.getTags().forEach(player::addScoreboardTag);
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
                        player.inventory.armor.set(i, CraftItemStack.asNMSCopy(item.toItemStack()));
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
    public Player mock(@Nullable World world, @NotNull PlayerBean bean)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.E();
        GameProfile profile = createGameProfile(bean);

        MockedPlayer player = new MockedPlayer(server, worldServer, profile);
        this.initializePlayer(player, bean);

        if (!dispatchLoginEvent(player.getBukkitEntity()))
            throw new IllegalStateException("Login for " + player.getName() + " was denied.");

        this.registerPlayer(server, player, world != null ? ((CraftWorld) world).getHandle(): worldServer);

        return player.getBukkitEntity();
    }

    @Override
    public void unmock(Player player)
    {
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

        ((MockedPlayerConnection) mockedPlayer.playerConnection).shutdown();
        mockedPlayer.playerConnection.disconnect("Unmocked");
    }

    @Override
    protected Class<?> getLoginListenerClass()
    {
        return LoginListener.class;
    }

}
