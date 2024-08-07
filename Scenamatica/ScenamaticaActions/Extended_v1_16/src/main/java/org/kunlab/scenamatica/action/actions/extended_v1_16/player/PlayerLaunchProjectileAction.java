package org.kunlab.scenamatica.action.actions.extended_v1_16.player;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Trident;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

import java.util.Collections;
import java.util.List;

@Action(value = "player_projectile_launch", supportsSince = MinecraftVersion.V1_16)
public class PlayerLaunchProjectileAction extends org.kunlab.scenamatica.action.actions.base.player.PlayerLaunchProjectileAction
        implements Executable, Expectable
{
    public static final InputToken<ProjectileType_v1_16> IN_PROJECTILE_TYPE = ofEnumInput(
            "projectileType",
            ProjectileType_v1_16.class
    );

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        ProjectileType_v1_16 type = ctxt.input(IN_PROJECTILE_TYPE);
        Vector velocity = ctxt.input(IN_VELOCITY).create().toVector();

        this.makeOutputs(ctxt, player);
        Projectile proj = player.launchProjectile(type.getClazz(), velocity);
        this.makeLazyOutputs(ctxt, player, proj);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerLaunchProjectileEvent;
        PlayerLaunchProjectileEvent e = (PlayerLaunchProjectileEvent) event;

        ProjectileType_v1_16 projectileType = ProjectileType_v1_16.fromProjectile(e.getProjectile());
        Vector velocity = e.getProjectile().getVelocity().clone();

        boolean result = ctxt.ifHasInput(IN_PROJECTILE_TYPE, type -> type == projectileType)
                && ctxt.ifHasInput(
                IN_VELOCITY,
                loc -> Utils.vectorEquals(loc.create().toVector(), velocity, ctxt.input(IN_EPSILON))
        );

        if (result)
            this.makeLazyOutputs(ctxt, e.getPlayer(), e.getProjectile());

        return result;
    }

    protected void makeLazyOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull Projectile projectile)
    {
        ctxt.output(KEY_OUT_PROJECTILE, projectile);
        super.makeOutputs(ctxt, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerLaunchProjectileEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .register(IN_PROJECTILE_TYPE);
    }

    @AllArgsConstructor
    @Getter
    public enum ProjectileType_v1_16
    {
        ARROW(Arrow.class),
        SNOWBALL(Snowball.class),
        EGG(Egg.class),
        FIREBALL(Fireball.class),
        SMALL_FIREBALL(SmallFireball.class),
        ENDER_PEARL(EnderPearl.class),
        WITHER_SKULL(WitherSkull.class),
        SHULKER_BULLET(ShulkerBullet.class),
        DRAGON_FIREBALL(DragonFireball.class),
        POTION(ThrownPotion.class),
        EXPERIENCE_BOTTLE(ThrownExpBottle.class),
        ITEM(ThrownPotion.class),
        FIREWORK(Firework.class),
        TRIDENT(Trident.class);

        private final Class<? extends Projectile> clazz;

        public static <T extends Projectile> ProjectileType_v1_16 fromProjectile(T proj)
        {
            return fromClass(proj.getClass());
        }

        public static <T extends Projectile> ProjectileType_v1_16 fromClass(Class<T> clazz)
        {
            for (ProjectileType_v1_16 type : values())
            {
                if (type.getClazz().isAssignableFrom(clazz))
                    return type;
            }
            return null;
        }
    }

}
