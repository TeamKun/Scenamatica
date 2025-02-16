package org.kunlab.scenamatica.action.actions.base.entity.vehicle;

import org.bukkit.EntityEffect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;

import java.util.Collections;
import java.util.List;

@Action("vehicle_exit")
@ActionDoc(
        name = "乗り物からの後者",
        description = "エンティティを乗り物から降ろします。",
        events = {
                VehicleExitEvent.class
        },

        executable = "エンティティを乗り物から降ろします。",
        expectable = "エンティティが乗り物から降りることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = VehicleExitAction.OUT_ENTITY,
                        description = "降りたエンティティです。",
                        type = EntityEffect.class
                )
        }
)
public class VehicleExitAction extends AbstractVehicleAction
        implements Executable, Expectable, Requireable
{
    public static final String OUT_ENTITY = "passenger";

    public static final InputToken<EntitySpecifier<Entity>> IN_ENTITY = ofSpecifier("passenger");

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Vehicle vehicle = this.selectTarget(ctxt);
        // エンティティを取得し, もし存在しない場合は例外をスローする。
        Entity entity = ctxt.input(IN_ENTITY).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalActionInputException("エンティティが見つかりません。"));

        ctxt.output(OUT_ENTITY, entity);
        // エンティティを乗り物から降ろす。
        vehicle.eject();
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedVehicleEvent(ctxt, event))
            return false;
        assert event instanceof VehicleExitEvent;
        VehicleExitEvent e = (VehicleExitEvent) event;

        Entity entity = e.getExited();

        // ↓ 乗り物自体の比較は, #checkMatchedVehicleEvent で行っているので, ここではエンティティの比較のみ行う。
        boolean result = ctxt.ifHasInput(IN_ENTITY, inputEntity -> inputEntity.checkMatchedEntity(entity));
        if (result)
            this.makeOutputs(ctxt, e.getVehicle(), entity);

        return result;
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Vehicle vehicle = this.selectTarget(ctxt);
        // entity を選択しないことも可能。
        Entity entity = ctxt.input(IN_ENTITY).selectTarget(ctxt.getContext()).orElse(null);
        if (ctxt.hasInput(IN_ENTITY) && entity == null)
            throw new IllegalActionInputException(IN_ENTITY, "Unable to find the entity to board the vehicle.");

        boolean result = !vehicle.getPassengers().contains(entity);
        if (result)
            this.makeOutputs(ctxt, vehicle, entity);

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Vehicle vehicle, @Nullable Entity entity)
    {
        if (entity != null)
            ctxt.output(OUT_ENTITY, entity);
        super.makeOutputs(ctxt, vehicle);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                VehicleExitEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_ENTITY);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_ENTITY);

        return board;
    }
}
