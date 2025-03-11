package org.kunlab.scenamatica.nms.impl.v1_18_R1.entity;

import net.minecraft.world.entity.LightningBolt;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftLightningStrike;
import org.bukkit.entity.LightningStrike;
import org.kunlab.scenamatica.nms.types.entity.NMSLightningStrike;

public class NMSLightningStrikeImpl extends NMSEntityImpl implements NMSLightningStrike
{
    private final LightningStrike bukkitLightningStrike;
    private final LightningBolt nmsLightning;

    public NMSLightningStrikeImpl(LightningStrike bukkitLightningStrike)
    {
        super(bukkitLightningStrike);
        this.bukkitLightningStrike = bukkitLightningStrike;
        this.nmsLightning = ((CraftLightningStrike) bukkitLightningStrike).getHandle();
    }

    @Override
    public void setVisualOnly(boolean visualOnly)
    {
        this.nmsLightning.visualOnly = visualOnly;
    }

    @Override
    public LightningStrike getBukkit()
    {
        return this.bukkitLightningStrike;
    }
}
