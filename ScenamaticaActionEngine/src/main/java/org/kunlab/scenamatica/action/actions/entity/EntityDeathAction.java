package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EntityDeathAction extends AbstractGeneralEntityAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "entity_death";
    public static final InputToken<List<ItemStackStructure>> IN_DROPS = ofInput(
            "drops",
            InputTypeToken.ofList(ItemStackStructure.class),
            ofTraverser(List.class, (ser, list) -> {
                List<ItemStackStructure> drops = new ArrayList<>();
                List<Map<String, Object>> dropMaps = MapUtils.checkAndCastList(list, InputTypeToken.ofMap(String.class, Object.class));
                for (Map<String, Object> dropMap : dropMaps)
                    drops.add(ser.deserialize(dropMap, ItemStackStructure.class));

                return drops;
            })
    );
    public static final InputToken<Integer> IN_DROP_EXP = ofInput(
            "dropExp",
            Integer.class
    );
    public static final InputToken<Double> IN_REVIVE_HEALTH = ofInput(
            "reviveHealth",
            Double.class
    );
    public static final InputToken<Boolean> IN_SHOULD_PLAY_DEATH_SOUND = ofInput(
            "playDeathSound",
            Boolean.class
    );
    public static final InputToken<Sound> IN_DEATH_SOUND = ofInput(
            "sound",
            Sound.class
    );
    public static final InputToken<SoundCategory> IN_DEATH_SOUND_CATEGORY = ofInput(
            "soundCategory",
            SoundCategory.class
    );
    public static final InputToken<Float> IN_DEATH_SOUND_VOLUME = ofInput(
            "soundVolume",
            Float.class
    );
    public static final InputToken<Float> IN_DEATH_SOUND_PITCH = ofInput(
            "soundPitch",
            Float.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Entity target = this.selectTarget(ctxt);
        if (target.isDead())
            throw new IllegalStateException("The target entity " + target + " is already dead.");

        if (!(target instanceof LivingEntity))
            throw new IllegalStateException("The target entity " + target + " is not a living entity.");

        LivingEntity livingEntity = (LivingEntity) target;
        this.makeOutputs(ctxt, livingEntity);
        livingEntity.setHealth(0.0f);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(ctxt, event))
            return false;

        EntityDeathEvent e = (EntityDeathEvent) event;

        if (ctxt.hasInput(IN_DROPS))
        {
            List<ItemStackStructure> expectedDrops = new ArrayList<>(ctxt.input(IN_DROPS));
            expectedDrops.removeIf(expectedDrop ->
                    e.getDrops().stream()
                            .noneMatch(expectedDrop::isAdequate)
            );

            if (!expectedDrops.isEmpty())
                return false;
        }

        boolean result = ctxt.ifHasInput(IN_DROP_EXP, exp -> exp == e.getDroppedExp())
                && ctxt.ifHasInput(IN_REVIVE_HEALTH, health -> health == e.getReviveHealth())
                && ctxt.ifHasInput(IN_SHOULD_PLAY_DEATH_SOUND, shouldPlay -> shouldPlay == e.shouldPlayDeathSound())
                && ctxt.ifHasInput(IN_DEATH_SOUND, sound -> sound == e.getDeathSound())
                && ctxt.ifHasInput(IN_DEATH_SOUND_CATEGORY, category -> category == e.getDeathSoundCategory())
                && ctxt.ifHasInput(IN_DEATH_SOUND_VOLUME, volume -> volume == e.getDeathSoundVolume())
                && ctxt.ifHasInput(IN_DEATH_SOUND_PITCH, pitch -> pitch == e.getDeathSoundPitch());
        if (result)
            this.makeOutputs(ctxt, e.getEntity());

        return result;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityDeathEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type);
        if (type != ScenarioType.ACTION_EXECUTE)
            board.registerAll(
                    IN_DROPS,
                    IN_DROP_EXP,
                    IN_REVIVE_HEALTH,
                    IN_SHOULD_PLAY_DEATH_SOUND,
                    IN_DEATH_SOUND,
                    IN_DEATH_SOUND_CATEGORY,
                    IN_DEATH_SOUND_VOLUME,
                    IN_DEATH_SOUND_PITCH
            );

        return board;
    }
}
