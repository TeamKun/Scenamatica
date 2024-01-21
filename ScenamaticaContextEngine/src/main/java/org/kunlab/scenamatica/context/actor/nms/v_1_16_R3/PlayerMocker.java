package org.kunlab.scenamatica.context.actor.nms.v_1_16_R3;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBufAllocator;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
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
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.context.actor.PlayerMockerBase;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryStructure;
import org.kunlab.scenamatica.settings.ActorSettings;

import java.io.IOException;
import java.net.InetSocketAddress;
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

    private void initializePlayer(MockedPlayer player, PlayerStructure structure)
    {
        this.initHumanEntity(player, structure);
        this.initBasePlayer(player, structure);
        this.initEntity(player, structure);
    }

    private void initBasePlayer(MockedPlayer player, PlayerStructure structure)
    {
        if (structure.getDisplayName() != null)
            player.displayName = structure.getDisplayName();
        if (structure.getPlayerListName() != null)
            player.listName = new ChatComponentText(structure.getPlayerListName());
        if (structure.getPlayerListHeader() != null)
            player.getBukkitEntity().setPlayerListHeader(structure.getPlayerListHeader());
        if (structure.getPlayerListFooter() != null)
            player.getBukkitEntity().setPlayerListFooter(structure.getPlayerListFooter());
        if (structure.getCompassTarget() != null)
            player.compassTarget = player.getBukkitEntity().getLocation();
        if (structure.getBedSpawnLocation() != null)
        {
            Location loc = structure.getBedSpawnLocation().create();
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
        if (structure.getExp() != null)
            player.giveExp(structure.getExp());
        if (structure.getLevel() != null)
            player.expLevel = structure.getLevel();
        if (structure.getTotalExperience() != null)
            player.expTotal = structure.getTotalExperience();
        if (Boolean.TRUE.equals(structure.getFlying()))
            player.abilities.isFlying = structure.getFlying();
        if (structure.getWalkSpeed() != null)
            player.abilities.walkSpeed = structure.getWalkSpeed();
        if (structure.getFlySpeed() != null)
            player.abilities.flySpeed = structure.getFlySpeed();

        int opLevel = structure.getOpLevel() == null ? this.settings.getDefaultOPLevel(): structure.getOpLevel();
        boolean isOP = opLevel > 0;
        if (isOP)
        {
            OpListEntry entry = new OpListEntry(
                    player.getProfile(),
                    opLevel,
                    true  // bypassPlayerLimit
            );

            player.server.getPlayerList().getOPs().add(entry);
        }

        // permissions

        Stream.of(structure.getActivePermissions(), this.settings.getDefaultPermissions())
                .flatMap(List::stream)
                .distinct()
                .forEach(permission -> player.getBukkitEntity().addAttachment(this.registry.getPlugin(), permission, true));
    }

    @SuppressWarnings("deprecation")
    private void initEntity(MockedPlayer player, PlayerStructure structure)
    {
        if (structure.getLocation() != null)
        {
            Location loc = structure.getLocation().create();
            player.setLocation(
                    loc.getX(), loc.getY(), loc.getZ(),
                    loc.getYaw(), loc.getPitch()
            );
            if (loc.getWorld() != null)
            {
                World world = Bukkit.getWorld(loc.getWorld().getName());
                if (world == null)
                    throw new IllegalArgumentException("World not found: " + loc.getWorld().getName());

                player.spawnIn(((CraftWorld) world).getHandle());
            }
        }
        if (structure.getCustomName() != null)
            player.setCustomName(new ChatComponentText(structure.getCustomName()));
        if (Boolean.TRUE.equals(structure.getGlowing()))
            player.glowing = true;
        if (Boolean.TRUE.equals(structure.getGravity()))
            player.setNoGravity(false);

        Stream.of(structure.getTags(), this.settings.getDefaultScoreboardTags())
                .flatMap(List::stream)
                .distinct()
                .forEach(player::addScoreboardTag);

        if (structure.getMaxHealth() != null)
            Objects.requireNonNull(player.getAttributeInstance(GenericAttributes.MAX_HEALTH))
                    .setValue(structure.getMaxHealth());
        if (structure.getHealth() != null)
            player.setHealth(structure.getHealth());
        for (PotionEffect effect : structure.getPotionEffects())
            player.addEffect(new MobEffect(
                    MobEffectList.fromId(effect.getType().getId()),  // deprecated
                    effect.getDuration(),
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.hasParticles(),
                    effect.hasIcon()
            ));
        if (structure.getLastDamageCause() != null)
            player.getBukkitEntity().setLastDamageCause(new EntityDamageEvent(
                    player.getBukkitEntity(),
                    structure.getLastDamageCause().getCause(),
                    structure.getLastDamageCause().getDamage()
            ));
    }

    private void initHumanEntity(MockedPlayer player, PlayerStructure structure)
    {
        if (structure.getInventory() != null)
        {
            PlayerInventoryStructure inventory = structure.getInventory();
            for (Map.Entry<Integer, ItemStackStructure> entry : inventory.getMainContents().entrySet())
                player.inventory.setItem(entry.getKey(), CraftItemStack.asNMSCopy(entry.getValue().create()));

            if (inventory.getArmorContents() != null)
                for (int i = 0; i < 4; i++)
                {
                    ItemStackStructure item = inventory.getArmorContents()[i];
                    if (item != null)
                    {
                        int slot = 3 - i;  // 直感的に, 0 がヘルメットになるように逆順にする。
                        player.inventory.armor.set(slot, CraftItemStack.asNMSCopy(item.create()));
                    }
                }

            if (inventory.getOffHand() != null)
                player.getBukkitEntity().getInventory().setItemInOffHand(inventory.getOffHand().create());
            if (inventory.getMainHand() != null)
                player.getBukkitEntity().getInventory().setItemInMainHand(inventory.getMainHand().create());
        }
        if (player.getBukkitEntity().getGameMode() != structure.getGamemode())
            player.playerInteractManager.setGameMode(EnumGamemode.a(structure.getGamemode().name().toLowerCase(Locale.ROOT)));
        if (structure.getFoodLevel() != null)
            player.getFoodData().foodLevel = structure.getFoodLevel();
    }

    @Override
    @NotNull
    public Actor mock(@NotNull World world, @NotNull PlayerStructure structure)
    {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        NetworkManager mockedNetworkManager = new MockedNetworkManager(server, structure);
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        GameProfile profile = createGameProfile(structure);
        boolean doLogin = structure.getOnline() == null || structure.getOnline();

        Location initialLocation;
        if (structure.getLocation() != null)
            initialLocation = structure.getLocation().create().clone();
        else
            initialLocation = world.getSpawnLocation().clone();

        if (structure.getLocation() != null && structure.getLocation().getWorld() == null)
            initialLocation.setWorld(world);

        MockedPlayer player = new MockedPlayer(
                this.manager, this, mockedNetworkManager, server, worldServer,
                profile, initialLocation, structure
        );
        this.initializePlayer(player, structure);

        if (doLogin)
            this.doLogin(server, player);

        return player;
    }

    /* non-public */ void doLogin(MinecraftServer server, MockedPlayer player)
    {
        if (!this.dispatchLoginEvent(player, (InetSocketAddress) player.getNetworkManager().getSocketAddress()))
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

        if (mockedPlayer.playerConnection != null)
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

        Location initialLocation = mockedPlayer.getInitialLocation();
        WorldServer world = ((CraftWorld) initialLocation.getWorld()).getHandle();
        float yaw = mockedPlayer.yaw;
        float pitch = mockedPlayer.pitch;
        double x = initialLocation.getX();
        double y = initialLocation.getY();
        double z = initialLocation.getZ();

        if (world != mockedPlayer.getWorldServer())
        {
            PlayerList list = ((CraftServer) Bukkit.getServer()).getHandle();

            Runner.run(() -> {  // Removing entity while ticking! 回避
                list.moveToWorld(
                        mockedPlayer,
                        world,
                        true,
                        new Location(world.getWorld(), x, y, z, yaw, pitch),
                        true
                );
            });
        }
        else
            mockedPlayer.getPlayer().teleport(new Location(world.getWorld(), x, y, z, yaw, pitch));
    }

}
