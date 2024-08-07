package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
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
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;

import java.util.Collections;
import java.util.List;

@Action("player_drop_item")
@ActionDoc(
        name = "プレイヤのアイテムドロップ",
        description = "プレイヤがアイテムをドロップします。",
        events = {
                PlayerDropItemEvent.class
        },

        executable = "プレイヤにアイテムをドロップさせます。",
        expectable = "プレイヤがアイテムをドロップすることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = PlayerDropItemAction.KEY_OUT_ITEM,
                        description = "ドロップされたアイテムです。",
                        type = Item.class
                )
        }
)
public class PlayerDropItemAction extends AbstractPlayerAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "item",
            description = "ドロップするアイテムを指定します。",
            type = Item.class
    )
    public static final InputToken<EntityItemStructure> IN_ITEM = ofInput(
            "item",
            EntityItemStructure.class,
            ofDeserializer(EntityItemStructure.class)
    );
    public static final String KEY_OUT_ITEM = "item";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player target = selectTarget(ctxt);
        ItemStack stack = ctxt.ifHasInput(IN_ITEM, (r) -> {
            ItemStack st = r.getItemStack().create();
            target.getInventory().setItemInMainHand(st);
            return st;
        }, target.getInventory().getItemInMainHand());

        this.makeOutputs(ctxt, target, stack);

        NMSEntityPlayer nmsPlayer = NMSProvider.getProvider().wrap(target);
        nmsPlayer.drop(/* dropALl: */ false);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerDropItemEvent;
        PlayerDropItemEvent e = (PlayerDropItemEvent) event;

        boolean result = ctxt.ifHasInput(IN_ITEM, item -> item.isAdequate(e.getItemDrop()));
        if (result)
            ctxt.output(KEY_OUT_ITEM, e.getItemDrop());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player target, @NotNull Item item)
    {
        ctxt.output(KEY_OUT_ITEM, item);
        super.makeOutputs(ctxt, target);
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player target, @NotNull ItemStack item)
    {
        ctxt.output(KEY_OUT_ITEM, item);
        super.makeOutputs(ctxt, target);
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
