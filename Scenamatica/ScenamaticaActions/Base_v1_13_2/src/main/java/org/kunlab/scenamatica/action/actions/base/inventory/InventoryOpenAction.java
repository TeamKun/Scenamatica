package org.kunlab.scenamatica.action.actions.base.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;

import java.util.Collections;
import java.util.List;

@Action("inventory_open")
@ActionDoc(
        name = "インベントリを開く",
        description = "プレイヤのインベントリを開きます。",
        events = {
                InventoryOpenEvent.class
        },

        executable = "プレイヤのインベントリを開きます。",
        watchable = "プレイヤのインベントリが開かれることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = InventoryOpenAction.KEY_OUT_TARGET,
                        description = "対象のアクタです。",
                        type = Player.class
                )
        },

        admonitions = {
                @Admonition(
                        type = AdmonitionType.DANGER,
                        content = "プレイヤがインベントリを（既に・今まさに）開いているか確認するアクションはありません。  \n" +
                                "これは、 Bukkit がプレイヤのインベントリの状態を追跡していないためです。"
                )
        }

)
public class InventoryOpenAction extends AbstractInventoryAction
        implements Executable, Watchable
{
    @InputDoc(
            name = "target",
            description = "対象のアクタです。",
            type = PlayerSpecifier.class
    )
    public static final InputToken<PlayerSpecifier> IN_PLAYER = ofInput(
            "target",
            PlayerSpecifier.class,
            ofPlayer()
    );
    public static final String KEY_OUT_TARGET = "target";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = ctxt.input(IN_PLAYER).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalStateException("Cannot select target for this action, please specify target with valid specifier."));

        InventoryStructure inventoryStructure = ctxt.input(IN_INVENTORY);
        Inventory inventory = inventoryStructure.create();

        this.makeOutputs(ctxt, player, inventory);
        player.openInventory(inventory);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedInventoryEvent(ctxt, event))
            return false;

        assert event instanceof InventoryOpenEvent;
        InventoryOpenEvent e = (InventoryOpenEvent) event;
        HumanEntity player = e.getPlayer();
        if (!(player instanceof Player))
            return false;

        boolean result = ctxt.ifHasInput(IN_PLAYER, playerSpecifier -> playerSpecifier.checkMatchedPlayer((Player) player));
        if (result)
            this.makeOutputs(ctxt, e);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull Inventory inventory)
    {
        ctxt.output(KEY_OUT_TARGET, player);
        super.makeOutputs(ctxt, inventory);
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull InventoryOpenEvent event)
    {
        this.makeOutputs(ctxt, (Player) event.getPlayer(), event.getInventory());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryOpenEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_PLAYER);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_PLAYER);
        return board;
    }
}
