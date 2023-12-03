package org.kunlab.scenamatica.scenariofile.structures.entity.entities;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.MainHand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.DamageStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.AHumanEntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.HumanEntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryStructure;

import java.util.List;
import java.util.UUID;

public class AHumanEntityStructureImpl extends HumanEntityStructureImpl implements AHumanEntityStructure
{
    public AHumanEntityStructureImpl(PlayerInventoryStructure inventory, InventoryStructure enderChest, MainHand mainHand, GameMode gamemode, Integer foodLevel)
    {
        super(inventory, enderChest, mainHand, gamemode, foodLevel);
    }

    public AHumanEntityStructureImpl(Location location, Vector velocity, String customName, UUID uuid, Boolean glowing, Boolean gravity, Boolean silent, Boolean customNameVisible, Boolean invulnerable, @NotNull List<String> tags, Integer maxHealth, Integer health, DamageStructure lastDamageCause, @NotNull List<PotionEffect> potionEffects, Integer fireTicks, Integer ticksLived, Integer portalCooldown, Boolean persistent, Float fallDistance, PlayerInventoryStructure inventory, InventoryStructure enderChest, MainHand mainHand, GameMode gamemode, Integer foodLevel)
    {
        super(location, velocity, customName, uuid, glowing, gravity, silent, customNameVisible, invulnerable, tags, maxHealth, health, lastDamageCause, potionEffects, fireTicks, ticksLived, portalCooldown, persistent, fallDistance, inventory, enderChest, mainHand, gamemode, foodLevel);
    }

    public AHumanEntityStructureImpl(HumanEntityStructure original)
    {
        super(original);
    }

    public AHumanEntityStructureImpl(EntityStructure original, PlayerInventoryStructure inventory, InventoryStructure enderChest, MainHand mainHand, GameMode gamemode, Integer foodLevel)
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
