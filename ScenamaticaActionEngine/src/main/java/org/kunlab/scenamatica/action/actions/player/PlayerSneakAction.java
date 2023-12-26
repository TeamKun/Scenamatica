package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSneakEvent;
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

public class PlayerSneakAction extends AbstractPlayerAction
        implements Executable, Watchable, Requireable
{
    public static final String KEY_ACTION_NAME = "player_sneak";
    public static final InputToken<Boolean> IN_SNEAKING = ofInput(
            "sneaking",
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
        boolean sneaking = argument.get(IN_SNEAKING);

        Player player = selectTarget(argument, engine);
        // Player#setSneaking は PlayerToggleSneakEvent を呼び出さないので、以下手動で呼び出す。
        PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(player, sneaking);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        player.setSneaking(sneaking);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerToggleSneakEvent;
        PlayerToggleSneakEvent e = (PlayerToggleSneakEvent) event;

        return argument.ifPresent(IN_SNEAKING, sneaking -> sneaking == e.isSneaking());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerToggleSneakEvent.class
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        Player player = selectTarget(argument, engine);
        return argument.ifPresent(IN_SNEAKING, sneaking -> sneaking == player.isSneaking());
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_SNEAKING);
        if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
            board.requirePresent(IN_SNEAKING);

        return board;
    }
}
