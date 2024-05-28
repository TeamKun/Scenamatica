package org.kunlab.scenamatica.action.actions.base.inventory.interact;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;

import java.util.Collections;
import java.util.List;

@Action("inventory_click")
@ActionDoc(
        name = "インベントリのクリック",
        description = "インベントリをクリックします。",
        events = {
                InventoryClickEvent.class
        },

        executable = "インベントリをクリックします。",
        watchable = "インベントリがクリックされることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = InventoryClickAction.OUT_KEY_CLICK_TYPE,
                        description = "クリックの種類です。",
                        type = ClickType.class
                ),
                @OutputDoc(
                        name = InventoryClickAction.OUT_KEY_INVENTORY_ACTION,
                        description = "インベントリのアクションです。",
                        type = InventoryAction.class
                ),
                @OutputDoc(
                        name = InventoryClickAction.OUT_KEY_SLOT_TYPE,
                        description = "スロットの種類です。",
                        type = InventoryType.SlotType.class
                ),
                @OutputDoc(
                        name = InventoryClickAction.OUT_KEY_SLOT,
                        description = "スロットのインデックスです。",
                        type = int.class,
                        min = 0
                ),
                @OutputDoc(
                        name = InventoryClickAction.OUT_KEY_RAW_SLOT,
                        description = "生のスロットのインデックスです。",
                        type = int.class,
                        min = 0
                ),
                @OutputDoc(
                        name = InventoryClickAction.OUT_KEY_CLICKED_ITEM,
                        description = "クリックされたアイテムです。",
                        type = ItemStack.class
                ),
                @OutputDoc(
                        name = InventoryClickAction.OUT_KEY_BUTTON,
                        description = "ボタンのインデックスです。",
                        type = int.class,
                        min = 0
                ),
                @OutputDoc(
                        name = InventoryClickAction.OUT_KEY_CURSOR_ITEM,
                        description = "カーソルが保持しているアイテムです。",
                        type = ItemStack.class
                )
        }
)
public class InventoryClickAction extends AbstractInventoryInteractAction
        implements Executable, Watchable
{
    @InputDoc(
            name = "type",
            description = "クリックの種類を指定します。",
            type = ClickType.class
    )
    public static final InputToken<ClickType> IN_CLICK_TYPE = ofInput(
            "type",
            ClickType.class,
            ofEnum(ClickType.class)
    );
    @InputDoc(
            name = "action",
            description = "動作を指定します。",
            type = InventoryAction.class
    )
    public static final InputToken<InventoryAction> IN_INVENTORY_ACTION = ofInput(
            "action",
            InventoryAction.class,
            ofEnum(InventoryAction.class)
    );
    @InputDoc(
            name = "slotType",
            description = "スロットの種類を指定します。",
            type = InventoryType.SlotType.class
    )
    public static final InputToken<InventoryType.SlotType> IN_SLOT_TYPE = ofInput(
            "slotType",
            InventoryType.SlotType.class,
            ofEnum(InventoryType.SlotType.class)
    );
    @InputDoc(
            name = "slot",
            description = "スロットのインデックスを指定します。",
            type = int.class,
            min = 0
    )
    public static final InputToken<Integer> IN_SLOT = ofInput(
            "slot",
            Integer.class
    );
    @InputDoc(
            name = "rawSlot",
            description = "生のスロットのインデックスを指定します。",
            type = int.class,
            min = 0
    )
    public static final InputToken<Integer> IN_RAW_SLOT = ofInput(
            "rawSlot",
            Integer.class
    );
    @InputDoc(
            name = "clickedItem",
            description = "クリックされたアイテムを指定します。",
            type = ItemStack.class
    )
    public static final InputToken<ItemStackStructure> IN_CLICKED_ITEM = ofInput(
            "clickedItem",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );
    @InputDoc(
            name = "button",
            description = "ボタンのインデックスを指定します。",
            type = int.class,
            min = 0
    )
    public static final InputToken<Integer> IN_BUTTON = ofInput(
            "button",
            Integer.class
    );
    @InputDoc(
            name = "cursorItem",
            description = "カーソルが保持しているアイテムを指定します。",
            type = ItemStack.class
    )
    public static final InputToken<ItemStackStructure> IN_CURSOR_ITEM = ofInput(
            "cursorItem",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );
    public static final String OUT_KEY_CLICK_TYPE = "type";
    public static final String OUT_KEY_INVENTORY_ACTION = "action";
    public static final String OUT_KEY_SLOT_TYPE = "slotType";
    public static final String OUT_KEY_SLOT = "slot";
    public static final String OUT_KEY_RAW_SLOT = "rawSlot";
    public static final String OUT_KEY_CLICKED_ITEM = "clickedItem";
    public static final String OUT_KEY_BUTTON = "button";
    public static final String OUT_KEY_CURSOR_ITEM = "cursorItem";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player target = ctxt.input(IN_PLAYER).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalStateException("Target is not found."));
        Actor actor = ctxt.getActorOrThrow(target);
        ClickType type = ctxt.input(IN_CLICK_TYPE);

        Inventory inv = null;
        if (ctxt.hasInput(IN_INVENTORY))
        {
            inv = ctxt.input(IN_INVENTORY).create();
            target.openInventory(inv);
        }
        Integer slot = ctxt.orElseInput(IN_SLOT, () -> {
            if (ctxt.orElseInput(IN_SLOT_TYPE, () -> null) == InventoryType.SlotType.OUTSIDE)
                return -999;
            else
                return target.getOpenInventory().convertSlot(ctxt.input(IN_RAW_SLOT));
        });

        Integer button = ctxt.orElseInput(IN_BUTTON, () -> {
            switch (type)
            {
                case LEFT:
                    return 0;
                case RIGHT:
                    return 1;
                case MIDDLE:
                    return 2;
                default:
                    throw new IllegalStateException("Invalid click type: " + type);
            }
        });

        assert button != null;

        ItemStack clicked = ctxt.ifHasInput(IN_CLICKED_ITEM, ItemStackStructure::create, null);

        this.makeOutputs(ctxt, target, inv, type, slot, button);
        actor.clickInventory(
                type,
                slot,
                button,
                clicked
        );
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, Player target, @Nullable Inventory inventory, ClickType type, int slot, int button)
    {
        ctxt.output(OUT_KEY_TARGET, target);
        ctxt.output(OUT_KEY_CLICK_TYPE, type);
        ctxt.output(OUT_KEY_SLOT, slot);
        ctxt.output(OUT_KEY_BUTTON, button);
        super.makeOutputs(ctxt, target, inventory);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedInventoryInteractEvent(ctxt, event))
            return false;

        InventoryClickEvent e = (InventoryClickEvent) event;

        boolean result = ctxt.ifHasInput(IN_CLICK_TYPE, type -> type == e.getClick())
                && ctxt.ifHasInput(IN_INVENTORY_ACTION, action -> action == e.getAction())
                && ctxt.ifHasInput(IN_SLOT_TYPE, slotType -> slotType == e.getSlotType())
                && ctxt.ifHasInput(IN_SLOT, slot -> slot == e.getSlot())
                && ctxt.ifHasInput(IN_RAW_SLOT, rawSlot -> rawSlot == e.getRawSlot())
                && ctxt.ifHasInput(IN_CLICKED_ITEM, clickedItem -> clickedItem.isAdequate(e.getCurrentItem()))
                && ctxt.ifHasInput(IN_BUTTON, button -> button == e.getHotbarButton())
                && ctxt.ifHasInput(IN_CURSOR_ITEM, cursorItem -> cursorItem.isAdequate(e.getCursor()));

        if (result)
            this.makeOutputs(ctxt, e);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull InventoryClickEvent e)
    {
        ctxt.output(OUT_KEY_CLICK_TYPE, e.getClick());
        ctxt.output(OUT_KEY_INVENTORY_ACTION, e.getAction());
        ctxt.output(OUT_KEY_SLOT_TYPE, e.getSlotType());
        ctxt.output(OUT_KEY_SLOT, e.getSlot());
        ctxt.output(OUT_KEY_RAW_SLOT, e.getRawSlot());
        if (e.getCurrentItem() != null)
            ctxt.output(OUT_KEY_CLICKED_ITEM, e.getCurrentItem());
        ctxt.output(OUT_KEY_BUTTON, e.getHotbarButton());
        if (e.getCursor() != null)
            ctxt.output(OUT_KEY_CURSOR_ITEM, e.getCursor());
        super.makeOutputs(ctxt, e.getWhoClicked(), e.getClickedInventory());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryClickEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_CLICK_TYPE, IN_INVENTORY_ACTION, IN_SLOT_TYPE,
                        IN_SLOT, IN_RAW_SLOT, IN_CLICKED_ITEM, IN_BUTTON, IN_CURSOR_ITEM
                );

        if (type == ScenarioType.ACTION_EXECUTE)
            board.validator(
                            b -> b.isPresent(IN_SLOT) || b.isPresent(IN_RAW_SLOT),
                            "slot or raw_slot must be present at the same time"
                    )
                    .validator(
                            b -> !(b.isPresent(IN_SLOT) && b.isPresent(IN_RAW_SLOT)),
                            "cannot specify both slot and raw_slot at the same time"
                    )
                    .validator(
                            b -> b.isPresent(IN_BUTTON) || !b.isPresent(IN_CLICK_TYPE)
                                    || (b.get(IN_CLICK_TYPE) == ClickType.LEFT || b.get(IN_CLICK_TYPE) == ClickType.RIGHT || b.get(IN_CLICK_TYPE) == ClickType.MIDDLE),
                            "button cannot be null when click type is not left, right or middle"
                    );

        return board;
    }
}
