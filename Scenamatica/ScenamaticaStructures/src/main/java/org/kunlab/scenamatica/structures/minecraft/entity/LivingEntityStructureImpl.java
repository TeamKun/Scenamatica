package org.kunlab.scenamatica.structures.minecraft.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.LivingEntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;
import org.kunlab.scenamatica.structures.minecraft.inventory.ItemStackStructureImpl;
import org.kunlab.scenamatica.structures.specifiers.EntitySpecifierImpl;
import org.kunlab.scenamatica.structures.specifiers.PlayerSpecifierImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class LivingEntityStructureImpl extends EntityStructureImpl implements LivingEntityStructure
{
    protected final Double eyeHeight;
    protected final Integer remainAir;
    protected final Integer maxAir;
    protected final Integer arrowCooldown;
    protected final Integer arrowsInBody;
    protected final Integer maxNoDamageTicks;
    protected final Double lastDamage;
    protected final Integer noDamageTicks;
    @NotNull
    protected final PlayerSpecifier killer;
    @NotNull
    protected final List<PotionEffect> potionEffects;
    protected final Boolean removeWhenFarAway;
    protected final Boolean canPickupItems;
    protected final Boolean leashed;
    @NotNull
    protected final EntitySpecifier<Entity> leashHolder;
    protected final Boolean gliding;
    protected final Boolean swimming;
    protected final Boolean riptiding;
    protected final Boolean sleeping;
    protected final Boolean ai;
    protected final Boolean collidable;
    protected final Boolean invisible;

    // Paper

    protected final Integer arrowsStuck;
    protected final Integer shieldBlockingDelay;
    protected final ItemStackStructure activeItem;
    protected final Integer itemUseRemainTime;
    protected final Integer handRaisedTime;
    protected final Boolean isHandRaised;


    // <editor-fold desc="Constructors">

    protected LivingEntityStructureImpl(
            EntityStructure original,
            Double eyeHeight,
            Integer remainAir,
            Integer maxAir,
            Integer arrowCooldown,
            Integer arrowsInBody,
            Integer maxNoDamageTicks,
            Double lastDamage,
            Integer noDamageTicks,
            @NotNull PlayerSpecifier killer,
            @NotNull List<PotionEffect> potionEffects,
            Boolean removeWhenFarAway,
            Boolean canPickupItems,
            Boolean leashed,
            @NotNull EntitySpecifier<Entity> leashHolder,
            Boolean gliding,
            Boolean swimming,
            Boolean riptiding,
            Boolean sleeping,
            Boolean ai,
            Boolean collidable,
            Boolean invisible,
            Integer arrowsStuck,
            Integer shieldBlockingDelay,
            ItemStackStructure activeItem,
            Integer itemUseRemainTime,
            Integer handRaisedTime,
            Boolean isHandRaised)
    {
        super(original);
        this.eyeHeight = eyeHeight;
        this.remainAir = remainAir;
        this.maxAir = maxAir;
        this.arrowCooldown = arrowCooldown;
        this.arrowsInBody = arrowsInBody;
        this.maxNoDamageTicks = maxNoDamageTicks;
        this.lastDamage = lastDamage;
        this.noDamageTicks = noDamageTicks;
        this.killer = killer;
        this.potionEffects = potionEffects;
        this.removeWhenFarAway = removeWhenFarAway;
        this.canPickupItems = canPickupItems;
        this.leashed = leashed;
        this.leashHolder = leashHolder;
        this.gliding = gliding;
        this.swimming = swimming;
        this.riptiding = riptiding;
        this.sleeping = sleeping;
        this.ai = ai;
        this.collidable = collidable;
        this.invisible = invisible;
        this.arrowsStuck = arrowsStuck;
        this.shieldBlockingDelay = shieldBlockingDelay;
        this.activeItem = activeItem;
        this.itemUseRemainTime = itemUseRemainTime;
        this.handRaisedTime = handRaisedTime;
        this.isHandRaised = isHandRaised;
    }

    protected LivingEntityStructureImpl()
    {
        this(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                PlayerSpecifierImpl.EMPTY,
                Collections.emptyList(),
                null,
                null,
                null,
                EntitySpecifierImpl.EMPTY,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    protected LivingEntityStructureImpl(@NotNull LivingEntityStructure original)
    {
        this(original.getType(), original);
    }

    protected LivingEntityStructureImpl(@NotNull EntityType type, @NotNull LivingEntityStructure original)
    {
        super(type, original);

        this.eyeHeight = original.getEyeHeight();
        this.remainAir = original.getRemainAir();
        this.maxAir = original.getMaxAir();
        this.arrowCooldown = original.getArrowCooldown();
        this.arrowsInBody = original.getArrowsInBody();
        this.maxNoDamageTicks = original.getMaxNoDamageTicks();
        this.lastDamage = original.getLastDamage();
        this.noDamageTicks = original.getNoDamageTicks();
        this.killer = original.getKiller();
        this.potionEffects = original.getPotionEffects();
        this.removeWhenFarAway = original.getRemoveWhenFarAway();
        this.canPickupItems = original.getCanPickupItems();
        this.leashed = original.getLeashed();
        this.leashHolder = original.getLeashHolder();
        this.gliding = original.getGliding();
        this.swimming = original.getSwimming();
        this.riptiding = original.getRiptiding();
        this.sleeping = original.getSleeping();
        this.ai = original.getAi();
        this.collidable = original.getCollidable();
        this.invisible = original.getInvisible();
        this.arrowsStuck = original.getArrowsStuck();
        this.shieldBlockingDelay = original.getShieldBlockingDelay();
        this.activeItem = original.getActiveItem();
        this.itemUseRemainTime = original.getItemUseRemainTime();
        this.handRaisedTime = original.getHandRaisedTime();
        this.isHandRaised = original.getIsHandRaised();
    }


    // </editor-fold>

    /**
     * エンティティ情報をMapにシリアライズします。
     *
     * @param entity     エンティティ情報
     * @param serializer シリアライザ
     * @return エンティティ情報をシリアライズしたMap
     */
    public static Map<String, Object> serializeLivingEntity(LivingEntityStructure entity, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = EntityStructureImpl.serialize(entity, serializer);
        MapUtils.putIfNotNull(map, KEY_EYE_HEIGHT, entity.getEyeHeight());
        MapUtils.putIfNotNull(map, KEY_REMAINING_AIR, entity.getRemainAir());
        MapUtils.putIfNotNull(map, KEY_MAX_AIR, entity.getMaxAir());
        MapUtils.putIfNotNull(map, KEY_ARROW_COOLDOWN, entity.getArrowCooldown());
        MapUtils.putIfNotNull(map, KEY_ARROWS_IN_BODY, entity.getArrowsInBody());
        MapUtils.putIfNotNull(map, KEY_MAX_NO_DAMAGE_TICKS, entity.getMaxNoDamageTicks());
        MapUtils.putIfNotNull(map, KEY_LAST_DAMAGE, entity.getLastDamage());
        MapUtils.putIfNotNull(map, KEY_NO_DAMAGE_TICKS, entity.getNoDamageTicks());
        if (entity.getKiller().canProvideTarget())
            map.put(KEY_KILLER, entity.getKiller().getTargetRaw());

        if (!entity.getPotionEffects().isEmpty())
            map.put(KEY_POTION_EFFECTS, serializePotionEffects(entity.getPotionEffects()));

        MapUtils.putIfNotNull(map, KEY_REMOVE_WHEN_FAR_AWAY, entity.getRemoveWhenFarAway());
        MapUtils.putIfNotNull(map, KEY_CAN_PICKUP_ITEMS, entity.getCanPickupItems());
        MapUtils.putIfNotNull(map, KEY_LEASHED, entity.getLeashed());
        if (entity.getLeashHolder().canProvideTarget())
            map.put(KEY_LEASH_HOLDER, entity.getLeashHolder().getTargetRaw());
        MapUtils.putIfNotNull(map, KEY_GLIDING, entity.getGliding());
        MapUtils.putIfNotNull(map, KEY_SWIMMING, entity.getSwimming());
        MapUtils.putIfNotNull(map, KEY_RIPTIDING, entity.getRiptiding());
        MapUtils.putIfNotNull(map, KEY_SLEEPING, entity.getSleeping());
        MapUtils.putIfNotNull(map, KEY_AI, entity.getAi());
        MapUtils.putIfNotNull(map, KEY_COLLIDABLE, entity.getCollidable());
        MapUtils.putIfNotNull(map, KEY_INVISIBLE, entity.getInvisible());
        // Paper
        MapUtils.putIfNotNull(map, KEY_ARROWS_STUCK, entity.getArrowsStuck());
        MapUtils.putIfNotNull(map, KEY_SHIELD_BLOCKING_DELAY, entity.getShieldBlockingDelay());
        if (entity.getActiveItem() != null)
            MapUtils.putIfNotNull(map, KEY_ACTIVE_ITEM, serializer.serialize(entity.getActiveItem(), ItemStackStructure.class));
        MapUtils.putIfNotNull(map, KEY_ITEM_USE_REMAIN_TIME, entity.getItemUseRemainTime());
        MapUtils.putIfNotNull(map, KEY_HAND_RAISED_TIME, entity.getHandRaisedTime());
        MapUtils.putIfNotNull(map, KEY_IS_HAND_RAISED, entity.getIsHandRaised());

        return map;
    }

    public static void validateLivingEntity(@NotNull StructuredYamlNode node) throws YamlParsingException
    {
        EntityStructureImpl.validate(node);

        node.get(KEY_REMAINING_AIR).ensureTypeOf(YAMLNodeType.NUMBER);
        node.get(KEY_MAX_AIR).ensureTypeOf(YAMLNodeType.NUMBER);
        node.get(KEY_ARROW_COOLDOWN).ensureTypeOf(YAMLNodeType.NUMBER);
        node.get(KEY_ARROWS_IN_BODY).ensureTypeOf(YAMLNodeType.NUMBER);
        node.get(KEY_MAX_NO_DAMAGE_TICKS).ensureTypeOf(YAMLNodeType.NUMBER);
        node.get(KEY_LAST_DAMAGE).ensureTypeOf(YAMLNodeType.NUMBER);
        node.get(KEY_NO_DAMAGE_TICKS).ensureTypeOf(YAMLNodeType.NUMBER);

        if (node.containsKey(KEY_POTION_EFFECTS))
        {
            StructuredYamlNode potionEffectsNode = node.get(KEY_POTION_EFFECTS);
            potionEffectsNode.ensureTypeOf(YAMLNodeType.LIST);
            validatePotionEffectNodes(potionEffectsNode.asList());
        }

        node.get(KEY_REMOVE_WHEN_FAR_AWAY).ensureTypeOf(YAMLNodeType.BOOLEAN);
        node.get(KEY_CAN_PICKUP_ITEMS).ensureTypeOf(YAMLNodeType.BOOLEAN);
        node.get(KEY_LEASHED).ensureTypeOf(YAMLNodeType.BOOLEAN);
        node.get(KEY_GLIDING).ensureTypeOf(YAMLNodeType.BOOLEAN);
        node.get(KEY_SWIMMING).ensureTypeOf(YAMLNodeType.BOOLEAN);
        node.get(KEY_RIPTIDING).ensureTypeOf(YAMLNodeType.BOOLEAN);
        node.get(KEY_SLEEPING).ensureTypeOf(YAMLNodeType.BOOLEAN);
        node.get(KEY_AI).ensureTypeOf(YAMLNodeType.BOOLEAN);
        node.get(KEY_COLLIDABLE).ensureTypeOf(YAMLNodeType.BOOLEAN);
        node.get(KEY_INVISIBLE).ensureTypeOf(YAMLNodeType.BOOLEAN);
        // Paper

        node.get(KEY_ARROWS_STUCK).ensureTypeOf(YAMLNodeType.NUMBER);
        node.get(KEY_SHIELD_BLOCKING_DELAY).ensureTypeOf(YAMLNodeType.NUMBER);
        if (node.containsKey(KEY_ACTIVE_ITEM))
        {
            StructuredYamlNode activeItemNode = node.get(KEY_ACTIVE_ITEM);
            activeItemNode.ensureTypeOf(YAMLNodeType.MAPPING);
            ItemStackStructureImpl.validate(activeItemNode);
        }
        node.get(KEY_ITEM_USE_REMAIN_TIME).ensureTypeOf(YAMLNodeType.NUMBER);
        node.get(KEY_HAND_RAISED_TIME).ensureTypeOf(YAMLNodeType.NUMBER);
        node.get(KEY_IS_HAND_RAISED).ensureTypeOf(YAMLNodeType.BOOLEAN);
        node.get(KEY_JUMPING).ensureTypeOf(YAMLNodeType.BOOLEAN);
        node.get(KEY_HURT_DIRECTION).ensureTypeOf(YAMLNodeType.NUMBER);
    }

    /**
     * Mapからエンティティ情報をデシリアライズします。
     *
     * @param node デシリアライズするMap
     * @return デシリアライズしたエンティティ情報
     */
    @NotNull
    public static LivingEntityStructure deserializeLivingEntity(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        validateLivingEntity(node);

        EntityStructure base = EntityStructureImpl.deserialize(node, serializer);

        Double eyeHeight = node.get(KEY_EYE_HEIGHT).asDouble(null);
        Integer remainAir = node.get(KEY_REMAINING_AIR).asInteger(null);
        Integer maxAir = node.get(KEY_MAX_AIR).asInteger(null);
        Integer arrowCooldown = node.get(KEY_ARROW_COOLDOWN).asInteger(null);
        Integer arrowsInBody = node.get(KEY_ARROWS_IN_BODY).asInteger(null);
        Integer maxNoDamageTicks = node.get(KEY_MAX_NO_DAMAGE_TICKS).asInteger(null);
        Double lastDamage = node.get(KEY_LAST_DAMAGE).asDouble(null);
        Integer noDamageTicks = node.get(KEY_NO_DAMAGE_TICKS).asInteger(null);
        PlayerSpecifier killer = serializer.tryDeserializePlayerSpecifier(node.get(KEY_KILLER));

        List<PotionEffect> potionEffects = new ArrayList<>();
        if (node.containsKey(KEY_POTION_EFFECTS))
            potionEffects = deserializePotionEffects(node.get(KEY_POTION_EFFECTS));

        Boolean removeWhenFarAway = node.get(KEY_REMOVE_WHEN_FAR_AWAY).asBoolean(null);
        Boolean canPickupItems = node.get(KEY_CAN_PICKUP_ITEMS).asBoolean(null);
        Boolean leashed = node.get(KEY_LEASHED).asBoolean(null);
        EntitySpecifier<Entity> leashHolder = serializer.tryDeserializeEntitySpecifier(node.get(KEY_LEASH_HOLDER));
        Boolean gliding = node.get(KEY_GLIDING).asBoolean(null);
        Boolean swimming = node.get(KEY_SWIMMING).asBoolean(null);
        Boolean riptiding = node.get(KEY_RIPTIDING).asBoolean(null);
        Boolean sleeping = node.get(KEY_SLEEPING).asBoolean(null);
        Boolean ai = node.get(KEY_AI).asBoolean(null);
        Boolean collidable = node.get(KEY_COLLIDABLE).asBoolean(null);
        Boolean invisible = node.get(KEY_INVISIBLE).asBoolean(null);
        // Paper

        Integer arrowsStuck = node.get(KEY_ARROWS_STUCK).asInteger(null);
        Integer shieldBlockingDelay = node.get(KEY_SHIELD_BLOCKING_DELAY).asInteger(null);
        ItemStackStructure activeItem = null;
        if (node.containsKey(KEY_ACTIVE_ITEM))
            activeItem = serializer.deserialize(node.get(KEY_ACTIVE_ITEM), ItemStackStructure.class);

        Integer itemUseRemainTime = node.get(KEY_ITEM_USE_REMAIN_TIME).asInteger(null);
        Integer handRaisedTime = node.get(KEY_HAND_RAISED_TIME).asInteger(null);
        Boolean isHandRaised = node.get(KEY_IS_HAND_RAISED).asBoolean(null);

        return new LivingEntityStructureImpl(
                base,
                eyeHeight,
                remainAir,
                maxAir,
                arrowCooldown,
                arrowsInBody,
                maxNoDamageTicks,
                lastDamage,
                noDamageTicks,
                killer,
                potionEffects,
                removeWhenFarAway,
                canPickupItems,
                leashed,
                leashHolder,
                gliding,
                swimming,
                riptiding,
                sleeping,
                ai,
                collidable,
                invisible,
                arrowsStuck,
                shieldBlockingDelay,
                activeItem,
                itemUseRemainTime,
                handRaisedTime,
                isHandRaised
        );
    }

    public static LivingEntityStructure ofLivingEntity(@NotNull LivingEntity entity)
    {
        NMSEntityLiving nmsEntity = NMSProvider.getProvider().wrap(entity);

        return new LivingEntityStructureImpl(
                EntityStructureImpl.of(entity),
                entity.getEyeHeight(),
                entity.getRemainingAir(),
                entity.getMaximumAir(),
                NMSProvider.doNMSSafe(nmsEntity::getArrowCooldown),
                nmsEntity.getArrowCount(),
                entity.getMaximumNoDamageTicks(),
                entity.getLastDamage(),
                entity.getNoDamageTicks(),
                entity.getKiller() == null ? PlayerSpecifierImpl.EMPTY: PlayerSpecifierImpl.ofPlayer(entity.getKiller()),
                new ArrayList<>(entity.getActivePotionEffects()),
                entity.getRemoveWhenFarAway(),
                entity.getCanPickupItems(),
                entity.isLeashed(),
                entity.isLeashed() ? EntitySpecifierImpl.of(entity.getLeashHolder()): EntitySpecifierImpl.EMPTY,
                entity.isGliding(),
                entity.isSwimming(),
                entity.isRiptiding(),
                nmsEntity.isSleeping(),
                entity.hasAI(),
                entity.isCollidable(),
                nmsEntity.isInvisible(),
                // Paper
                entity.getArrowsStuck(),
                entity.getShieldBlockingDelay(),
                ItemStackStructureImpl.of(Objects.requireNonNull(entity.getEquipment()).getItemInMainHand()),
                entity.getItemUseRemainingTime(),
                entity.getHandRaisedTime(),
                entity.isHandRaised()
        );
    }

    private static List<Map<String, Object>> serializePotionEffects(@NotNull List<? extends PotionEffect> potionEffects)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PotionEffect potionEffect : potionEffects)
        {
            Map<String, Object> potionEffectMap = new HashMap<>();

            if (!potionEffect.isAmbient())
                potionEffectMap.put(KEY_POTION_EFFECTS_AMBIENT, false);
            if (potionEffect.getAmplifier() != 0)
                potionEffectMap.put(KEY_POTION_EFFECTS_AMPLIFIER, potionEffect.getAmplifier());
            if (!potionEffect.hasParticles())
                potionEffectMap.put(KEY_POTION_EFFECTS_SHOW_PARTICLES, false);
            if (!potionEffect.hasIcon())
                potionEffectMap.put(KEY_POTION_EFFECTS_SHOW_ICON, false);
            if (potionEffect.getDuration() != 0)
                potionEffectMap.put(KEY_POTION_EFFECTS_DURATION, potionEffect.getDuration());
            potionEffectMap.put(KEY_POTION_EFFECTS_TYPE, potionEffect.getType().getName());

            list.add(potionEffectMap);
        }

        return list;
    }

    private static void validatePotionEffectNodes(@NotNull List<StructuredYamlNode> nodes) throws YamlParsingException
    {
        if (nodes.isEmpty())
            return;

        for (StructuredYamlNode node : nodes)
        {
            node.ensureTypeOf(YAMLNodeType.MAPPING);
            StructuredYamlNode typeNode = node.get(KEY_POTION_EFFECTS_TYPE);
            typeNode.ensureTypeOf(YAMLNodeType.STRING);
            typeNode.validate(n -> {
                if (PotionEffectType.getByName(n.asString()) == null)
                    throw new IllegalArgumentException("Invalid potion effect type.");
                return null;
            }, "Invalid potion effect type.");

            node.get(KEY_POTION_EFFECTS_AMBIENT).ensureTypeOf(YAMLNodeType.BOOLEAN);
            node.get(KEY_POTION_EFFECTS_AMPLIFIER).ensureTypeOf(YAMLNodeType.NUMBER);
            node.get(KEY_POTION_EFFECTS_DURATION).ensureTypeOf(YAMLNodeType.NUMBER);
            node.get(KEY_POTION_EFFECTS_SHOW_ICON).ensureTypeOf(YAMLNodeType.BOOLEAN);
            node.get(KEY_POTION_EFFECTS_SHOW_PARTICLES).ensureTypeOf(YAMLNodeType.BOOLEAN);
        }
    }

    private static List<PotionEffect> deserializePotionEffects(@NotNull StructuredYamlNode potionEffects) throws YamlParsingException
    {
        List<PotionEffect> list = new ArrayList<>();
        for (StructuredYamlNode effectMap : potionEffects.asList())
        {
            PotionEffectType type =
                    PotionEffectType.getByName(effectMap.get(KEY_POTION_EFFECTS_TYPE).asString());

            long duration = 0L;
            if (effectMap.containsKey(KEY_POTION_EFFECTS_DURATION))
                duration = Long.parseLong(effectMap.get(KEY_POTION_EFFECTS_DURATION).toString());
            int amplifier = effectMap.get(KEY_POTION_EFFECTS_AMPLIFIER).asInteger(0);
            boolean ambient = effectMap.get(KEY_POTION_EFFECTS_AMBIENT).asBoolean(true);
            boolean particles = effectMap.get(KEY_POTION_EFFECTS_SHOW_PARTICLES).asBoolean(true);
            boolean icon = effectMap.get(KEY_POTION_EFFECTS_SHOW_ICON).asBoolean(true);

            assert type != null;  // validatePotionEffectMapで検証済み
            list.add(new PotionEffect(type, (int) duration, amplifier, ambient, particles, icon));
        }

        return list;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof LivingEntityStructureImpl)) return false;
        if (!super.equals(o)) return false;
        LivingEntityStructureImpl that = (LivingEntityStructureImpl) o;

        return Objects.equals(this.eyeHeight, that.eyeHeight)
                && Objects.equals(this.remainAir, that.remainAir)
                && Objects.equals(this.maxAir, that.maxAir)
                && Objects.equals(this.arrowCooldown, that.arrowCooldown)
                && Objects.equals(this.arrowsInBody, that.arrowsInBody)
                && Objects.equals(this.maxNoDamageTicks, that.maxNoDamageTicks)
                && Objects.equals(this.lastDamage, that.lastDamage)
                && Objects.equals(this.noDamageTicks, that.noDamageTicks)
                && Objects.equals(this.killer, that.killer)
                && Objects.equals(this.potionEffects, that.potionEffects)
                && Objects.equals(this.removeWhenFarAway, that.removeWhenFarAway)
                && Objects.equals(this.canPickupItems, that.canPickupItems)
                && Objects.equals(this.leashed, that.leashed)
                && Objects.equals(this.leashHolder, that.leashHolder)
                && Objects.equals(this.gliding, that.gliding)
                && Objects.equals(this.swimming, that.swimming)
                && Objects.equals(this.riptiding, that.riptiding)
                && Objects.equals(this.sleeping, that.sleeping)
                && Objects.equals(this.ai, that.ai)
                && Objects.equals(this.collidable, that.collidable)
                && Objects.equals(this.invisible, that.invisible)
                && Objects.equals(this.arrowsStuck, that.arrowsStuck)
                && Objects.equals(this.shieldBlockingDelay, that.shieldBlockingDelay)
                && Objects.equals(this.activeItem, that.activeItem)
                && Objects.equals(this.itemUseRemainTime, that.itemUseRemainTime)
                && Objects.equals(this.handRaisedTime, that.handRaisedTime)
                && Objects.equals(this.isHandRaised, that.isHandRaised);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(
                super.hashCode(), this.eyeHeight, this.remainAir, this.maxAir, this.arrowCooldown,
                this.arrowsInBody, this.maxNoDamageTicks, this.lastDamage, this.noDamageTicks, this.killer,
                this.potionEffects, this.removeWhenFarAway, this.canPickupItems, this.leashed, this.leashHolder,
                this.gliding, this.swimming, this.riptiding, this.sleeping, this.ai, this.collidable, this.invisible,
                this.arrowsStuck, this.shieldBlockingDelay, this.activeItem, this.itemUseRemainTime,
                this.handRaisedTime, this.isHandRaised
        );
    }

    @Override
    public void applyTo(@NotNull Entity entity, boolean applyLocation)
    {
        super.applyTo(entity, true);
        if (!(entity instanceof LivingEntity))
            return;

        LivingEntity livingEntity = (LivingEntity) entity;
        NMSEntityLiving nmsEntity = NMSProvider.getProvider().wrap(livingEntity);

        if (this.remainAir != null)
            livingEntity.setRemainingAir(this.remainAir);
        if (this.maxAir != null)
            livingEntity.setMaximumAir(this.maxAir);
        if (this.arrowCooldown != null)
            NMSProvider.tryDoNMS(() -> nmsEntity.setArrowCooldown(this.arrowCooldown));
        if (this.arrowsInBody != null)
            nmsEntity.setArrowCount(this.arrowsInBody);
        if (this.maxNoDamageTicks != null)
            livingEntity.setMaximumNoDamageTicks(this.maxNoDamageTicks);
        if (this.lastDamage != null)
            livingEntity.setLastDamage(this.lastDamage);
        if (this.noDamageTicks != null)
            livingEntity.setNoDamageTicks(this.noDamageTicks);
        if (this.killer.canProvideTarget())
            livingEntity.setKiller(this.killer.selectTarget(null).orElse(null));
        if (!this.potionEffects.isEmpty())
        {
            new ArrayList<>(livingEntity.getActivePotionEffects()).stream()
                    .map(PotionEffect::getType)
                    .forEach(((LivingEntity) livingEntity)::removePotionEffect);

            this.potionEffects.stream()
                    .map(b -> new PotionEffect(
                                    b.getType(),
                                    b.getDuration(),
                                    b.getAmplifier(),
                                    b.isAmbient(),
                                    b.hasParticles(),
                                    b.hasIcon()
                            )
                    )
                    .forEach(((LivingEntity) livingEntity)::addPotionEffect);
        }
        if (this.removeWhenFarAway != null)
            livingEntity.setRemoveWhenFarAway(this.removeWhenFarAway);
        if (this.canPickupItems != null)
            livingEntity.setCanPickupItems(this.canPickupItems);
        if (this.leashHolder.canProvideTarget())
            livingEntity.setLeashHolder(this.leashHolder.selectTarget(null).orElse(null));
        if (this.gliding != null)
            livingEntity.setGliding(this.gliding);
        if (this.swimming != null)
            livingEntity.setSwimming(this.swimming);
        if (this.ai != null)
            livingEntity.setAI(this.ai);
        if (this.collidable != null)
            livingEntity.setCollidable(this.collidable);
        if (this.invisible != null)
            nmsEntity.setInvisible(this.invisible);
        // Paper

        if (this.arrowsStuck != null)
            livingEntity.setArrowsStuck(this.arrowsStuck);
        if (this.shieldBlockingDelay != null)
            livingEntity.setShieldBlockingDelay(this.shieldBlockingDelay);
    }

    @Override
    public boolean isAdequate(@Nullable Entity entity, boolean strict)
    {
        if (!(super.isAdequate(entity, strict) && entity instanceof LivingEntity))
            return false;

        LivingEntity livingEntity = (LivingEntity) entity;

        if (!this.potionEffects.isEmpty())
        {
            List<PotionEffect> potionEffects = new ArrayList<>(livingEntity.getActivePotionEffects());
            if (strict && potionEffects.size() != this.potionEffects.size())
                return false;

            for (PotionEffect effects : this.potionEffects)
                if (!potionEffects.contains(effects))
                    return false;
        }

        NMSEntityLiving nmsEntity = NMSProvider.getProvider().wrap(livingEntity);

        boolean arrowCooldownMatches = true;
        if (this.arrowCooldown != null)
        {
            Integer nmsArrowCooldown = NMSProvider.doNMSSafe(nmsEntity::getArrowCooldown);
            if (nmsArrowCooldown != null)
                arrowCooldownMatches = Objects.equals(this.arrowCooldown, nmsArrowCooldown);
        }
        return (this.eyeHeight == null || this.eyeHeight.equals(livingEntity.getEyeHeight()))
                && (this.remainAir == null || this.remainAir == livingEntity.getRemainingAir())
                && (this.maxAir == null || this.maxAir == livingEntity.getMaximumAir())
                && arrowCooldownMatches
                && (this.arrowsInBody == null || this.arrowsInBody == nmsEntity.getArrowCount())
                && (this.maxNoDamageTicks == null || this.maxNoDamageTicks == livingEntity.getMaximumNoDamageTicks())
                && (this.lastDamage == null || this.lastDamage == livingEntity.getLastDamage())
                && (this.noDamageTicks == null || this.noDamageTicks == livingEntity.getNoDamageTicks())
                && (!this.killer.canProvideTarget() || this.killer.checkMatchedPlayer(livingEntity.getKiller()))
                && (this.removeWhenFarAway == null || this.removeWhenFarAway == livingEntity.getRemoveWhenFarAway())
                && (this.canPickupItems == null || this.canPickupItems == livingEntity.getCanPickupItems())
                && (this.leashed == null || this.leashed == livingEntity.isLeashed())
                && (!this.leashHolder.canProvideTarget() || this.leashHolder.checkMatchedEntity(livingEntity.getLeashHolder()))
                && (this.gliding == null || this.gliding == livingEntity.isGliding())
                && (this.swimming == null || this.swimming == livingEntity.isSwimming())
                && (this.ai == null || this.ai == livingEntity.hasAI())
                && (this.collidable == null || this.collidable == livingEntity.isCollidable())
                && (this.invisible == null || this.invisible == nmsEntity.isInvisible())
                // Paper
                && (this.arrowsStuck == null || this.arrowsStuck == livingEntity.getArrowsStuck())
                && (this.shieldBlockingDelay == null || this.shieldBlockingDelay == livingEntity.getShieldBlockingDelay());
    }


}
