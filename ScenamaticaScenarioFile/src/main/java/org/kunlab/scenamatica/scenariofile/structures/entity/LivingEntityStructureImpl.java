package org.kunlab.scenamatica.scenariofile.structures.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.specifiers.EntitySpecifierImpl;
import org.kunlab.scenamatica.commons.specifiers.PlayerSpecifierImpl;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.LivingEntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.scenariofile.structures.inventory.ItemStackStructureImpl;

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
    protected final EquipmentSlot handRaised;
    protected final Boolean jumping;
    protected final Float hurtDirection;


    // <editor-fold desc="Constructors">

    protected LivingEntityStructureImpl(
            EntityStructure original,
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
            Boolean isHandRaised,
            EquipmentSlot handRaised,
            Boolean jumping,
            Float hurtDirection)
    {
        super(original);
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
        this.handRaised = handRaised;
        this.jumping = jumping;
        this.hurtDirection = hurtDirection;
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
        this.handRaised = original.getHandRaised();
        this.jumping = original.getJumping();
        this.hurtDirection = original.getHurtDirection();
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
        if (entity.getHandRaised() != null)
            map.put(KEY_HAND_RAISED, entity.getHandRaised().name());
        MapUtils.putIfNotNull(map, KEY_JUMPING, entity.getJumping());
        MapUtils.putIfNotNull(map, KEY_HURT_DIRECTION, entity.getHurtDirection());

        return map;
    }

    /**
     * Mapが正しいエンティティ情報かどうかを検証します。
     *
     * @param map 検証するMap
     * @throws IllegalArgumentException Mapが正しいエンティティ情報ではない場合
     */
    public static void validateLivingEntity(@NotNull Map<String, Object> map)
    {
        EntityStructureImpl.validate(map);

        MapUtils.checkNumberIfContains(map, KEY_REMAINING_AIR);
        MapUtils.checkNumberIfContains(map, KEY_MAX_AIR);
        MapUtils.checkNumberIfContains(map, KEY_ARROW_COOLDOWN);
        MapUtils.checkNumberIfContains(map, KEY_ARROWS_IN_BODY);
        MapUtils.checkNumberIfContains(map, KEY_MAX_NO_DAMAGE_TICKS);
        MapUtils.checkNumberIfContains(map, KEY_LAST_DAMAGE);
        MapUtils.checkNumberIfContains(map, KEY_NO_DAMAGE_TICKS);

        if (map.containsKey(KEY_POTION_EFFECTS))
        {
            MapUtils.checkType(map, KEY_POTION_EFFECTS, List.class);
            validatePotionEffectMap(MapUtils.getAsList(map, KEY_POTION_EFFECTS));
        }

        MapUtils.checkTypeIfContains(map, KEY_REMOVE_WHEN_FAR_AWAY, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_CAN_PICKUP_ITEMS, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_LEASHED, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_GLIDING, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_SWIMMING, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_RIPTIDING, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_SLEEPING, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_AI, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_COLLIDABLE, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_INVISIBLE, Boolean.class);
        // Paper

        MapUtils.checkNumberIfContains(map, KEY_ARROWS_STUCK);
        MapUtils.checkNumberIfContains(map, KEY_SHIELD_BLOCKING_DELAY);
        if (map.containsKey(KEY_ACTIVE_ITEM))
        {
            MapUtils.checkType(map, KEY_ACTIVE_ITEM, Map.class);
            ItemStackStructureImpl.validate(MapUtils.checkAndCastMap(map.get(KEY_ACTIVE_ITEM)));
        }
        MapUtils.checkNumberIfContains(map, KEY_ITEM_USE_REMAIN_TIME);
        MapUtils.checkNumberIfContains(map, KEY_HAND_RAISED_TIME);
        MapUtils.checkTypeIfContains(map, KEY_IS_HAND_RAISED, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_HAND_RAISED, String.class);
        MapUtils.checkTypeIfContains(map, KEY_JUMPING, Boolean.class);
        MapUtils.checkNumberIfContains(map, KEY_HURT_DIRECTION);
    }

    /**
     * Mapからエンティティ情報をデシリアライズします。
     *
     * @param map デシリアライズするMap
     * @return デシリアライズしたエンティティ情報
     */
    @NotNull
    public static LivingEntityStructure deserializeLivingEntity(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validateLivingEntity(map);

        EntityStructure base = EntityStructureImpl.deserialize(map, serializer);

        Integer remainAir = MapUtils.getOrNull(map, KEY_REMAINING_AIR);
        Integer maxAir = MapUtils.getOrNull(map, KEY_MAX_AIR);
        Integer arrowCooldown = MapUtils.getOrNull(map, KEY_ARROW_COOLDOWN);
        Integer arrowsInBody = MapUtils.getOrNull(map, KEY_ARROWS_IN_BODY);
        Integer maxNoDamageTicks = MapUtils.getOrNull(map, KEY_MAX_NO_DAMAGE_TICKS);
        Double lastDamage = MapUtils.getOrNull(map, KEY_LAST_DAMAGE);
        Integer noDamageTicks = MapUtils.getOrNull(map, KEY_NO_DAMAGE_TICKS);
        PlayerSpecifier killer = PlayerSpecifierImpl.tryDeserializePlayer(map.get(KEY_KILLER), serializer);

        List<PotionEffect> potionEffects = new ArrayList<>();
        if (map.containsKey(KEY_POTION_EFFECTS))
            potionEffects = deserializePotionEffects(MapUtils.getAsList(map, KEY_POTION_EFFECTS));

        Boolean removeWhenFarAway = MapUtils.getOrNull(map, KEY_REMOVE_WHEN_FAR_AWAY);
        Boolean canPickupItems = MapUtils.getOrNull(map, KEY_CAN_PICKUP_ITEMS);
        Boolean leashed = MapUtils.getOrNull(map, KEY_LEASHED);
        EntitySpecifier<Entity> leashHolder = EntitySpecifierImpl.tryDeserialize(map.get(KEY_LEASH_HOLDER), serializer);
        Boolean gliding = MapUtils.getOrNull(map, KEY_GLIDING);
        Boolean swimming = MapUtils.getOrNull(map, KEY_SWIMMING);
        Boolean riptiding = MapUtils.getOrNull(map, KEY_RIPTIDING);
        Boolean sleeping = MapUtils.getOrNull(map, KEY_SLEEPING);
        Boolean ai = MapUtils.getOrNull(map, KEY_AI);
        Boolean collidable = MapUtils.getOrNull(map, KEY_COLLIDABLE);
        Boolean invisible = MapUtils.getOrNull(map, KEY_INVISIBLE);
        // Paper

        Integer arrowsStuck = MapUtils.getOrNull(map, KEY_ARROWS_STUCK);
        Integer shieldBlockingDelay = MapUtils.getOrNull(map, KEY_SHIELD_BLOCKING_DELAY);
        ItemStackStructure activeItem = null;
        if (map.containsKey(KEY_ACTIVE_ITEM))
            activeItem = serializer.deserialize(MapUtils.checkAndCastMap(map.get(KEY_ACTIVE_ITEM)), ItemStackStructure.class);

        Integer itemUseRemainTime = MapUtils.getOrNull(map, KEY_ITEM_USE_REMAIN_TIME);
        Integer handRaisedTime = MapUtils.getOrNull(map, KEY_HAND_RAISED_TIME);
        Boolean isHandRaised = MapUtils.getOrNull(map, KEY_IS_HAND_RAISED);
        EquipmentSlot handRaised = MapUtils.getAsEnumOrNull(map, KEY_HAND_RAISED, EquipmentSlot.class);
        Boolean jumping = MapUtils.getOrNull(map, KEY_JUMPING);
        Float hurtDirection = MapUtils.getOrNull(map, KEY_HURT_DIRECTION);

        return new LivingEntityStructureImpl(
                base,
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
                isHandRaised,
                handRaised,
                jumping,
                hurtDirection
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

    private static void validatePotionEffectMap(@NotNull List<Map<String, Object>> map)
    {
        if (map.isEmpty())
            return;

        for (Object o : map)
        {
            Map<String, Object> effectMap =
                    MapUtils.checkAndCastMap(o);
            MapUtils.checkType(effectMap, KEY_POTION_EFFECTS_TYPE, String.class);
            if (PotionEffectType.getByName((String) effectMap.get(KEY_POTION_EFFECTS_TYPE)) == null)
                throw new IllegalArgumentException("Invalid potion effect type.");

            MapUtils.checkTypeIfContains(effectMap, KEY_POTION_EFFECTS_AMBIENT, Boolean.class);
            MapUtils.checkTypeIfContains(effectMap, KEY_POTION_EFFECTS_AMPLIFIER, Integer.class);
            MapUtils.checkTypeIfContains(effectMap, KEY_POTION_EFFECTS_DURATION, Integer.class);
            MapUtils.checkTypeIfContains(effectMap, KEY_POTION_EFFECTS_SHOW_ICON, Boolean.class);
            MapUtils.checkTypeIfContains(effectMap, KEY_POTION_EFFECTS_SHOW_PARTICLES, Boolean.class);
        }
    }

    private static List<PotionEffect> deserializePotionEffects(@NotNull List<? extends Map<String, Object>> map)
    {
        List<PotionEffect> list = new ArrayList<>();
        for (Map<String, Object> effectMap : map)
        {
            PotionEffectType type =
                    PotionEffectType.getByName((String) effectMap.get(KEY_POTION_EFFECTS_TYPE));

            long duration = 0L;
            if (effectMap.containsKey(KEY_POTION_EFFECTS_DURATION))
                duration = Long.parseLong(effectMap.get(KEY_POTION_EFFECTS_DURATION).toString());
            int amplifier = MapUtils.getOrDefault(effectMap, KEY_POTION_EFFECTS_AMPLIFIER, 0);
            boolean ambient = MapUtils.getOrDefault(effectMap, KEY_POTION_EFFECTS_AMBIENT, false);
            boolean particles = MapUtils.getOrDefault(effectMap, KEY_POTION_EFFECTS_SHOW_PARTICLES, true);
            boolean icon = MapUtils.getOrDefault(effectMap, KEY_POTION_EFFECTS_SHOW_ICON, true);

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

        return Objects.equals(this.remainAir, that.remainAir)
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
                && Objects.equals(this.isHandRaised, that.isHandRaised)
                && this.handRaised == that.handRaised
                && Objects.equals(this.jumping, that.jumping)
                && Objects.equals(this.hurtDirection, that.hurtDirection);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(
                super.hashCode(), this.remainAir, this.maxAir, this.arrowCooldown, this.arrowsInBody,
                this.maxNoDamageTicks, this.lastDamage, this.noDamageTicks, this.killer, this.potionEffects,
                this.removeWhenFarAway, this.canPickupItems, this.leashed, this.leashHolder, this.gliding,
                this.swimming, this.riptiding, this.sleeping, this.ai, this.collidable, this.invisible,
                this.arrowsStuck, this.shieldBlockingDelay, this.activeItem, this.itemUseRemainTime,
                this.handRaisedTime, this.isHandRaised, this.handRaised, this.jumping, this.hurtDirection
        );
    }

    protected void applyToLivingEntity(LivingEntity entity)
    {
        super.applyToEntity(entity);

        if (this.remainAir != null)
            entity.setRemainingAir(this.remainAir);
        if (this.maxAir != null)
            entity.setMaximumAir(this.maxAir);
        if (this.arrowCooldown != null)
            entity.setArrowCooldown(this.arrowCooldown);
        if (this.arrowsInBody != null)
            entity.setArrowsInBody(this.arrowsInBody);
        if (this.maxNoDamageTicks != null)
            entity.setMaximumNoDamageTicks(this.maxNoDamageTicks);
        if (this.lastDamage != null)
            entity.setLastDamage(this.lastDamage);
        if (this.noDamageTicks != null)
            entity.setNoDamageTicks(this.noDamageTicks);
        if (this.killer.canProvideTarget())
            entity.setKiller(this.killer.selectTarget(null));
        if (!this.potionEffects.isEmpty())
        {
            new ArrayList<>(entity.getActivePotionEffects()).stream()
                    .map(PotionEffect::getType)
                    .forEach(((LivingEntity) entity)::removePotionEffect);

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
                    .forEach(((LivingEntity) entity)::addPotionEffect);
        }
        if (this.removeWhenFarAway != null)
            entity.setRemoveWhenFarAway(this.removeWhenFarAway);
        if (this.canPickupItems != null)
            entity.setCanPickupItems(this.canPickupItems);
        if (this.leashHolder.canProvideTarget())
            entity.setLeashHolder(this.leashHolder.selectTarget(null));
        if (this.gliding != null)
            entity.setGliding(this.gliding);
        if (this.swimming != null)
            entity.setSwimming(this.swimming);
        if (this.ai != null)
            entity.setAI(this.ai);
        if (this.collidable != null)
            entity.setCollidable(this.collidable);
        if (this.invisible != null)
            entity.setInvisible(this.invisible);
        // Paper

        if (this.arrowsStuck != null)
            entity.setArrowsStuck(this.arrowsStuck);
        if (this.shieldBlockingDelay != null)
            entity.setShieldBlockingDelay(this.shieldBlockingDelay);
        if (this.jumping != null)
            entity.setJumping(this.jumping);
        if (this.hurtDirection != null)
            entity.setHurtDirection(this.hurtDirection);
    }

    protected boolean isAdequateLivingEntity(LivingEntity entity, boolean strict)
    {
        if (!this.potionEffects.isEmpty())
        {
            List<PotionEffect> potionEffects = new ArrayList<>(entity.getActivePotionEffects());
            if (strict && potionEffects.size() != this.potionEffects.size())
                return false;

            for (PotionEffect effects : this.potionEffects)
                if (!potionEffects.contains(effects))
                    return false;
        }

        return super.isAdequateEntity(entity, strict)
                && (this.remainAir == null || Objects.equals(this.remainAir, entity.getRemainingAir()))
                && (this.maxAir == null || Objects.equals(this.maxAir, entity.getMaximumAir()))
                && (this.arrowCooldown == null || Objects.equals(this.arrowCooldown, entity.getArrowCooldown()))
                && (this.arrowsInBody == null || Objects.equals(this.arrowsInBody, entity.getArrowsInBody()))
                && (this.maxNoDamageTicks == null || Objects.equals(this.maxNoDamageTicks, entity.getMaximumNoDamageTicks()))
                && (this.lastDamage == null || Objects.equals(this.lastDamage, entity.getLastDamage()))
                && (this.noDamageTicks == null || Objects.equals(this.noDamageTicks, entity.getNoDamageTicks()))
                && (!this.killer.canProvideTarget() || Objects.equals(this.killer.selectTarget(null), entity.getKiller()))
                && (this.removeWhenFarAway == null || Objects.equals(this.removeWhenFarAway, entity.getRemoveWhenFarAway()))
                && (this.canPickupItems == null || Objects.equals(this.canPickupItems, entity.getCanPickupItems()))
                && (this.leashed == null || Objects.equals(this.leashed, entity.isLeashed()))
                && (!this.leashHolder.canProvideTarget() || Objects.equals(this.leashHolder.selectTarget(null), entity.getLeashHolder()))
                && (this.gliding == null || Objects.equals(this.gliding, entity.isGliding()))
                && (this.swimming == null || Objects.equals(this.swimming, entity.isSwimming()))
                && (this.ai == null || Objects.equals(this.ai, entity.hasAI()))
                && (this.collidable == null || Objects.equals(this.collidable, entity.isCollidable()))
                && (this.invisible == null || Objects.equals(this.invisible, entity.isInvisible()))
                // Paper
                && (this.arrowsStuck == null || Objects.equals(this.arrowsStuck, entity.getArrowsStuck()))
                && (this.shieldBlockingDelay == null || Objects.equals(this.shieldBlockingDelay, entity.getShieldBlockingDelay()))
                && (this.jumping == null || Objects.equals(this.jumping, entity.isJumping()))
                && (this.hurtDirection == null || Objects.equals(this.hurtDirection, entity.getHurtDirection()));
    }


}
