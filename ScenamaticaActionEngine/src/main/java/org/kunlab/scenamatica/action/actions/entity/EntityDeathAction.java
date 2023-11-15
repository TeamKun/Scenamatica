package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.BeanUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntityDeathAction extends AbstractEntityAction<EntityDeathAction.Argument>
        implements Executable<EntityDeathAction.Argument>, Watchable<EntityDeathAction.Argument>
{
    public static final String KEY_ACTION_NAME = "entity_death";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Entity target = argument.selectTarget();
        if (target.isDead())
            throw new IllegalStateException("The target entity " + target + " is already dead.");

        if (!(target instanceof LivingEntity))
            throw new IllegalStateException("The target entity " + target + " is not a living entity.");

        LivingEntity livingEntity = (LivingEntity) target;
        livingEntity.setHealth(0.0f);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(argument, engine, event))
            return false;

        EntityDeathEvent e = (EntityDeathEvent) event;
        if (argument.drops != null)
        {
            List<ItemStackBean> expectedDrops = new ArrayList<>(argument.getDrops());
            expectedDrops.removeIf(expectedDrop ->
                    e.getDrops().stream()
                            .noneMatch(drop -> BeanUtils.isSame(expectedDrop, drop, false))
            );

            if (!expectedDrops.isEmpty())
                return false;
        }

        return (argument.getDropExp() == null || argument.getDropExp() == e.getDroppedExp())
                && (argument.getReviveHealth() == null || argument.getReviveHealth() == e.getReviveHealth())
                && (argument.getShouldPlayDeathSound() == null || argument.getShouldPlayDeathSound() == e.shouldPlayDeathSound())
                && (argument.getDeathSound() == null || argument.getDeathSound() == e.getDeathSound())
                && (argument.getDeathSoundCategory() == null || argument.getDeathSoundCategory() == e.getDeathSoundCategory())
                && (argument.getDeathSoundVolume() == null || argument.getDeathSoundVolume() == e.getDeathSoundVolume())
                && (argument.getDeathSoundPitch() == null || argument.getDeathSoundPitch() == e.getDeathSoundPitch());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityDeathEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        List<ItemStackBean> drops = null;
        if (map.containsKey(Argument.KEY_DROPS))
        {
            drops = new ArrayList<>();
            List<Map<String, Object>> dropMaps = MapUtils.getAsList(map, Argument.KEY_DROPS);
            for (Map<String, Object> dropMap : dropMaps)
                drops.add(serializer.deserializeItemStack(dropMap));
        }

        return new Argument(
                super.deserializeTarget(map, serializer),
                drops,
                MapUtils.getOrNull(map, Argument.KEY_DROP_EXP),
                MapUtils.getOrNull(map, Argument.KEY_REVIVE_HEALTH),

                MapUtils.getOrNull(map, Argument.KEY_SHOULD_PLAY_DEATH_SOUND),
                MapUtils.getOrNull(map, Argument.KEY_DEATH_SOUND),
                MapUtils.getOrNull(map, Argument.KEY_DEATH_SOUND_CATEGORY),
                MapUtils.getAsNumberOrNull(map, Argument.KEY_DEATH_SOUND_VOLUME, Number::floatValue),
                MapUtils.getAsNumberOrNull(map, Argument.KEY_DEATH_SOUND_VOLUME, Number::floatValue)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument
    {
        public static final String KEY_DROPS = "drops";
        public static final String KEY_DROP_EXP = "dropExp";
        public static final String KEY_REVIVE_HEALTH = "reviveHealth";

        public static final String KEY_SHOULD_PLAY_DEATH_SOUND = "playDeathSound";
        public static final String KEY_DEATH_SOUND = "sound";
        public static final String KEY_DEATH_SOUND_CATEGORY = "soundCategory";
        public static final String KEY_DEATH_SOUND_VOLUME = "soundVolume";
        public static final String KEY_DEATH_SOUND_PITCH = "soundPitch";

        List<ItemStackBean> drops;
        Integer dropExp;
        Double reviveHealth;

        Boolean shouldPlayDeathSound;
        Sound deathSound;
        SoundCategory deathSoundCategory;
        Float deathSoundVolume;
        Float deathSoundPitch;

        public Argument(@Nullable EntityArgumentHolder mayTarget, List<ItemStackBean> drops, Integer dropExp, Double reviveHealth, Boolean shouldPlayDeathSound, Sound deathSound, SoundCategory deathSoundCategory, Float deathSoundVolume, Float deathSoundPitch)
        {
            super(mayTarget);
            this.drops = drops;
            this.dropExp = dropExp;
            this.reviveHealth = reviveHealth;
            this.shouldPlayDeathSound = shouldPlayDeathSound;
            this.deathSound = deathSound;
            this.deathSoundCategory = deathSoundCategory;
            this.deathSoundVolume = deathSoundVolume;
            this.deathSoundPitch = deathSoundPitch;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && (this.drops == null && arg.drops == null || this.drops != null && arg.drops != null && MapUtils.equals(this.drops, arg.drops))
                    && Objects.equals(this.dropExp, arg.dropExp)
                    && Objects.equals(this.reviveHealth, arg.reviveHealth)
                    && Objects.equals(this.shouldPlayDeathSound, arg.shouldPlayDeathSound)
                    && this.deathSound == arg.deathSound
                    && this.deathSoundCategory == arg.deathSoundCategory
                    && Objects.equals(this.deathSoundVolume, arg.deathSoundVolume)
                    && Objects.equals(this.deathSoundPitch, arg.deathSoundPitch);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                this.throwIfNotSelectable();
                ensureNotPresent(KEY_DROPS, this.drops);
                ensureNotPresent(KEY_DROP_EXP, this.dropExp);
                ensureNotPresent(KEY_REVIVE_HEALTH, this.reviveHealth);
                ensureNotPresent(KEY_SHOULD_PLAY_DEATH_SOUND, this.shouldPlayDeathSound);
                ensureNotPresent(KEY_DEATH_SOUND, this.deathSound);
                ensureNotPresent(KEY_DEATH_SOUND_CATEGORY, this.deathSoundCategory);
                ensureNotPresent(KEY_DEATH_SOUND_VOLUME, this.deathSoundVolume);
                ensureNotPresent(KEY_DEATH_SOUND_PITCH, this.deathSoundPitch);
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_DROPS, this.drops,
                    KEY_DROP_EXP, this.dropExp,
                    KEY_REVIVE_HEALTH, this.reviveHealth,
                    KEY_SHOULD_PLAY_DEATH_SOUND, this.shouldPlayDeathSound,
                    KEY_DEATH_SOUND, this.deathSound,
                    KEY_DEATH_SOUND_CATEGORY, this.deathSoundCategory,
                    KEY_DEATH_SOUND_VOLUME, this.deathSoundVolume,
                    KEY_DEATH_SOUND_PITCH, this.deathSoundPitch
            );
        }
    }
}
