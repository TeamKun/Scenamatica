package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.BeanUtils;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerItemConsumeAction extends AbstractPlayerAction<PlayerItemConsumeAction.Argument>
        implements Watchable<PlayerItemConsumeAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_item_consume";

    private static boolean isConsumable(@NotNull ItemStack item)
    {
        Material type = item.getType();
        return type.isEdible() || type == Material.POTION || type == Material.MILK_BUCKET;
    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        ItemStackBean item = argument.getItem();
        Actor actor = PlayerUtils.getActorOrThrow(engine, argument.getTarget());

        if (item != null)
            actor.getPlayer().getInventory().setItemInMainHand(item.toItemStack());
        else if (!isConsumable(actor.getPlayer().getInventory().getItemInMainHand()))
            throw new IllegalArgumentException("The item in the main hand is not consumable.");

        // 食べ初めをトリガするので、シナリオタイムアウトになるかもしれない。
        actor.consume(EquipmentSlot.HAND);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerItemConsumeEvent;
        PlayerItemConsumeEvent e = (PlayerItemConsumeEvent) event;

        ItemStack item = e.getItem();
        ItemStack replacement = e.getReplacement();

        return (argument.item == null || BeanUtils.isSame(argument.getItem(), item, false))
                && (argument.replacement == null || BeanUtils.isSame(argument.getReplacement(), replacement, false));
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        if (type != ScenarioType.ACTION_EXECUTE)
            return;

        this.throwIfPresent(Argument.KEY_REPLACEMENT, argument.getReplacement());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerItemConsumeEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        ItemStackBean item = null;
        if (map.containsKey(Argument.KEY_ITEM))
            item = serializer.deserializeItemStack(MapUtils.checkAndCastMap(
                    map.get(Argument.KEY_ITEM),
                    String.class,
                    Object.class
            ));

        ItemStackBean replacement = null;
        if (map.containsKey(Argument.KEY_REPLACEMENT))
            replacement = serializer.deserializeItemStack(MapUtils.checkAndCastMap(
                    map.get(Argument.KEY_REPLACEMENT),
                    String.class,
                    Object.class
            ));

        return new Argument(
                super.deserializeTarget(map),
                item,
                replacement
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_ITEM = "item";
        public static final String KEY_REPLACEMENT = "replacement";

        @Nullable
        ItemStackBean item;
        @Nullable
        ItemStackBean replacement;

        public Argument(@NotNull String target, @Nullable ItemStackBean item, @Nullable ItemStackBean replacement)
        {
            super(target);
            this.item = item;
            this.replacement = replacement;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;
            return super.isSame(arg)
                    && Objects.equals(this.item, arg.item)
                    && Objects.equals(this.replacement, arg.replacement);
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_ITEM, this.item,
                    KEY_REPLACEMENT, this.replacement
            );
        }
    }
}
