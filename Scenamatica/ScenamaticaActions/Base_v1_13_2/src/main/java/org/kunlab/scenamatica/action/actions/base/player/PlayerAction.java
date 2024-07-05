package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;

@Action("player")
@ActionDoc(
        name = "プレイヤ",
        description = "プレイヤの状態や振る舞い、属性を変更します。",

        executable = "プレイヤの状態や属性を変更します。",
        requireable = ActionDoc.UNALLOWED,
        watchable = "プレイヤの状態や属性が指定されたものと一致するかどうかを確認します。"
)
public class PlayerAction extends AbstractPlayerAction
        implements Executable, Requireable
{
    @InputDoc(
            name = "data",
            description = "変更するプレイヤのデータを指定します。",
            type = PlayerStructure.class
    )
    public static final InputToken<PlayerStructure> IN_PLAYER = ofInput(
            "data",
            PlayerStructure.class,
            ofDeserializer(PlayerStructure.class)
    );

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player target = selectTarget(ctxt);
        PlayerStructure playerInfo = ctxt.input(IN_PLAYER);

        this.makeOutputs(ctxt, target);
        playerInfo.applyTo(target);
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Player target = selectTarget(ctxt);

        boolean result = ctxt.ifHasInput(IN_PLAYER, player -> player.isAdequate(target));

        if (result)
            this.makeOutputs(ctxt, target);

        return result;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_PLAYER);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_PLAYER);

        return board;
    }
}
