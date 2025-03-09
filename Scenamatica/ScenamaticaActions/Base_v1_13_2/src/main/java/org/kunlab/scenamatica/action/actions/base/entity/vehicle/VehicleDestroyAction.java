package org.kunlab.scenamatica.action.actions.base.entity.vehicle;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.entity.NMSDamageSource;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Action("vehicle_destroy")
@ActionDoc(
        name = "乗り物の破壊",
        description = "乗り物を破壊します。",
        events = {
                VehicleDestroyEvent.class
        },
        executable = "乗り物を破壊します。",
        expectable = "乗り物が破壊されることを期待します。",
        requireable = "乗り物が破壊されたかどうかを確認します。",
        admonitions = {
                @Admonition(
                        type = AdmonitionType.NOTE,
                        content = "このアクションは, 乗り物を破壊するために, 乗り物に 40.1f のダメージを与えます。"
                )
        },

        outputs = {
                @OutputDoc(
                        name = VehicleDestroyAction.OUT_VEHICLE,
                        description = "破壊された乗り物です。",
                        type = Vehicle.class
                ),
                @OutputDoc(
                        name = VehicleDestroyAction.OUT_ATTACKER,
                        description = "乗り物を破壊したエンティティです。",
                        type = Entity.class
                )
        }
)
public class VehicleDestroyAction extends AbstractVehicleAction
        implements Executable, Expectable, Requireable
{
    @InputDoc(
            name = "attacker",
            description = "乗り物を破壊したエンティティです。",
            type = EntitySpecifier.class
    )
    public static final InputToken<EntitySpecifier<Entity>> IN_ATTACKER = ofSpecifier("attacker");

    public static final String OUT_VEHICLE = "vehicle";
    public static final String OUT_ATTACKER = "attacker";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Vehicle vehicle = this.selectTarget(ctxt);
        Entity attacker = null;
        if (ctxt.hasInput(IN_ATTACKER))
            attacker = ctxt.input(IN_ATTACKER).selectTarget(ctxt.getContext())
                    .orElseThrow(() -> new IllegalActionInputException("The attacker entity is not found."));

        this.makeOutputs(ctxt, vehicle, attacker);

        NMSEntity nmsEntity = NMSProvider.getProvider().wrap(vehicle);
        NMSDamageSource damageSource = NMSDamageSource.anyEntityAttack(attacker);
        // 40.0f より大きいダメージを与えることで, 乗り物を破壊する（この値は ソースにハードコーディングされている）
        nmsEntity.damageEntity(damageSource, 40.1f);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedVehicleEvent(ctxt, event))
            return false;
        assert event instanceof VehicleDestroyEvent;
        VehicleDestroyEvent e = (VehicleDestroyEvent) event;

        boolean result = ctxt.ifHasInput(IN_ATTACKER, attacker -> attacker.checkMatchedEntity(e.getAttacker()));
        if (result)
            this.makeOutputs(ctxt, e.getVehicle(), e.getAttacker());

        return result;
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        // エンティティは存在しないことを許容するので, super#searchEntity は使わない
        if (!ctxt.hasInput(this.IN_TARGET_ENTITY))
            throw new IllegalActionInputException(this.IN_TARGET_ENTITY, "You must specify the target entity.");

        Optional<Vehicle> vehicle = ctxt.input(this.IN_TARGET_ENTITY)
                .selectTarget(ctxt.getContext());
        boolean result = !vehicle.isPresent()  // 乗り物が存在しない場合も破壊されたとみなす
                || vehicle.get().isDead();
        if (result)
            this.makeOutputs(ctxt, vehicle.orElse(null), null);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @Nullable Vehicle vehicle, @Nullable Entity attacker)
    {
        if (vehicle != null)
            ctxt.output(OUT_VEHICLE, vehicle);
        if (attacker != null)
            ctxt.output(OUT_ATTACKER, attacker);
        super.makeOutputs(ctxt, vehicle);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                VehicleDestroyEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type);
        if (type != ScenarioType.CONDITION_REQUIRE)
            board.register(IN_ATTACKER);

        return board;
    }
}
