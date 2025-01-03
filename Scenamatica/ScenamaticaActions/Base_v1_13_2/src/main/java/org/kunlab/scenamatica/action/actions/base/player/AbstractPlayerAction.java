package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.AbstractAction;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;

@Category(
        id = "players",
        name = "プレイヤ",
        description = "プレイヤに関するアクションを提供します。"
)
@OutputDocs({
        @OutputDoc(
                name = AbstractPlayerAction.KEY_OUT_TARGET,
                description = "対象のプレイヤです。",
                type = Player.class
        )
})
public abstract class AbstractPlayerAction extends AbstractAction
{
    @InputDoc(
            name = "target",
            description = "対象のプレイヤです。",
            type = PlayerSpecifier.class,
            requiredOn = ActionMethod.EXECUTE
    )
    public static final InputToken<PlayerSpecifier> IN_TARGET = ofInput(
            "target",
            PlayerSpecifier.class,
            ofPlayer()
    );

    public static final String KEY_OUT_TARGET = "target";

    public static Player selectTarget(@NotNull ActionContext ctxt)
    {
        return ctxt.input(IN_TARGET).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalActionInputException(IN_TARGET, "Cannot select target for this action, please specify the target with valid specifier."));
    }

    public boolean checkMatchedPlayerEvent(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof PlayerEvent))
            return false;
        PlayerEvent e = (PlayerEvent) event;
        Player player = e.getPlayer();

        return ctxt.ifHasInput(IN_TARGET, target -> target.checkMatchedPlayer(player));
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player)
    {
        ctxt.output(KEY_OUT_TARGET, player);
        ctxt.commitOutput();
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_TARGET);
        if (type == ScenarioType.ACTION_EXECUTE)
            board = board.requirePresent(IN_TARGET);
        return board;
    }
}
