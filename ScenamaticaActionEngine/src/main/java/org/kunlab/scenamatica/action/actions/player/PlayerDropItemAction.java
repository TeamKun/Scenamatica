package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.StructureUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerDropItemAction extends AbstractPlayerAction<PlayerDropItemAction.Argument>
        implements Executable<PlayerDropItemAction.Argument>, Watchable<PlayerDropItemAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_drop_item";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);
        EntityItemStructure item = argument.getItem();

        // item がしてある場合は、先に手に持たせる。
        if (item != null)
            argument.getTarget().getInventory().setItemInMainHand(item.getItemStack().toItemStack());

        argument.getTarget().dropItem(/* dropAll: */ false);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerDropItemEvent;
        PlayerDropItemEvent e = (PlayerDropItemEvent) event;
        EntityItemStructure item = argument.getItem();

        return item == null || StructureUtils.isSame(
                item.getItemStack(),
                e.getItemDrop().getItemStack(),
                /* strict: */ true
        );
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerDropItemEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        EntityItemStructure item = null;
        if (map.containsKey(Argument.KEY_ITEM))
            item = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(Argument.KEY_ITEM)),
                    EntityItemStructure.class
            );


        return new Argument(
                super.deserializeTarget(map),
                item
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_ITEM = "item";

        @Nullable
        EntityItemStructure item;

        public Argument(String target, @Nullable EntityItemStructure item)
        {
            super(target);
            this.item = item;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg) &&
                    Objects.equals(this.item, arg.item);
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_ITEM, this.item
            );
        }
    }
}
