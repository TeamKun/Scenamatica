package org.kunlab.scenamatica.nms.types.entity;

import javax.annotation.Nullable;
import lombok.Getter;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.util.Vector;
import org.kunlab.scenamatica.nms.NMSElement;

/**
 * NMSコンテキストにおけるダメージソースを表します。
 * このインターフェースは、ダメージソースの様々なプロパティを操作およびクエリするためのメソッドを提供します。
 */
@Getter
public class NMSDamageSource implements NMSElement
{
    public static final String ARROW_TYPE = "arrow";
    public static final String CACTUS_TYPE = "cactus";
    public static final String CRAMMING_TYPE = "cramming";
    public static final String DRAGON_BREATH_TYPE = "dragonBreath";
    public static final String DROWN_TYPE = "drown";
    public static final String DRYOUT_TYPE = "dryout";
    public static final String EXPLOSION_PLAYER_TYPE = "explosion.player";
    public static final String EXPLOSION_TYPE = "explosion";
    public static final String FALL_TYPE = "fall";
    public static final String FALLING_BLOCK_TYPE = "fallingBlock";
    public static final String FALLING_STALACTITE_TYPE = "fallingStalactite";
    public static final String FIREBALL_TYPE = "fireball";
    public static final String FIREWORKS_TYPE = "fireworks";
    public static final String FLY_INTO_WALL_TYPE = "flyIntoWall";
    public static final String FREEZE_TYPE = "freeze";
    public static final String GENERIC_TYPE = "generic";
    public static final String HOT_FLOOR_TYPE = "hotFloor";
    public static final String IN_FIRE_TYPE = "inFire";
    public static final String IN_WALL_TYPE = "inWall";
    public static final String INDIRECT_MAGIC_TYPE = "indirectMagic";
    public static final String LAVA_TYPE = "lava";
    public static final String LIGHTNING_BOLT_TYPE = "lightningBolt";
    public static final String MAGIC_TYPE = "magic";
    public static final String MOB_TYPE = "mob";
    public static final String ON_FIRE_TYPE = "onFire";
    public static final String OUT_OF_WORLD_TYPE = "outOfWorld";
    public static final String PLAYER_TYPE = "player";
    public static final String STALAGMITE_TYPE = "stalagmite";
    public static final String STARVE_TYPE = "starve";
    public static final String STING_TYPE = "sting";
    public static final String SWEET_BERRY_BUSH_TYPE = "sweetBerryBush";
    public static final String THORNS_TYPE = "thorns";
    public static final String THROWN_TYPE = "thrown";
    public static final String TRIDENT_TYPE = "trident";
    public static final String WITHER_SKULL_TYPE = "witherSkull";
    public static final String WITHER_TYPE = "wither";

    public static final NMSDamageSource CACTUS = new NMSDamageSource(CACTUS_TYPE);
    public static final NMSDamageSource CRAMMING = new NMSDamageSource(CRAMMING_TYPE);
    public static final NMSDamageSource DRAGON_BREATH = new NMSDamageSource(DRAGON_BREATH_TYPE).bypassArmor().bypassMagic();
    public static final NMSDamageSource DROWN = new NMSDamageSource(DROWN_TYPE);
    public static final NMSDamageSource DRYOUT = new NMSDamageSource(DRYOUT_TYPE);
    public static final NMSDamageSource FALL = new NMSDamageSource(FALL_TYPE).isFall();
    public static final NMSDamageSource FALLING_BLOCK = new NMSDamageSource(FALLING_BLOCK_TYPE);
    public static final NMSDamageSource FALLING_STALACTITE = new NMSDamageSource(FALLING_STALACTITE_TYPE).isFall();
    public static final NMSDamageSource FLY_INTO_WALL = new NMSDamageSource(FLY_INTO_WALL_TYPE);
    public static final NMSDamageSource FREEZE = new NMSDamageSource(FREEZE_TYPE).bypassArmor().bypassMagic();
    public static final NMSDamageSource GENERIC = new NMSDamageSource(GENERIC_TYPE);
    public static final NMSDamageSource HOT_FLOOR = new NMSDamageSource(HOT_FLOOR_TYPE).isFire();
    public static final NMSDamageSource IN_FIRE = new NMSDamageSource(IN_FIRE_TYPE).isFire();
    public static final NMSDamageSource IN_WALL = new NMSDamageSource(IN_WALL_TYPE);
    public static final NMSDamageSource LAVA = new NMSDamageSource(LAVA_TYPE).isFire();
    public static final NMSDamageSource LIGHTNING_BOLT = new NMSDamageSource(LIGHTNING_BOLT_TYPE);
    public static final NMSDamageSource MAGIC = new NMSDamageSource(MAGIC_TYPE).bypassArmor().bypassMagic();
    public static final NMSDamageSource ON_FIRE = new NMSDamageSource(ON_FIRE_TYPE).isFire();
    public static final NMSDamageSource OUT_OF_WORLD = new NMSDamageSource(OUT_OF_WORLD_TYPE).bypassArmor().bypassInvul();
    public static final NMSDamageSource STALAGMITE = new NMSDamageSource(STALAGMITE_TYPE).isFall();
    public static final NMSDamageSource STARVE = new NMSDamageSource(STARVE_TYPE).bypassArmor().bypassMagic();
    public static final NMSDamageSource SWEET_BERRY_BUSH = new NMSDamageSource(SWEET_BERRY_BUSH_TYPE);
    public static final NMSDamageSource WITHER = new NMSDamageSource(WITHER_TYPE).bypassArmor().bypassMagic();

