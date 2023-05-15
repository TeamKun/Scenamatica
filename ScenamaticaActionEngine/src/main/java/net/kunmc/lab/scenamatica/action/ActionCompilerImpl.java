package net.kunmc.lab.scenamatica.action;

import net.kunmc.lab.scenamatica.action.actions.player.*;
import net.kunmc.lab.scenamatica.action.actions.scenamatica.*;
import net.kunmc.lab.scenamatica.action.actions.server.*;
import net.kunmc.lab.scenamatica.action.actions.server.log.*;
import net.kunmc.lab.scenamatica.action.actions.server.plugin.*;
import net.kunmc.lab.scenamatica.action.actions.world.*;
import net.kunmc.lab.scenamatica.action.actions.world.border.*;
import net.kunmc.lab.scenamatica.interfaces.*;
import net.kunmc.lab.scenamatica.interfaces.action.*;
import net.kunmc.lab.scenamatica.interfaces.scenario.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.*;

public class ActionCompilerImpl implements ActionCompiler
{
    private static final List<Action<?>> ACTIONS;

    static
    {
        ACTIONS = new ArrayList<>();

        // プレイヤーのアクション
        {
            ACTIONS.add(new PlayerGameModeAction());
            ACTIONS.add(new PlayerAdvancementAction());
            ACTIONS.add(new PlayerAnimationAction());
            ACTIONS.add(new PlayerDeathAction());
            ACTIONS.add(new PlayerHotbarSlotAction());
            ACTIONS.add(new PlayerInteractBlockAction());
            ACTIONS.add(new PlayerLaunchProjectileAction());
        }

        // サーバーのアクション
        {
            ACTIONS.add(new ServerLogAction());
            ACTIONS.add(new PluginDisableAction());
            ACTIONS.add(new PluginEnableAction());
            ACTIONS.add(new CommandDispatchAction());
            ACTIONS.add(new WhitelistToggleAction());
        }

        // ワールドのアクション
        {
            ACTIONS.add(new WorldBorderAction());
            ACTIONS.add(new WorldBorderChangedAction());
            ACTIONS.add(new WorldGameRuleAction());
            ACTIONS.add(new WorldInitAction());
            ACTIONS.add(new WorldLoadAction());
            ACTIONS.add(new WorldSaveAction());
            ACTIONS.add(new WorldUnloadAction());
        }

        // Scenamatica 内部
        {
            ACTIONS.add(new MessageAction());
            ACTIONS.add(new MilestoneAction());
        }
    }

    @Override
    public <A extends ActionArgument> CompiledAction<A> compile(@NotNull ScenamaticaRegistry registry,
                                                                @NotNull ScenarioEngine engine,
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

        return new CompiledActionImpl<>(engine, action, argument, reportErrorTo, onSuccess, bean);
    }
}
