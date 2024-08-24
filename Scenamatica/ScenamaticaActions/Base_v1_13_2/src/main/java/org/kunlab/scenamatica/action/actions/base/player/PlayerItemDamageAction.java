package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Action("player_item_damage")
@ActionDoc(
        name = "プレイヤによるアイテムのダメージ",
        description = "プレイヤのアイテムにダメージを与えます。",
        events = {
                PlayerItemDamageEvent.class
        },

        executable = "プレイヤが所持しているアイテムにダメージを与えます。",
        expectable = "プレイヤが所持しているアイテムにダメージが与えることを期待します。",
        requireable = "プレイヤが所持しているアイテムのダメージが一致することを要求します。",

        outputs = {
                @OutputDoc(
                        name = PlayerItemDamageAction.KEY_OUT_ITEM,
                        description = "ダメージを与えられたアイテムです。",
                        type = ItemStack.class
                ),
                @OutputDoc(
                        name = PlayerItemDamageAction.KEY_OUT_DAMAGE,
                        description = "与えられたダメージです。",
                        type = int.class,
                        min = 0
                )
        }
)
public class PlayerItemDamageAction extends AbstractPlayerAction
        implements Executable, Expectable, Requireable
{
    @InputDoc(
            name = "item",
            description = "ダメージを与えるアイテムを指定します。",
            type = ItemStack.class
    )
    public static final InputToken<ItemStackStructure> IN_ITEM = ofInput(
            "item",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );
    @InputDoc(
            name = "damage",
            description = "与えるダメージを指定します。",
            type = int.class,
            min = 0
    )
    public static final InputToken<Integer> IN_DAMAGE = ofInput(
            "damage",
            Integer.class
    );
    @InputDoc(
            name = "slot",
            description = "ダメージを与えるアイテムのスロットを指定します。",
            type = EquipmentSlot.class
    )
    public static final InputToken<EquipmentSlot> IN_SLOT = ofEnumInput(
            "slot",
            EquipmentSlot.class
    );
    public static final String KEY_OUT_ITEM = "item";
    public static final String KEY_OUT_DAMAGE = "damage";

    private static ItemStack getDamagedItem(@NotNull ItemStack item, int damage)
    {
        ItemStack newItem = item.clone();
        ItemMeta meta = newItem.getItemMeta();
        if (!(meta instanceof Damageable))
            throw new IllegalStateException("Target item is not Damageable");
        Damageable damageable = (Damageable) meta;
        damageable.setDamage(damage);
        newItem.setItemMeta(meta);
        return newItem;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        EquipmentSlot slot = ctxt.orElseInput(IN_SLOT, () -> EquipmentSlot.HAND);

        ctxt.runIfHasInput(IN_ITEM, item -> player.getInventory().setItem(slot, item.create()));

        ItemStack itemStack = player.getInventory().getItem(slot);
        if (itemStack == null)
            throw new IllegalStateException("Target does not have item in slot " + slot);
        else if (!(itemStack.getItemMeta() instanceof Damageable))
            throw new IllegalStateException("Target item in slot " + slot + " is not Damageable");
        int damage = ctxt.input(IN_DAMAGE);

        this.makeOutputs(ctxt, player, getDamagedItem(itemStack, damage), damage);

        NMSEntityPlayer nmsPlayer = NMSProvider.getProvider().wrap(player);
        NMSItemStack nmsStack = NMSProvider.getProvider().wrap(itemStack);
        nmsStack.damage(damage, nmsPlayer);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerItemDamageEvent;
        PlayerItemDamageEvent e = (PlayerItemDamageEvent) event;

        boolean result = ctxt.ifHasInput(IN_ITEM, item -> item.isAdequate(e.getItem()))
                && ctxt.ifHasInput(IN_DAMAGE, damage -> damage == e.getDamage());
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getItem(), e.getDamage());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull ItemStack item, int damage)
    {
        ctxt.output(KEY_OUT_ITEM, item);
        ctxt.output(KEY_OUT_DAMAGE, damage);
        super.makeOutputs(ctxt, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerItemDamageEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);

        List<EquipmentSlot> slotToCheck;
        if (ctxt.hasInput(IN_SLOT))
            slotToCheck = Collections.singletonList(ctxt.input(IN_SLOT));
        else
            slotToCheck = Arrays.asList(EquipmentSlot.values());

        int expectedDamage = ctxt.input(IN_DAMAGE);
        for (EquipmentSlot slot : slotToCheck)
        {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null)
                continue;

            ItemMeta meta = item.getItemMeta();
            if (!(meta instanceof Damageable))
                continue;
            Damageable damageable = (Damageable) meta;

            if (!ctxt.ifHasInput(IN_ITEM, itemStructure -> itemStructure.isAdequate(item)))
                continue;

            if (expectedDamage == damageable.getDamage())
            {
                this.makeOutputs(ctxt, player, item, expectedDamage);
                return true;
            }
        }

        return false;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_ITEM, IN_DAMAGE, IN_SLOT);
        if (type == ScenarioType.CONDITION_REQUIRE || type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_DAMAGE);

        return board;
    }
}
