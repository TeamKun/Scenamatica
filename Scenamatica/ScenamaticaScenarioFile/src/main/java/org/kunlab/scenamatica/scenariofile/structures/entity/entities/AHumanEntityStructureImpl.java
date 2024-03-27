package org.kunlab.scenamatica.scenariofile.structures.entity.entities;

import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.MainHand;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.LivingEntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.AHumanEntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.HumanEntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryStructure;

public class AHumanEntityStructureImpl extends HumanEntityStructureImpl implements AHumanEntityStructure
{
    public AHumanEntityStructureImpl(PlayerInventoryStructure inventory, InventoryStructure enderChest, MainHand mainHand, GameMode gamemode, Integer foodLevel)
    {
        super(inventory, enderChest, mainHand, gamemode, foodLevel);
    }

    public AHumanEntityStructureImpl(HumanEntityStructure original)
    {
        super(original);
    }

    public AHumanEntityStructureImpl(LivingEntityStructure original, PlayerInventoryStructure inventory, InventoryStructure enderChest, MainHand mainHand, GameMode gamemode, Integer foodLevel)
    {
        super(original, inventory, enderChest, mainHand, gamemode, foodLevel);
    }

    public AHumanEntityStructureImpl()
    {
    }

    @Override
    public void applyTo(HumanEntity object)
    {
        super.applyToHumanEntity(object);
    }

    @Override
    public boolean canApplyTo(Object target)
    {
        return target instanceof HumanEntity;
    }

    @Override
    public boolean isAdequate(HumanEntity object, boolean strict)
    {
        return super.isAdequateHumanEntity(object, strict);
    }
}
