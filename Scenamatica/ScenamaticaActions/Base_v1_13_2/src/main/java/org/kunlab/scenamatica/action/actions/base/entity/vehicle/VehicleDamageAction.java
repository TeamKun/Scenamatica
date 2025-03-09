package org.kunlab.scenamatica.action.actions.base.entity.vehicle;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.entity.NMSDamageSource;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;

import java.util.Collections;
import java.util.List;

@Action("vehicle_damage")
@ActionDoc(
        name = "乗り物の負傷",
        description = "乗り物にダメージを与えます。",
        events = {
                VehicleDamageEvent.class
        },
        executable = "乗り物にダメージを与えます。",
        expectable = "乗り物がダメージを受けることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = VehicleDamageAction.OUT_ATTACKER,
                        description = "ダメージを与えたエンティティです。",
                        type = Entity.class
                ),
                @OutputDoc(
                        name = VehicleDamageAction.OUT_DAMAGE,
                        description = "与えたダメージの量です。",
                        type = double.class
                )
        }
)
public class VehicleDamageAction extends AbstractVehicleAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "attacker",
            description = "ダメージを与えたエンティティを指定します。",
            type = EntitySpecifier.class
    )
    public static final InputToken<EntitySpecifier<Entity>> IN_ATTACKER = ofSpecifier("attacker");

    @InputDoc(
            name = "amount",
            description = "ダメージの量を指定します。",
            type = double.class,
            requiredOn = ActionMethod.EXECUTE
    )
    public static final InputToken<Double> IN_AMOUNT = ofInput(
            "amount",
            Double.class
    );

    public static final String OUT_ATTACKER = "attacker";
    public static final String OUT_DAMAGE = "damage";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Vehicle vehicle = this.selectTarget(ctxt);
        Entity attacker = null;
        if (ctxt.hasInput(IN_ATTACKER))
            attacker = ctxt.input(IN_ATTACKER).selectTarget(ctxt.getContext())
                    .orElseThrow(() -> new IllegalActionInputException("The attacker entity is not found."));
        double damage = ctxt.input(IN_AMOUNT);

        this.makeOutputs(ctxt, vehicle, attacker, damage);

        NMSEntity nmsEntity = NMSProvider.getProvider().wrap(vehicle);
        NMSDamageSource damageSource;
        if (attacker == null)
            damageSource = NMSDamageSource.GENERIC;
        else
            damageSource = NMSDamageSource.anyEntityAttack(attacker);

        nmsEntity.damageEntity(damageSource, (float) damage);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedVehicleEvent(ctxt, event))
            return false;
        assert event instanceof VehicleDamageEvent;
        VehicleDamageEvent e = (VehicleDamageEvent) event;

        boolean result = ctxt.ifHasInput(IN_ATTACKER, attacker -> attacker.checkMatchedEntity(e.getAttacker()));
        if (result)
            this.makeOutputs(ctxt, e.getVehicle(), e.getAttacker(), e.getDamage());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @Nullable Vehicle vehicle, @Nullable Entity attacker, @Nullable Double damage)
    {
        if (attacker != null)
            ctxt.output(OUT_ATTACKER, attacker);
        if (damage != null)
            ctxt.output(OUT_DAMAGE, damage);
        super.makeOutputs(ctxt, vehicle);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                VehicleDamageEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .registerAll(IN_AMOUNT, IN_ATTACKER);
    }
}
