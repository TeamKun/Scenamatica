package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;

import java.util.Collections;
import java.util.List;

@Action("player_interact_at_entity")
@ActionDoc(
        name = "プレイヤのエンティティでのインタラクト",
        description = "プレイヤがエンティティでインタラクトします。",
        events = {
                PlayerInteractAtEntityEvent.class
        },

        executable = "プレイヤがエンティティをクリックします。",
        watchable = "プレイヤがエンティティをクリックすることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = PlayerInteractAtEntityAction.OUT_POSITION,
                        description = "クリックされた位置です。",
                        type = Location.class
                )
        },

        admonitions = {
                @Admonition(
                        type = AdmonitionType.DANGER,
                        title = "DEPRECATED!!!",
                        content = "このアクションは、**アーマースタンドのクリックのみ**をサポートしています。  \n" +
                                "ほとんどの場合は、 \\[プレイヤのエンティティのクリック] を使用してください。\n" +
                                "\n" +
                                "出典：[PlayerInteractAtEntityEvent (Paper-API 1.16.5-R0.1-SNAPSHOT API)](https://jd.papermc.io/paper/1.16/org/bukkit/event/player/PlayerInteractAtEntityEvent.html#:~:text=Users%20are%20advised%20to%20listen%20to%20this%20(parent)%20class%20unless%20specifically%20required.%0ANote%20that%20interacting%20with%20Armor%20Stands%20fires%20this%20event%20only%20and%20not%20its%20parent%20and%20as%20such%20users%20are%20expressly%20required%20to%20listen%20to%20this%20event%20for%20that%20scenario.)"
                )
        }
)
public class PlayerInteractAtEntityAction extends PlayerInteractEntityAction
{
    public static final String OUT_POSITION = "position";

    @InputDoc(
            name = "position",
            description = "クリックする位置を指定します。",
            type = Location.class
    )
    public static final InputToken<LocationStructure> IN_POSITION = ofInput(
            "position",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );

    @Override
    protected void doInteract(ActionContext ctxt, Entity targeTentity, Actor actor, @NotNull NMSHand hand)
    {
        actor.interactEntity(
                targeTentity,
                NMSEntityUseAction.INTERACT_AT,
                hand,
                ctxt.input(IN_POSITION).create()
        );
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkFired(ctxt, event))
            return false;

        PlayerInteractAtEntityEvent e = (PlayerInteractAtEntityEvent) event;
        Vector clickedPosition = e.getClickedPosition();
        Location loc = clickedPosition.toLocation(ctxt.getContext().getStage().getWorld());

        boolean result = ctxt.ifHasInput(IN_POSITION, position -> position.isAdequate(loc));
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getRightClicked(), NMSHand.fromEquipmentSlot(e.getHand()), loc);

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull Entity targetEntity, @NotNull NMSHand hand, @NotNull Location position)
    {
        ctxt.output(OUT_POSITION, position);
        super.makeOutputs(ctxt, player, targetEntity, hand);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerInteractAtEntityEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_POSITION);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_POSITION);

        return board;
    }
}
