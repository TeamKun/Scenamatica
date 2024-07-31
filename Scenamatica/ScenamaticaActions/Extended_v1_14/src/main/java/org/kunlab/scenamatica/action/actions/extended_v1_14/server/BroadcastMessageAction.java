package org.kunlab.scenamatica.action.actions.extended_v1_14.server;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

import java.util.Set;

@Action(value = "broadcast", supportsSince = MinecraftVersion.V1_14)
public class BroadcastMessageAction extends org.kunlab.scenamatica.action.actions.base.server.BroadcastMessageAction
        implements Executable, Expectable
{
    @Override
    protected BroadcastMessageEvent createEvent(@NotNull String message, @NotNull Set<CommandSender> recipients)
    {
        return new BroadcastMessageEvent(!Bukkit.isPrimaryThread(), message, recipients);
    }
}
