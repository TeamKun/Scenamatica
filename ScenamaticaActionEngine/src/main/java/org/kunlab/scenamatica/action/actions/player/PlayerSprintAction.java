package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Collections;
import java.util.List;

public class PlayerSprintAction extends AbstractPlayerAction
        implements Executable, Watchable, Requireable
{
    public static final String KEY_ACTION_NAME = "player_sprint";
    public static final InputToken<Boolean> IN_SPRINTING = ofInput(
            "sprinting",
            Boolean.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        boolean sprinting = argument.get(IN_SPRINTING);

        Player player = selectTarget(argument, engine);
        // Player#setSprinting は PlayerToggleSprintEvent を呼び出さないので、以下手動で呼び出す。
        PlayerToggleSprintEvent event = new PlayerToggleSprintEvent(player, sprinting);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        player.setSprinting(sprinting);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerToggleSprintEvent;
        PlayerToggleSprintEvent e = (PlayerToggleSprintEvent) event;

        return argument.ifPresent(IN_SPRINTING, sprinting -> sprinting == e.isSprinting());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerToggleSprintEvent.class
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        Player player = selectTarget(argument, engine);
        return argument.ifPresent(IN_SPRINTING, sprinting -> sprinting == player.isSprinting());
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_SPRINTING);
        if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
            board.requirePresent(IN_SPRINTING);

        return board;
    }
}
