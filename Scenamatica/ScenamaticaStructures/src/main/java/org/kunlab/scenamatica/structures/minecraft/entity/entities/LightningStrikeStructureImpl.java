package org.kunlab.scenamatica.structures.minecraft.entity.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.DamageStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.LightningStrikeStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.entity.NMSLightningStrike;
import org.kunlab.scenamatica.structures.minecraft.entity.EntityStructureImpl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LightningStrikeStructureImpl extends EntityStructureImpl implements LightningStrikeStructure
{
    protected final Boolean effect;

    public LightningStrikeStructureImpl(EntityStructure entityStructure, Boolean isEffect)
    {
        super(entityStructure);
        this.effect = isEffect;
    }

    public LightningStrikeStructureImpl(@Nullable EntityType type, @NotNull EntityStructure original, Boolean effect)
    {
        super(type, original);
        this.effect = effect;
    }

    public LightningStrikeStructureImpl(EntityType type, LocationStructure location, Vector velocity, String customName,
                                        UUID uuid, Boolean glowing, Boolean gravity, Boolean silent,
                                        Boolean customNameVisible, Boolean invulnerable, @NotNull List<String> tags,
                                        Integer maxHealth, Integer health, DamageStructure lastDamageCause,
                                        Integer fireTicks, Integer ticksLived, Integer portalCooldown,
                                        Boolean persistent, Float fallDistance, Boolean effect)
    {
        super(
                type, location, velocity, customName, uuid, glowing, gravity, silent, customNameVisible, invulnerable,
                tags, maxHealth, health, lastDamageCause, fireTicks, ticksLived, portalCooldown, persistent,
                fallDistance
        );
        this.effect = effect;
    }

    public LightningStrikeStructureImpl()
    {
        super();
        this.effect = null;
    }

    public static LightningStrikeStructure ofLightning(@NotNull LightningStrike strike)
    {
        return new LightningStrikeStructureImpl(
                EntityStructureImpl.of(strike),
                strike.isEffect()
        );
    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull LightningStrikeStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = EntityStructureImpl.serialize(structure, serializer);
        map.put(LightningStrikeStructure.KEY_EFFECT, structure.isEffect());
        return map;
    }

    public static void validate(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        EntityStructureImpl.validate(node);
        node.ensureTypeOfIfExists(YAMLNodeType.BOOLEAN);
    }

    @NotNull
    public static LightningStrikeStructure deserialize(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        validate(node);
        return new LightningStrikeStructureImpl(
                EntityStructureImpl.deserialize(node, serializer),
                node.get(LightningStrikeStructure.KEY_EFFECT).asBoolean(null)
        );
    }

    @Override
    public Boolean isEffect()
    {
        return this.effect;
    }

    @Override
    public boolean isAdequate(@NotNull Entity object)
    {
        if (!(super.isAdequate(object) && object instanceof LightningStrike))
            return false;

        LightningStrike strike = (LightningStrike) object;
        return (this.isEffect() == null || this.isEffect().equals(strike.isEffect()));
    }

    @Override
    public void applyTo(@NotNull Entity object)
    {
        super.applyTo(object);
        LightningStrike strike = (LightningStrike) object;
        NMSLightningStrike nmsStrike = NMSProvider.getProvider().wrap(strike);

        if (this.isEffect() != null)
            nmsStrike.setVisualOnly(this.isEffect());
    }

    @Override
    public boolean canApplyTo(@Nullable Object target)
    {
        return target instanceof LightningStrike;
    }
}