    private final String damageType;

    private boolean damageHelmet;
    private boolean bypassArmor;
    private boolean bypassInvul;
    private boolean bypassMagic;
    private float exhaustion = 0.1F;
    private boolean isFireSource;
    private boolean isProjectile;
    private boolean scalesWithDifficulty;
    private boolean isMagic;
    private boolean isExplosion;
    private boolean isFall;
    private boolean noAggro;
    private Entity directEntity;
    private Entity entity;
    private Vector sourcePosition;

    public NMSDamageSource(String damageType)
    {
        this.damageType = damageType;
    }

    public static NMSDamageSource arrow(Arrow arrow, @Nullable Entity shooter)
    {
        return new NMSDamageSource(ARROW_TYPE).entity(arrow).directEntity(shooter).projectile();
    }

    public static NMSDamageSource explosion(@Nullable LivingEntity entity)
    {
        return entity != null ? new NMSDamageSource(EXPLOSION_PLAYER_TYPE).entity(entity).scalesWithDifficulty().explosion(): new NMSDamageSource(EXPLOSION_TYPE).scalesWithDifficulty().explosion();
    }

    public static NMSDamageSource fireball(Fireball fireball, @Nullable Entity shooter)
    {
        return new NMSDamageSource(FIREBALL_TYPE).entity(fireball).directEntity(shooter).isFire().projectile();
    }

    public static NMSDamageSource fireworks(Firework firework, @Nullable Entity shooter)
    {
        return new NMSDamageSource(FIREWORKS_TYPE).entity(firework).directEntity(shooter).explosion();
    }

    public static NMSDamageSource indirectMagic(Entity magic, @Nullable Entity shooter)
    {
        return new NMSDamageSource(INDIRECT_MAGIC_TYPE).entity(magic).directEntity(shooter).bypassArmor().magic();
    }

    public static NMSDamageSource indirectMobAttack(Entity entity, @Nullable LivingEntity indirectEntity)
    {
        return new NMSDamageSource(MOB_TYPE).entity(entity).directEntity(indirectEntity);
    }

    public static NMSDamageSource mobAttack(LivingEntity entity)
    {
        return new NMSDamageSource(MOB_TYPE).entity(entity);
    }

    public static NMSDamageSource playerAttack(Player player)
    {
        return new NMSDamageSource(PLAYER_TYPE).entity(player);
    }

    public static NMSDamageSource anyEntityAttack(Entity entity)
    {
        if (entity instanceof HumanEntity)
            return playerAttack((Player) entity);
        else if (entity instanceof LivingEntity)
            return mobAttack((LivingEntity) entity);
        else
            return new NMSDamageSource(GENERIC_TYPE).entity(entity);
    }

    public static NMSDamageSource sting(LivingEntity entity)
    {
        return new NMSDamageSource(STING_TYPE).entity(entity);
    }

    public static NMSDamageSource thorns(Entity entity)
    {
        return new NMSDamageSource(THORNS_TYPE).entity(entity).magic();
    }

    public static NMSDamageSource thrown(Entity thrown, @Nullable Entity shooter)
    {
        return new NMSDamageSource(THROWN_TYPE).entity(thrown).directEntity(shooter).projectile();
    }

    public static NMSDamageSource trident(Entity trident, @Nullable Entity shooter)
    {
        return new NMSDamageSource(TRIDENT_TYPE).entity(trident).directEntity(shooter).projectile();
    }

    public static NMSDamageSource witherSkull(WitherSkull witherSkull, Entity shooter)
    {
        return new NMSDamageSource(WITHER_SKULL_TYPE).entity(witherSkull).directEntity(shooter).projectile();
    }

    public NMSDamageSource damageHelmet()
    {
        this.damageHelmet = true;
        return this;
    }

    public NMSDamageSource bypassArmor()
    {
        this.bypassArmor = true;
        this.exhaustion = 0.0F;
        return this;
    }

    public NMSDamageSource bypassInvul()
    {
        this.bypassInvul = true;
        return this;
    }

    public NMSDamageSource bypassMagic()
    {
        this.bypassMagic = true;
        this.exhaustion = 0.0F;
        return this;
    }

    public NMSDamageSource isFire()
    {
        this.isFireSource = true;
        return this;
    }

    public NMSDamageSource projectile()
    {
        this.isProjectile = true;
        return this;
    }

    public NMSDamageSource explosion()
    {
        this.isExplosion = true;
        return this;
    }

    public NMSDamageSource isFall()
    {
        this.isFall = true;
        return this;
    }

    public NMSDamageSource noAggro()
    {
        this.noAggro = true;
        return this;
    }

    public NMSDamageSource scalesWithDifficulty()
    {
        this.scalesWithDifficulty = true;
        return this;
    }

    public NMSDamageSource magic()
    {
        this.isMagic = true;
        return this;
    }

    public NMSDamageSource directEntity(Entity directEntity)
    {
        this.directEntity = directEntity;
        return this;
    }

    public NMSDamageSource entity(Entity entity)
    {
        this.entity = entity;
        return this;
    }

    public NMSDamageSource sourcePosition(Vector sourcePosition)
    {
        this.sourcePosition = sourcePosition;
        return this;
    }

    @Nullable
    public Entity getDirectEntity()
    {
        return this.directEntity;
    }

    @Nullable
    public Entity getEntity()
    {
        return this.entity;
    }

    @Nullable
    public Vector getSourcePosition()
    {
        return this.sourcePosition;
    }
}
