package org.kunlab.scenamatica.action.actions.player;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Objects;

@AllArgsConstructor
public abstract class AbstractPlayerActionArgument extends AbstractActionArgument
{
    public static final String KEY_TARGET_PLAYER = "target";

    private final PlayerSpecifier target;

    public PlayerSpecifier getTargetSpecifier()
    {
        return this.target;
    }

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof ActionArgument))
            return false;

        if (!AbstractPlayerActionArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        AbstractPlayerActionArgument a = (AbstractPlayerActionArgument) argument;
        return Objects.equals(this.target, a.target);
    }

    protected boolean isSameTarget(AbstractPlayerActionArgument argument)
    {
        return Objects.equals(this.target, argument.target);
    }

    @NotNull
    public Player getTarget(@NotNull ScenarioEngine engine)
    {
        this.ensureCanProvideTarget();
        Player player = this.getTargetOrNull(engine);
        if (player == null)
            throw new IllegalStateException("Cannot select target for this action, please specify target with valid specifier.");

        return player;
    }

    @Nullable
    public Player getTargetOrNull(@NotNull ScenarioEngine engine)
    {
        this.ensureCanProvideTarget();
        return this.target.selectTarget(engine.getContext())
                .orElse(null);
    }

    public boolean checkMatchedPlayer(@NotNull Player player)
    {
        this.ensureCanProvideTarget();
        return this.target.checkMatchedPlayer(player);
    }

    public boolean canProvideTarget()
    {
        return this.target != null && this.target.canProvideTarget();
    }

    public void ensureCanProvideTarget()
    {
        if (this.target == null || !this.target.canProvideTarget())
            throw new IllegalArgumentException("Cannot select target for this action, please specify target with valid specifier.");
    }

    @Override
    public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
    {
        if (type == ScenarioType.ACTION_EXECUTE)
            this.ensureCanProvideTarget();
    }

    @Override
    public String getArgumentString()
    {
        return buildArgumentString(
                KEY_TARGET_PLAYER, this.target
        );
    }
}
