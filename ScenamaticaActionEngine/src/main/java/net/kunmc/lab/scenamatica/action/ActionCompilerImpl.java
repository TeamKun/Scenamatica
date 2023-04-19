package net.kunmc.lab.scenamatica.action;

import net.kunmc.lab.scenamatica.action.actions.chat.MessageSendAction;
import net.kunmc.lab.scenamatica.action.actions.player.GameModeAction;
import net.kunmc.lab.scenamatica.action.actions.player.PlayerDeathAction;
import net.kunmc.lab.scenamatica.action.actions.player.PlayerLaunchProjectileAction;
import net.kunmc.lab.scenamatica.action.actions.server.CommandDispatchAction;
import net.kunmc.lab.scenamatica.action.actions.server.log.ServerLogAction;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionCompiler;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ActionCompilerImpl implements ActionCompiler
{
    private static final List<Action<?>> ACTIONS;

    static
    {
        ACTIONS = new ArrayList<>();
        ACTIONS.add(new MessageSendAction());

        // プレイヤーのアクション
        {
            ACTIONS.add(new GameModeAction());
            ACTIONS.add(new PlayerDeathAction());
            ACTIONS.add(new PlayerLaunchProjectileAction());
        }

        // サーバーのアクション
        {
            ACTIONS.add(new ServerLogAction());
            ACTIONS.add(new CommandDispatchAction());
        }
    }

    @Override
    public <A extends ActionArgument> CompiledAction<A> compile(@NotNull ScenamaticaRegistry registry,
                                                                @NotNull ActionBean bean,
                                                                @Nullable BiConsumer<CompiledAction<A>, Throwable> reportErrorTo,
                                                                @Nullable Consumer<CompiledAction<A>> onSuccess)
    {
        Action<A> action = null;
        for (Action<?> a : ACTIONS)
        {
            if (a.getName().equals(bean.getType()))
            {
                //noinspection unchecked
                action = (Action<A>) a;
                break;
            }
        }

        if (action == null)
            throw new IllegalArgumentException("Action " + bean.getType() + " is not found.");

        A argument = null;
        if (bean.getArguments() != null)
            argument = action.deserializeArgument(bean.getArguments());

        return new CompiledActionImpl<>(action, argument, reportErrorTo, onSuccess);
    }
}
