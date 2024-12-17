package org.kunlab.scenamatica.nms.impl.v1_14_R1.entity;

import net.minecraft.server.v1_14_R1.EntityLightning;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLightningStrike;
import org.bukkit.entity.LightningStrike;
import org.kunlab.scenamatica.nms.types.entity.NMSLightningStrike;

public class NMSLightningStrikeImpl extends NMSEntityImpl implements NMSLightningStrike
{
    private final LightningStrike bukkitLightningStrike;
    private final EntityLightning nmsLightning;

    public NMSLightningStrikeImpl(LightningStrike bukkitLightningStrike)
    {
        super(bukkitLightningStrike);
        this.bukkitLightningStrike = bukkitLightningStrike;
        this.nmsLightning = ((CraftLightningStrike) bukkitLightningStrike).getHandle();
    }

    @Override
    public void setVisualOnly(boolean visualOnly)
    {
        this.nmsLightning.isEffect = visualOnly;
    }

    @Override
    public LightningStrike getBukkit()
    {
        return this.bukkitLightningStrike;
    }
}
