package org.kunlab.scenamatica.action.actions.base.entity;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.exceptions.scenario.IllegalScenarioStateException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Action("entity_death")
@ActionDoc(
        name = "エンティティの殺害",
        description = "エンティティを死亡させます。",
        events = {
                EntityDeathEvent.class
        },

        executable = "エンティティを死亡させます。",
        expectable = "エンティティが死亡することを期待します。",
        requireable = ActionDoc.UNALLOWED
)
public class EntityDeathAction extends AbstractGeneralEntityAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "drops",
            description = "死亡時にドロップするアイテムです。",
            type = ItemStackStructure.class,
            availableFor = {ActionMethod.EXPECT}
    )
    public static final InputToken<List<ItemStackStructure>> IN_DROPS = ofInput(
            "drops",
            InputTypeToken.ofList(ItemStackStructure.class),
            ofListDeserializer(ofDeserializer(ItemStackStructure.class))
    );

    @InputDoc(
            name = "dropExp",
            description = "死亡時にドロップする経験値です。",
            type = int.class,
            min = 0,
            availableFor = {ActionMethod.EXPECT}
    )
    public static final InputToken<Integer> IN_DROP_EXP = ofInput(
            "dropExp",
            Integer.class
    );

    @InputDoc(
            name = "reviveHealth",
            description = "復活時の体力です。",
            type = double.class,
            min = 0,
            availableFor = {ActionMethod.EXPECT}
    )
    public static final InputToken<Double> IN_REVIVE_HEALTH = ofInput(
            "reviveHealth",
            Double.class
    );

    @InputDoc(
            name = "playDeathSound",
            description = "死亡時にサウンドを再生するかどうかです。",
            type = boolean.class,
            availableFor = {ActionMethod.EXPECT}
    )
    public static final InputToken<Boolean> IN_SHOULD_PLAY_DEATH_SOUND = ofInput(
            "playDeathSound",
            Boolean.class
    );

    @InputDoc(
            name = "sound",
            description = "死亡時に再生するサウンドです。",
            type = Sound.class,
            availableFor = {ActionMethod.EXPECT}
    )
    public static final InputToken<Sound> IN_DEATH_SOUND = ofInput(
            "sound",
            Sound.class
    );

    @InputDoc(
            name = "soundCategory",
            description = "死亡時に再生するサウンドのカテゴリです。",
            type = SoundCategory.class,
            availableFor = {ActionMethod.EXPECT}
    )
    public static final InputToken<SoundCategory> IN_DEATH_SOUND_CATEGORY = ofInput(
            "soundCategory",
            SoundCategory.class
    );

    @InputDoc(
            name = "soundVolume",
            description = "死亡時に再生するサウンドの音量です。",
            type = float.class,
            min = 0,
            availableFor = {ActionMethod.EXPECT}
    )
    public static final InputToken<Float> IN_DEATH_SOUND_VOLUME = ofInput(
            "soundVolume",
            Float.class
    );

    @InputDoc(
            name = "soundPitch",
            description = "死亡時に再生するサウンドのピッチです。",
            type = float.class,
            min = 0,
            availableFor = {ActionMethod.EXPECT}
    )
    public static final InputToken<Float> IN_DEATH_SOUND_PITCH = ofInput(
            "soundPitch",
            Float.class
    );

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Entity target = this.selectTarget(ctxt);
        if (target.isDead())
            throw new IllegalScenarioStateException("The target entity " + target + " is already dead.");

        if (!(target instanceof LivingEntity))
            throw new IllegalActionInputException(IN_TARGET_ENTITY, "The target entity " + target + " is not a living entity.");

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

    // TODO: require() 実装

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
