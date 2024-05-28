package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

@Action("player_teleport")
@ActionDoc(
        name = "プレイヤのテレポート",
        description = "プレイヤを指定した場所にテレポートさせます。",
        events = {
                PlayerTeleportEvent.class
        },

        executable = "プレイヤを指定した場所にテレポートさせます。",
        watchable = "プレイヤがテレポートすることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = PlayerTeleportAction.KEY_OUT_CAUSE,
                        description = "テレポートの原因です。",
                        type = PlayerTeleportEvent.TeleportCause.class
                )
        }
)
public class PlayerTeleportAction extends PlayerMoveAction
        implements Executable, Watchable
{
    @InputDoc(
            name = "cacuse",
            description = "テレポートの原因を指定します。",
            type = PlayerTeleportEvent.TeleportCause.class
    )
    public static final InputToken<PlayerTeleportEvent.TeleportCause> IN_CAUSE = ofEnumInput(
            "cause",
            PlayerTeleportEvent.TeleportCause.class
    );
    public static final String KEY_OUT_CAUSE = "cause";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Location toLoc = Utils.assignWorldToLocation(ctxt.input(IN_TO), ctxt.getEngine());
        PlayerTeleportEvent.TeleportCause cause;
        if (ctxt.hasInput(IN_CAUSE))
            cause = ctxt.input(IN_CAUSE);
        else
            cause = PlayerTeleportEvent.TeleportCause.PLUGIN;

        Player player = selectTarget(ctxt);
        this.makeOutputs(ctxt, player, player.getLocation(), toLoc, cause);
        player.teleport(toLoc, cause);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkFired(ctxt, event))
            return false;

        assert event instanceof PlayerTeleportEvent;
        PlayerTeleportEvent e = (PlayerTeleportEvent) event;

        boolean result = ctxt.ifHasInput(IN_CAUSE, e.getCause()::equals);
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getFrom(), e.getTo(), e.getCause());

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull Location from,
                             @NotNull Location to, @NotNull PlayerTeleportEvent.TeleportCause cause)
    {
        ctxt.output(KEY_OUT_CAUSE, cause);
        super.makeOutputs(ctxt, player, from, to);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerTeleportEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .register(IN_CAUSE);
    }
}
