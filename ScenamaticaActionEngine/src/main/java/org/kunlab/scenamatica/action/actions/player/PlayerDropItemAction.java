package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemStructure;

import java.util.Collections;
import java.util.List;

public class PlayerDropItemAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_drop_item";
    public static final InputToken<EntityItemStructure> IN_ITEM = ofInput(
            "item",
            EntityItemStructure.class,
            ofDeserializer(EntityItemStructure.class)
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player target = selectTarget(ctxt);
        // item がしてある場合は、先に手に持たせる。
        ctxt.runIfHasInput(IN_ITEM, item -> target.getInventory().setItemInMainHand(item.getItemStack().create()));

        target.dropItem(/* dropAll: */ false);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerDropItemEvent;
        PlayerDropItemEvent e = (PlayerDropItemEvent) event;

        return ctxt.ifHasInput(IN_ITEM, item -> item.isAdequate(e.getItemDrop()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerDropItemEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .registerAll(IN_ITEM);
    }
}
