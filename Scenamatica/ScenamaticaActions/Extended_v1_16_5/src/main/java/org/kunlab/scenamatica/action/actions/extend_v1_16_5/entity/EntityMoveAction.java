package org.kunlab.scenamatica.action.actions.extend_v1_16_5.entity;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.base.entity.AbstractGeneralEntityAction;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalScenarioStateException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;

import java.util.Collections;
import java.util.List;

@Action(value = "entity_move", supportsSince = MinecraftVersion.V1_16_5)
@ActionDoc(
        name = "エンティティの移動",
        description = "エンティティを移動させます。",
        events = {
                EntityMoveEvent.class
        },
        executable = "エンティティを移動させます。",
        expectable = "エンティティが移動されることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = EntityMoveAction.OUT_KEY_FROM,
                        description = "移動前の位置です。",
                        type = Location.class
                ),
                @OutputDoc(
                        name = EntityMoveAction.OUT_KEY_TO,
                        description = "移動後の位置です。",
                        type = Location.class
                )
        }
)
public class EntityMoveAction extends AbstractGeneralEntityAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "from",
            description = "移動前の位置です。",
            type = Location.class,
            availableFor = ActionMethod.EXPECT,
            admonitions = {
                    @Admonition(
                            type = AdmonitionType.TIP,
                            content = "座標の比較は, 誤差 `0.01` まで許容されます。"
                    )
            }
    )
    public static final InputToken<LocationStructure> IN_FROM = ofInput(
            "from",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );
    @InputDoc(
            name = "to",
            description = "移動後の位置です。",
            type = Location.class,
            requiredOn = ActionMethod.EXECUTE
    )
    public static final InputToken<LocationStructure> IN_TO = ofInput(
            "to",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );
    @InputDoc(
            name = "ai",
            description = "AIを使用するかどうかです。",
            type = boolean.class
    )
    public static final InputToken<Boolean> IN_USE_AI = ofInput(
            "ai",
            Boolean.class,
            true
    );
    public static final String OUT_KEY_FROM = "from";
    public static final String OUT_KEY_TO = "to";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Location toLoc = Utils.assignWorldToLocation(ctxt.input(IN_TO), ctxt.getEngine());
        Entity entity = this.selectTarget(ctxt);

        this.makeOutputs(ctxt, entity, entity.getLocation(), toLoc);
        if (ctxt.input(IN_USE_AI) && entity instanceof Mob)
        {
            Mob mob = (Mob) entity;
            boolean success = mob.getPathfinder().moveTo(toLoc);
            if (!success)
                throw new IllegalScenarioStateException("Failed to find path from " + entity.getLocation() + " to " + toLoc);
        }
        else
            entity.teleport(toLoc);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(ctxt, event))
            return false;

        assert event instanceof EntityMoveEvent;
        EntityMoveEvent e = (EntityMoveEvent) event;

        boolean result = ctxt.ifHasInput(IN_FROM, from -> from.isAdequate(e.getFrom()))
                && ctxt.ifHasInput(IN_TO, to -> to.isAdequate(e.getTo()));
        if (result)
            this.makeOutputs(ctxt, e.getEntity(), e.getFrom(), e.getTo());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Entity entity, @NotNull Location from, @NotNull Location to)
    {
        ctxt.output(OUT_KEY_FROM, from);
        ctxt.output(OUT_KEY_TO, to);
        super.makeOutputs(ctxt, entity);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityMoveEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_FROM, IN_TO);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.register(IN_USE_AI)
                    .requirePresent(IN_TO)
                    .validator(b -> !b.isPresent(IN_FROM), "Cannot specify from in execute action.");
        return board;
    }
}
