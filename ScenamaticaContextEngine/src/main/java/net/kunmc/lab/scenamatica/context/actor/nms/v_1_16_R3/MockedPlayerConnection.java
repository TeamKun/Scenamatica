package net.kunmc.lab.scenamatica.context.actor.nms.v_1_16_R3;

import io.netty.buffer.ByteBufAllocator;
import lombok.SneakyThrows;
import net.kunmc.lab.scenamatica.events.actor.ActorMessageReceiveEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketDataSerializer;
import net.minecraft.server.v1_16_R3.PacketPlayInKeepAlive;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;
import net.minecraft.server.v1_16_R3.PacketPlayOutHeldItemSlot;
import net.minecraft.server.v1_16_R3.PacketPlayOutKeepAlive;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.io.IOException;
import java.lang.reflect.Field;

class MockedPlayerConnection extends PlayerConnection
{
    private static final Field fChatComponent; // Lnet/minecraft/server/v1_16_R3/PacketPlayOutChat;a:Lnet/minecraft/network/chat/IChatBaseComponent;

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
            case CHAT:
                return ActorMessageReceiveEvent.Type.PLAYER;
            case GAME_INFO:
                return ActorMessageReceiveEvent.Type.GAME_INFO;
            case SYSTEM:
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
        else
            super.sendPacket(packet);
    }

    private void handleHeldItemSlot()
    {
        int slot = this.player.inventory.itemInHandIndex;
        PlayerItemHeldEvent event = new PlayerItemHeldEvent(
                this.player.getBukkitEntity(),
                slot,
                slot
        );

        Bukkit.getPluginManager().callEvent(event);
    }

    @SneakyThrows(IOException.class)
    private void handleKeepAlive(PacketPlayOutKeepAlive packet)
    {
        PacketDataSerializer buf = new PacketDataSerializer(ByteBufAllocator.DEFAULT.buffer());
        packet.b(buf);
        PacketPlayInKeepAlive packetPlayInKeepAlive = new PacketPlayInKeepAlive();
        packetPlayInKeepAlive.a(buf);

        this.a(packetPlayInKeepAlive);
    }

    private void handleChat(PacketPlayOutChat packet)
    {
        ChatMessageType type = packet.d();
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

        TextComponent textComponent = new TextComponent(components);

        ActorMessageReceiveEvent event = new ActorMessageReceiveEvent(
                this.player.getBukkitEntity(),
                textComponent,
                getEventType(type)
        );

        assert this.player.getMinecraftServer() != null;

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
            this.player.getMinecraftServer().server.getPluginManager().callEvent(event);
        else
            this.player.server.postToMainThread(() ->
                    this.player.getMinecraftServer().server.getPluginManager().callEvent(event)
            );
    }

    @Override
    public void disconnect(String s)
    {
        assert this.player instanceof MockedPlayer;
        MockedPlayer player = (MockedPlayer) this.player;
        player.getManager().onDestroyActor(player);

        super.disconnect(s);
    }
}
