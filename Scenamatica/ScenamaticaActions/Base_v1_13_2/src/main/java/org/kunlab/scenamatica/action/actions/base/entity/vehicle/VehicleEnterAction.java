package org.kunlab.scenamatica.action.actions.base.entity.vehicle;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
                        name = VehicleEnterAction.OUT_ENTITY,
                        description = "搭乗したエンティティです。",
                        type = Entity.class
                )
        }
)
public class VehicleEnterAction extends AbstractVehicleAction
        implements Executable, Expectable, Requireable
{
    public static final String OUT_ENTITY = "passenger";

    @InputDoc(
            name = "passenger",
            description = "乗り物に搭乗するエンティティです。",
            type = EntitySpecifier.class
    )
    public static final InputToken<EntitySpecifier<Entity>> IN_ENTITY = ofSpecifier("entity");

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Vehicle vehicle = this.selectTarget(ctxt);
        // エンティティを取得し, もし存在しない場合は例外をスローする。
        Entity entity = ctxt.input(IN_ENTITY).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalActionInputException(IN_ENTITY, "Unable to find the entity to board the vehicle."));

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
        boolean result = ctxt.ifHasInput(IN_ENTITY, inputEntity -> inputEntity.checkMatchedEntity(entity));
        if (result)
            this.makeOutputs(ctxt, e.getVehicle(), entity);

        return result;
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Vehicle vehicle = this.selectTarget(ctxt);
        Entity entity = ctxt.input(IN_ENTITY).selectTarget(ctxt.getContext()).orElse(null);
        if (ctxt.hasInput(IN_ENTITY) && entity == null)
            throw new IllegalActionInputException(IN_ENTITY, "Unable to find the entity to board the vehicle.");

        boolean result = vehicle.getPassengers().contains(entity);
        if (result)
            this.makeOutputs(ctxt, vehicle, entity);

        return result;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                VehicleEnterEvent.class
        );
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Vehicle vehicle, @Nullable Entity entity)
    {
        if (entity != null)
            ctxt.output(OUT_ENTITY, entity);
        super.makeOutputs(ctxt, vehicle);
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_ENTITY);
        // アクション実行シナリオの場合は, "entity" を必須とする。
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_ENTITY);

        return board;
    }
}
