package org.kunlab.scenamatica.context.actor.nms.v1_17_R1;

import io.netty.buffer.ByteBufAllocator;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayInKeepAlive;
import net.minecraft.network.protocol.game.PacketPlayInWindowClick;
import net.minecraft.network.protocol.game.PacketPlayOutChat;
import net.minecraft.network.protocol.game.PacketPlayOutHeldItemSlot;
import net.minecraft.network.protocol.game.PacketPlayOutKeepAlive;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.network.protocol.game.PacketPlayOutWindowItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.kunlab.scenamatica.events.actor.ActorMessageReceiveEvent;

import java.io.IOException;
import java.lang.reflect.Field;

class MockedPlayerConnection extends PlayerConnection
{
    private static final Field fChatComponent; // Lnet/minecraft/server/v1_16_R3/PacketPlayOutChat;a:Lnet/minecraft/network/chat/IChatBaseComponent;

    @Getter
    private int windowStateId;
    
    static
    {
        try
        {
            fChatComponent = PacketPlayOutChat.class.getDeclaredField("a");
            fChatComponent.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    public MockedPlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer)
    {
        super(minecraftserver, networkmanager, entityplayer);
    }

    private static ActorMessageReceiveEvent.Type getEventType(ChatMessageType type)
    {
        switch (type)
        {
            case a:
                return ActorMessageReceiveEvent.Type.PLAYER;
            case c:
                return ActorMessageReceiveEvent.Type.GAME_INFO;
            case b:
                /* fall through */
            default:
                return ActorMessageReceiveEvent.Type.SYSTEM;
        }
    }

    @Override
    public void tick()
    {
        super.tick();
    }

    @Override
    public void sendPacket(Packet<?> packet)
    {
        if (packet instanceof PacketPlayOutKeepAlive)
            this.handleKeepAlive((PacketPlayOutKeepAlive) packet);
        else if (packet instanceof PacketPlayOutChat)
            this.handleChat((PacketPlayOutChat) packet);
        else if (packet instanceof PacketPlayOutHeldItemSlot)
            this.handleHeldItemSlot();
        else if (packet instanceof PacketPlayOutSetSlot)
            this.handleSetSlot((PacketPlayOutSetSlot) packet);
        else if (packet instanceof PacketPlayOutWindowItems)
            this.handleWindowItem((PacketPlayOutWindowItems) packet);
        else
            super.sendPacket(packet);
    }

    private void handleSetSlot(PacketPlayOutSetSlot packet)
    {
        this.windowStateId = packet.e();
    }

    private void handleWindowItem(PacketPlayOutWindowItems packet)
    {
        this.windowStateId = packet.e();
    }


    private void handleHeldItemSlot()
    {
        int slot = this.b.getInventory().k;
        PlayerItemHeldEvent event = new PlayerItemHeldEvent(
                this.b.getBukkitEntity(),
                slot,
                slot
        );

        Bukkit.getPluginManager().callEvent(event);
    }

    private void handleKeepAlive(PacketPlayOutKeepAlive packet)
    {
        long validationNumber = packet.b();
        PacketPlayInKeepAlive packetPlayInKeepAlive = new PacketPlayInKeepAlive(validationNumber);

        this.a(packetPlayInKeepAlive);
    }

    private void handleChat(PacketPlayOutChat packet)
    {
        ChatMessageType type = packet.c();
        BaseComponent[] components = packet.components;
        if (components == null)
        {
            try
            {
                IChatBaseComponent component = (IChatBaseComponent) fChatComponent.get(packet);
                components = new ComponentBuilder(CraftChatMessage.fromComponent(component)).create();
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }

            if (components[0] == null)
                return;
        }


        ActorMessageReceiveEvent event = new ActorMessageReceiveEvent(
                this.b.getBukkitEntity(),
                Text.of(components),
                getEventType(type)
        );

        // Event は async で呼び出すと、Bukkit の内部でエラーが発生する。
        // それだけなら良いが, Bukkit はチャット処理時のエラーは完全に握りつぶす仕様になっている。
        // そのため, これらの問題に幾らかのコストがかかる。
        // そもそも例外を握りつぶすとかいう行為は愚の骨頂というか, 現代のプログラミングの基本的な考え方とは反すると思う。
        // なぜこういう仕様になっているのかはわからないが, MC に30000行のパッチを1ファイルで送りつける気など
        // 毛頭ないのでここではこのままにしておく所存である。
        // というかせめて, 例外を握りつぶすときはログに出力してくれると嬉しい。
        // というか例外握りつぶすなよ。まじで。頼むから！！！
        // n 年前の, プレイヤのチャットをasyncで処理するとかいうコンセプトは大変よろしいと思うのだが,
        // そのへんで発生するイベントだけ async 許容するとかいうのはどうなんだと思う。
        // 実際こういうことが起きているわけで, せめてDocs書いてくれ...
        // NMS いじっている身で言えたことではないと思うけど。(´・ω・`)
        if (Bukkit.getServer().isPrimaryThread())
            Bukkit.getServer().getPluginManager().callEvent(event);
        else
            this.b.c.postToMainThread(() ->
                    Bukkit.getServer().getPluginManager().callEvent(event)
            );
    }

    @Override
    public void disconnect(String s)
    {
        assert this.b instanceof MockedPlayer;
        MockedPlayer player = (MockedPlayer) this.b;
        player.getManager().onDestroyActor(player);

        super.disconnect(s);
    }
}
