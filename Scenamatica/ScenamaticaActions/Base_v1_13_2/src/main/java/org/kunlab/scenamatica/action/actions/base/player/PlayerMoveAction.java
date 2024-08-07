package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
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

import java.util.Collections;
import java.util.List;

@Action("player_move")
@ActionDoc(
        name = "プレイヤの移動",
        description = "プレイヤを指定した場所に移動させます。",
        events = {
                PlayerMoveEvent.class
        },

        executable = "プレイヤを指定した場所に移動させます。",
        expectable = "プレイヤが指定した場所に移動することを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = PlayerMoveAction.KEY_OUT_FROM,
                        description = "プレイヤが移動する前の場所です。",
                        type = Location.class
                ),
                @OutputDoc(
                        name = PlayerMoveAction.KEY_OUT_TO,
                        description = "プレイヤが移動する後の場所です。",
                        type = Location.class
                )
        }

)
public class PlayerMoveAction extends AbstractPlayerAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "from",
            description = "プレイヤが移動する前の場所を指定します。",
            type = Location.class,
            availableFor = ActionMethod.EXECUTE
    )
    public static final InputToken<LocationStructure> IN_FROM = ofInput(
            "from",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );
    @InputDoc(
            name = "to",
            description = "プレイヤが移動する後の場所を指定します。",
            type = Location.class,
            availableFor = ActionMethod.EXECUTE
    )
    public static final InputToken<LocationStructure> IN_TO = ofInput(
            "to",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );
    public static final String KEY_OUT_FROM = "from";
    public static final String KEY_OUT_TO = "to";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Location toLoc = Utils.assignWorldToLocation(ctxt.input(IN_TO), ctxt.getEngine());
        Player target = selectTarget(ctxt);
        this.makeOutputs(ctxt, target, target.getLocation(), toLoc);

        target.teleport(toLoc);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerMoveEvent;
        PlayerMoveEvent e = (PlayerMoveEvent) event;

        boolean result = ctxt.ifHasInput(IN_FROM, from -> from.isAdequate(e.getFrom()))
                && ctxt.ifHasInput(IN_TO, to -> to.isAdequate(e.getTo()));
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getFrom(), e.getTo());

        return result;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerMoveEvent.class
        );
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull Location from, @NotNull Location to)
    {
        ctxt.output(KEY_OUT_FROM, from);
        ctxt.output(KEY_OUT_TO, to);

        super.makeOutputs(ctxt, player);
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_TO);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_TO);
        else
            board.register(IN_FROM);

        return board;
    }
}
