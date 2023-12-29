package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.actions.player.bucket.AbstractPlayerBucketAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlayerAction extends AbstractAction
        implements Watchable
{
    public static final InputToken<PlayerSpecifier> IN_TARGET = ofInput(
            "target",
            PlayerSpecifier.class,
            ofPlayer()
    );

    public static List<? extends AbstractPlayerAction> getActions()
    {
        List<AbstractPlayerAction> actions = new ArrayList<>(AbstractPlayerBucketAction.getActions());
        actions.add(new PlayerAdvancementAction());
        actions.add(new PlayerAnimationAction());
        actions.add(new PlayerBucketEntityAction());
        actions.add(new PlayerChatAction());
        actions.add(new PlayerDeathAction());
        actions.add(new PlayerDropItemAction());
        actions.add(new PlayerFlightAction());
        actions.add(new PlayerGameModeAction());
        actions.add(new PlayerHarvestBlockAction());
        actions.add(new PlayerHotbarSlotAction());
        actions.add(new PlayerInteractAtEntityAction());
        actions.add(new PlayerInteractBlockAction());
        actions.add(new PlayerInteractEntityAction());
        actions.add(new PlayerItemBreakAction());
        actions.add(new PlayerItemConsumeAction());
        actions.add(new PlayerItemDamageAction());
        actions.add(new PlayerJoinAction());
        actions.add(new PlayerKickAction());
        actions.add(new PlayerLaunchProjectileAction());
        actions.add(new PlayerLevelChangeAction());
        actions.add(new PlayerMoveAction());
        actions.add(new PlayerQuitAction());
        actions.add(new PlayerRespawnAction());
        actions.add(new PlayerSneakAction());
        actions.add(new PlayerSprintAction());
        actions.add(new PlayerTeleportAction());

        return actions;
    }

    public static Player selectTarget(@NotNull ActionContext ctxt)
    {
        return ctxt.input(IN_TARGET).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalArgumentException("Cannot select target for this action, please specify the target with valid specifier."));
    }

    public boolean checkMatchedPlayerEvent(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof PlayerEvent))
            return false;
        PlayerEvent e = (PlayerEvent) event;
        Player player = e.getPlayer();

        return ctxt.ifHasInput(IN_TARGET, target -> target.checkMatchedPlayer(player));
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
