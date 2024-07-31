package org.kunlab.scenamatica.action.actions.extended_v1_16.player;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

@Action(value = "player_respawn", supportsSince = MinecraftVersion.V1_16)
public class PlayerRespawnAction extends org.kunlab.scenamatica.action.actions.base.player.PlayerRespawnAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "isAnchor",
            description = "アンカーでスポーンするかどうかです。",
            type = boolean.class,
            admonitions = {
                    @Admonition(
                            type = AdmonitionType.DANGER,
                            content = "`PlayerPostRespawnEvent` では常に `false` になります。"
                    )
            }
    )
    public static final InputToken<Boolean> IN_IS_ANCHOR = ofInput(
            "isAnchor",
            Boolean.class
    );
    public static final String KEY_OUT_IS_ANCHOR = "isAnchor";

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        boolean result;
        if (event instanceof PlayerRespawnEvent)
        {
            PlayerRespawnEvent e = (PlayerRespawnEvent) event;

            result = ctxt.ifHasInput(IN_IS_BED, isBed -> isBed == e.isBedSpawn())
                    && ctxt.ifHasInput(IN_IS_ANCHOR, isAnchor -> isAnchor == e.isAnchorSpawn())
                    && ctxt.ifHasInput(IN_LOCATION, loc -> loc.isAdequate(e.getRespawnLocation()));
            if (result)
                this.makeOutputs(ctxt, e.getPlayer(), e.isBedSpawn(), e.isAnchorSpawn(), e.getRespawnLocation());
        }
        else
        {
            assert event instanceof PlayerPostRespawnEvent;
            PlayerPostRespawnEvent e = (PlayerPostRespawnEvent) event;

            result = ctxt.ifHasInput(IN_IS_BED, isBed -> isBed == e.isBedSpawn())
                    && ctxt.ifHasInput(IN_LOCATION, loc -> loc.isAdequate(e.getRespawnedLocation()));
            if (result)
                this.makeOutputs(ctxt, e.getPlayer(), e.isBedSpawn(), false, e.getRespawnedLocation());
        }

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, boolean isBed, boolean isAnchor, @NotNull Location location)
    {
        ctxt.output(KEY_OUT_IS_ANCHOR, isAnchor);
        super.makeOutputs(ctxt, player);
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .register(IN_IS_ANCHOR);
    }
}
