package net.kunmc.lab.scenamatica.context.actor.nms.v_1_16_R3;

import com.mojang.authlib.GameProfile;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumMoveType;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.WorldServer;

class MockedPlayer extends EntityPlayer
{
    public MockedPlayer(MinecraftServer minecraftserver,
                        WorldServer worldserver,
                        GameProfile gameprofile)
    {
        super(minecraftserver, worldserver, gameprofile, new MocketPlayerInteractManager(worldserver));
        this.setNoGravity(false);
        this.G = 0.5f; // ブロックのぼれるたかさ
    }

    @Override
    public boolean doAITick()
    {
        return true;  // ノックバック用
    }

    @Override
    public void playerTick()
    {
        super.entityBaseTick();
        super.playerTick();
        this.noclip = this.isSpectator();

    }

    @Override
    public void tick()
    {
        super.tick();
        this.processGravity();
    }

    private void processGravity()
    {
        Vec3D vec = getMot();

        if (!this.onGround)
        {
            if (this.inWater)
                vec = vec.add(0, -0.0252f, 0);
            else
                vec = vec.add(0, -0.4, 0);
        }

        this.move(EnumMoveType.SELF, vec);
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float f)
    {
        Entity damager = damageSource.getEntity();

        boolean damaged = super.damageEntity(damageSource, f);
        if (damaged && damager != null)
            processKnockBack(damager);

        return damaged;
    }

    private void processKnockBack(Entity damager)
    {
        float knockbackDepth = 1.2F;

        Runner.run(() -> this.setMot(new Vec3D(
                        -Math.sin(damager.yaw * Math.PI / 180.0F) * knockbackDepth * 0.5F,
                        0.8F,
                        Math.cos(damager.yaw * Math.PI / 180.0F) * knockbackDepth * 0.5F
                )
        ));
    }

    @Override
    public void die(DamageSource damagesource)
    {
        if (this.dead)
            return;

        super.die(damagesource);
        Runner.runLater(() -> getWorldServer().removeEntity(this), 15L);
        // 15L 遅らせるのは, アニメーションのため
    }
}
