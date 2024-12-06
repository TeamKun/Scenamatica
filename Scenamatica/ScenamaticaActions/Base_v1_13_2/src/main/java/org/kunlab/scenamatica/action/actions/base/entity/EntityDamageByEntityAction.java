package org.kunlab.scenamatica.action.actions.base.entity;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Action("entity_damage_by_entity")
@ActionDoc(
        name = "エンティティへのエンティティによるダメージ",
        description = "エンティティにエンティティによるダメージを与えます。",
        events = {
                EntityDamageByEntityEvent.class
        },

        executable = "エンティティにエンティティによるダメージを与えます。",
        expectable = "エンティティがエンティティによるダメージを受けることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = "damager",
                        description = "ダメージを与えたエンティティです。",
                        type = Entity.class
                )
        }
)
public class EntityDamageByEntityAction extends EntityDamageAction
        implements Executable
{
    @InputDoc(
            name = "damager",
            description = "ダメージを与えたエンティティを指定します。",
            type = EntitySpecifier.class,
            requiredOn = ActionMethod.EXECUTE
    )
    public static final InputToken<EntitySpecifier<Entity>> IN_DAMAGER = ofSpecifier("damager"); // 殴った人

    public static final String OUT_KEY_DAMAGER = "damager";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Entity target = this.selectTarget(ctxt);

        if (!(target instanceof Damageable))
            throw new IllegalActionInputException("Target is not damageable");

        Entity damager = ctxt.input(IN_DAMAGER).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalActionInputException(IN_DAMAGER, "Cannot select damager for this action, please specify damager with valid specifier."));

        this.makeOutputs(ctxt, target, damager, null, ctxt.input(IN_AMOUNT), null);
        ((Damageable) target).damage(ctxt.input(IN_AMOUNT), damager);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof EntityDamageByEntityEvent || super.checkFired(ctxt, event)))
            return false;

        assert event instanceof EntityDamageByEntityEvent;
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

        boolean result = ctxt.ifHasInput(IN_DAMAGER, damager -> damager.checkMatchedEntity(e.getDamager()));
        if (result)
            this.makeOutputs(ctxt, e.getEntity(), e.getDamager(), e.getCause(), e.getDamage(), createModifiersMap(e));

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Entity entity, @NotNull Entity damager, EntityDamageEvent.DamageCause cause, double amount, @Nullable Map<String, Double> modifiers)
    {
        ctxt.output(OUT_KEY_DAMAGER, damager);
        super.makeOutputs(ctxt, entity, cause, amount, modifiers);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityDamageByEntityEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_DAMAGER);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_DAMAGER);

        return board;
    }
}
