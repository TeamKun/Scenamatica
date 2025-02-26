package org.kunlab.scenamatica.action.actions.base.entity.vehicle;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
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

@Action("vehicle_enter")
@ActionDoc(
        name = "乗り物への搭乗",
        description = "エンティティを乗り物に搭乗させます。",
        events = {
                VehicleEnterEvent.class
        },
        executable = "エンティティを乗り物に搭乗させます。",
        expectable = "エンティティが乗り物に搭乗することを期待します。",
        requireable = "エンティティが乗り物に搭乗しているかどうかを確認します。",

        outputs = {
                @OutputDoc(
                        name = VehicleEnterAction.OUT_PASSENGER,
                        description = "搭乗したエンティティです。",
                        type = Entity.class
                )
        }
)
public class VehicleEnterAction extends AbstractVehicleAction
        implements Executable, Expectable, Requireable
{
    public static final String OUT_PASSENGER = "passenger";

    @InputDoc(
            name = "passenger",
            description = "乗り物に搭乗するエンティティです。",
            type = EntitySpecifier.class
    )
    public static final InputToken<EntitySpecifier<Entity>> IN_PASSENGER = ofSpecifier("passenger");

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Vehicle vehicle = this.selectTarget(ctxt);
        // エンティティを取得し, もし存在しない場合は例外をスローする。
        Entity entity = ctxt.input(IN_PASSENGER).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalActionInputException(IN_PASSENGER, "Unable to find the entity to board the vehicle."));

        this.makeOutputs(ctxt, vehicle, entity);
        vehicle.addPassenger(entity);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedVehicleEvent(ctxt, event))
            return false;
        assert event instanceof VehicleEnterEvent;
        VehicleEnterEvent e = (VehicleEnterEvent) event;

        Entity entity = e.getEntered();  // 搭乗したエンティティ

        // ↓ 乗り物自体の比較は, #checkMatchedVehicleEvent で行っているので, ここではエンティティの比較のみ行う。
        boolean result = ctxt.ifHasInput(IN_PASSENGER, inputEntity -> inputEntity.checkMatchedEntity(entity));
        if (result)
            this.makeOutputs(ctxt, e.getVehicle(), entity);

        return result;
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        // 乗り物が指定されていない場合は, IN_PASSENGER が any 乗り物に乗っているかどうかのみ判定。
        if (!ctxt.hasInput(this.IN_TARGET_ENTITY))
        {
            // IN_PASSENGER を必須とする。
            if (!ctxt.hasInput(IN_PASSENGER))
                throw new IllegalActionInputException(IN_PASSENGER, "You must specify the entity to board the vehicle if you do not specify the vehicle.");

            Entity entity = ctxt.input(IN_PASSENGER).selectTarget(ctxt.getContext())
                    .orElseThrow(() -> new IllegalActionInputException(IN_PASSENGER, "Unable to find the entity to board the vehicle."));

            return entity.isInsideVehicle();
        }

        Entity targetVehicle = super.selectTarget(ctxt);

        return ctxt.ifHasInput(IN_PASSENGER, inputEntity -> targetVehicle.getPassengers().stream().anyMatch(inputEntity::checkMatchedEntity));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                VehicleEnterEvent.class
        );
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @Nullable Vehicle vehicle, @Nullable Entity entity)
    {
        if (entity != null)
            ctxt.output(OUT_PASSENGER, entity);
        super.makeOutputs(ctxt, vehicle);
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_PASSENGER);
        // アクション実行シナリオの場合は, "entity" を必須とする。
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_PASSENGER);

        return board;
    }
}
