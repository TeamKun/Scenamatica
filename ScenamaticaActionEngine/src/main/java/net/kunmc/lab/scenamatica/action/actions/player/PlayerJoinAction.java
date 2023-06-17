package net.kunmc.lab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.action.utils.EntityUtils;
import net.kunmc.lab.scenamatica.action.utils.PlayerUtils;
import net.kunmc.lab.scenamatica.interfaces.action.Requireable;
import net.kunmc.lab.scenamatica.interfaces.context.Actor;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.BeanSerializer;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerJoinAction extends AbstractPlayerAction<PlayerJoinAction.Argument> implements Requireable<PlayerJoinAction.Argument>
{
    // OfflinePlayer を扱うため, 通常の PlayerAction とは違う実装をする。

    public static final String KEY_ACTION_NAME = "player_join";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        String targetSpecifier = argument.getTargetSpecifier();  // argument.getTarget() は必ず null になる

        if (PlayerUtils.getPlayerOrNull(targetSpecifier) != null)
            throw new IllegalArgumentException("Cannot execute player join action because player is already online.");

        Actor actor = EntityUtils.getActorByStringOrThrow(engine, targetSpecifier);

        actor.joinServer();
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        assert event instanceof PlayerJoinEvent;
        PlayerJoinEvent e = (PlayerJoinEvent) event;
        Player player = e.getPlayer();

        String targetSpecifier = argument.getTargetSpecifier();

        return player.getName().equalsIgnoreCase(targetSpecifier)
                || this.isSameUUIDString(player.getUniqueId().toString(), targetSpecifier);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerJoinEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(super.deserializeTarget(map));
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);
        return PlayerUtils.getPlayerOrNull(argument.getTargetSpecifier()) != null;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public Argument(@NotNull String target)
        {
            super(target);
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            return super.isSame(argument);
        }

        @Override
        public String getArgumentString()
        {
            return super.getArgumentString();
        }

        @Override
        @Nullable
        public Player getTarget()
        {
            return PlayerUtils.getPlayerOrNull(this.getTargetSpecifier());
        }
    }
}
