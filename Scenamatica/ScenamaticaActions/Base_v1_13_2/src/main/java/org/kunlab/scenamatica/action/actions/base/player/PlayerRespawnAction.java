package org.kunlab.scenamatica.action.actions.base.player;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;

import java.util.Arrays;
import java.util.List;

@Action("player_respawn")
@ActionDoc(
        name = "プレイヤのリスポーン",
        description = "プレイヤをリスポーンさせます。",
        events = {
                PlayerRespawnEvent.class,
                PlayerPostRespawnEvent.class
        },

        executable = "プレイヤをリスポーンさせます。",
        expectable = "プレイヤがリスポーンすることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = PlayerRespawnAction.KEY_OUT_IS_BED,
                        description = "プレイヤがベッドでリスポーンしたかどうかです。",
                        type = boolean.class
                ),
                @OutputDoc(
                        name = PlayerRespawnAction.KEY_OUT_LOCATION,
                        description = "プレイヤがリスポーンする場所です。",
                        type = Location.class
                )
        }

)
public class PlayerRespawnAction extends AbstractPlayerAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "isBed",
            description = "プレイヤがベッドでリスポーンするかどうかを指定します。",
            type = boolean.class,
            availableFor = ActionMethod.EXPECT
    )
    public static final InputToken<Boolean> IN_IS_BED = ofInput(
            "isBed",
            Boolean.class
    );
    @InputDoc(
            name = "location",
            description = "プレイヤがリスポーンする場所を指定します。",
            type = Location.class
    )
    public static final InputToken<LocationStructure> IN_LOCATION = ofInput(
            "location",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );
    public static final String KEY_OUT_IS_BED = "isBed";
    public static final String KEY_OUT_LOCATION = "location";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        if (!player.isDead())
            throw new IllegalStateException("Player is not dead");

        if (ctxt.hasInput(IN_LOCATION))
            player.setBedSpawnLocation(Utils.assignWorldToLocation(ctxt.input(IN_LOCATION), ctxt.getEngine()), true);

        super.makeOutputs(ctxt, player);
        player.spigot().respawn();
    }

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
                    && ctxt.ifHasInput(IN_LOCATION, loc -> loc.isAdequate(e.getRespawnLocation()));
            if (result)
                this.makeOutputs(ctxt, e.getPlayer(), e.isBedSpawn(), e.getRespawnLocation());
        }
        else
        {
            assert event instanceof PlayerPostRespawnEvent;
            PlayerPostRespawnEvent e = (PlayerPostRespawnEvent) event;

            result = ctxt.ifHasInput(IN_IS_BED, isBed -> isBed == e.isBedSpawn())
                    && ctxt.ifHasInput(IN_LOCATION, loc -> loc.isAdequate(e.getRespawnedLocation()));
            if (result)
                this.makeOutputs(ctxt, e.getPlayer(), e.isBedSpawn(), e.getRespawnedLocation());
        }

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, boolean isBed, @NotNull Location location)
    {
        ctxt.output(KEY_OUT_IS_BED, isBed);
        ctxt.output(KEY_OUT_LOCATION, location);
        super.makeOutputs(ctxt, player);
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull Location location)
    {
        ctxt.output(KEY_OUT_LOCATION, location);
        super.makeOutputs(ctxt, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Arrays.asList(
                PlayerRespawnEvent.class,
                PlayerPostRespawnEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_LOCATION);
        if (type != ScenarioType.ACTION_EXECUTE)
            board.registerAll(IN_IS_BED);

        return board;
    }
}
