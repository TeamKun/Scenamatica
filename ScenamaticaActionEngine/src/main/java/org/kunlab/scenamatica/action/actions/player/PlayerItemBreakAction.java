package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.enums.entity.NMSItemSlot;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;
import org.kunlab.scenamatica.nms.types.item.NMSItem;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

import java.util.Collections;
import java.util.List;

public class PlayerItemBreakAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_item_break";
    public static final InputToken<ItemStackStructure> IN_ITEM = ofInput(
            "item",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );
    public static final InputToken<EquipmentSlot> IN_SLOT = ofEnumInput(
            "slot",
            EquipmentSlot.class
    );

    public static final String KEY_OUT_ITEM = "item";
    public static final String KEY_OUT_SLOT = "slot";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        EquipmentSlot slot = ctxt.orElseInput(IN_SLOT, () -> EquipmentSlot.HAND);

        if (ctxt.hasInput(IN_ITEM))
            player.getInventory().setItem(slot, ctxt.input(IN_ITEM).create());

        ItemStack item = player.getInventory().getItem(slot);
        if (item == null)
            throw new IllegalStateException("Item is null");
        else if (!this.isDamageable(item))
            throw new IllegalStateException("Item is not damageable");
        this.makeOutputs(ctxt, player, item, slot);

        NMSItemSlot nmsSlot = NMSItemSlot.fromBukkit(slot);
        NMSEntityLiving nmsPlayer = NMSProvider.getProvider().wrap(player);
        NMSItemStack nmsStack = NMSProvider.getProvider().wrap(item);
        NMSItem nmsItem = nmsStack.getItem();

        int damageToApply = 999;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable)
        {
            Damageable damageable = (Damageable) meta;
            int maxDurability = nmsItem.getMaxDurability();
            int currentDurability = maxDurability - damageable.getDamage();
            damageToApply = Math.min(damageToApply, currentDurability);
        }

        nmsStack.damage(damageToApply, nmsPlayer, nmsEntityLiving -> nmsEntityLiving.broadcastItemBreak(nmsSlot));
    }

    private boolean isDamageable(ItemStack itemInMainHand)
    {
        Material type = itemInMainHand.getType();
        switch (type)
        {
            // <editor-fold desc="Tools">
            case WOODEN_AXE:
            case WOODEN_HOE:
            case WOODEN_PICKAXE:
            case WOODEN_SHOVEL:
            case WOODEN_SWORD:
            case STONE_AXE:
            case STONE_HOE:
            case STONE_PICKAXE:
            case STONE_SHOVEL:
            case STONE_SWORD:
            case IRON_AXE:
            case IRON_HOE:
            case IRON_PICKAXE:
            case IRON_SHOVEL:
            case IRON_SWORD:
            case GOLDEN_AXE:
            case GOLDEN_HOE:
            case GOLDEN_PICKAXE:
            case GOLDEN_SHOVEL:
            case GOLDEN_SWORD:
            case DIAMOND_AXE:
            case DIAMOND_HOE:
            case DIAMOND_PICKAXE:
            case DIAMOND_SHOVEL:
            case DIAMOND_SWORD:
            case NETHERITE_AXE:
            case NETHERITE_HOE:
            case NETHERITE_PICKAXE:
            case NETHERITE_SHOVEL:
            case NETHERITE_SWORD:
            case SHEARS:
            case FISHING_ROD:
            case FLINT_AND_STEEL:
            case SHIELD:
            case BOW:
            case CROSSBOW:
            case CARROT_ON_A_STICK:
            case WARPED_FUNGUS_ON_A_STICK:
                // </editor-fold>
                // <editor-fold desc="Armors">
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case GOLDEN_HELMET:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
            case NETHERITE_HELMET:
            case NETHERITE_CHESTPLATE:
            case NETHERITE_LEGGINGS:
            case NETHERITE_BOOTS:
                // </editor-fold>
            case ELYTRA:
            case TRIDENT:
            case TURTLE_HELMET:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerItemBreakEvent;
        PlayerItemBreakEvent e = (PlayerItemBreakEvent) event;

        boolean result = ctxt.ifHasInput(IN_ITEM, item -> item.isAdequate(e.getBrokenItem()));
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getBrokenItem(), null);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull ItemStack item, @Nullable EquipmentSlot slot)
    {
        ctxt.output(KEY_OUT_ITEM, item);
        if (slot != null)
            ctxt.output(KEY_OUT_SLOT, slot);
        super.makeOutputs(ctxt, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerItemBreakEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_ITEM);
        if (type != ScenarioType.ACTION_EXPECT)
            board.register(IN_SLOT);

        return board;
    }
}
