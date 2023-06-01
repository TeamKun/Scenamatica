package net.kunmc.lab.scenamatica.scenariofile;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.BeanSerializer;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.ContextBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.StageBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.entities.DamageBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.entities.EntityBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.entities.HumanEntityBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.ScenarioFileBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.context.ContextBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.context.PlayerBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.context.StageBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.entities.DamageBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.entities.EntityBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.entities.HumanEntityBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.inventory.InventoryBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.inventory.ItemStackBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.inventory.PlayerInventoryBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.misc.BlockBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ActionBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ScenarioBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.trigger.TriggerBeanImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BeanSerializerImpl implements BeanSerializer
{
    private static final BeanSerializer INSTANCE;

    static
    {
        INSTANCE = new BeanSerializerImpl();  // シングルトン
    }

    private BeanSerializerImpl()
    {
    }

    @NotNull
    public static BeanSerializer getInstance()
    {
        return BeanSerializerImpl.INSTANCE;
    }

    // Serialize

    @Override
    @NotNull
    public Map<String, Object> serializeContext(@NotNull ContextBean contextBean)
    {
        return ContextBeanImpl.serialize(contextBean, this);
    }

    @Override
    @NotNull
    public Map<String, Object> serializePlayer(@NotNull PlayerBean playerBean)
    {
        return PlayerBeanImpl.serialize(playerBean, this);
    }

    @Override
    @NotNull
    public Map<String, Object> serializeStage(@NotNull StageBean stageBean)
    {
        return StageBeanImpl.serialize(stageBean);
    }

    @Override
    @NotNull
    public Map<String, Object> serializeDamage(@NotNull DamageBean damageBean)
    {
        return DamageBeanImpl.serialize(damageBean);
    }

    @Override
    @NotNull
    public Map<String, Object> serializeEntity(@NotNull EntityBean entityBean)
    {
        return EntityBeanImpl.serialize(entityBean, this);
    }

    @Override
    @NotNull
    public Map<String, Object> serializeHumanEntity(@NotNull HumanEntityBean humanEntityBean)
    {
        return HumanEntityBeanImpl.serialize(humanEntityBean, this);
    }

    @Override
    @NotNull
    public Map<String, Object> serializeInventory(@NotNull InventoryBean inventoryBean)
    {
        return InventoryBeanImpl.serialize(inventoryBean, this);
    }

    @Override
    @NotNull
    public Map<String, Object> serializeItemStack(@NotNull ItemStackBean itemStackBean)
    {
        return ItemStackBeanImpl.serialize(itemStackBean, this);
    }

    @Override
    @NotNull
    public Map<String, Object> serializePlayerInventory(@NotNull PlayerInventoryBean playerInventoryBean)
    {
        return PlayerInventoryBeanImpl.serialize(playerInventoryBean, this);
    }

    @Override
    @NotNull
    public Map<String, Object> serializeBlock(@NotNull BlockBean blockBean)
    {
        return BlockBeanImpl.serialize(blockBean);
    }

    @Override
    @NotNull
    public Map<String, Object> serializeAction(@NotNull ActionBean actionBean)
    {
        return ActionBeanImpl.serialize(actionBean);
    }

    @Override
    @NotNull
    public Map<String, Object> serializeScenario(@NotNull ScenarioBean scenarioBean)
    {
        return ScenarioBeanImpl.serialize(scenarioBean, this);
    }

    @Override
    @NotNull
    public Map<String, Object> serializeTrigger(@NotNull TriggerBean trigger)
    {
        return TriggerBeanImpl.serialize(trigger, this);
    }

    @Override
    @NotNull
    public Map<String, Object> serializeScenarioFile(@NotNull ScenarioFileBean scenarioFileBean)
    {
        return ScenarioFileBeanImpl.serialize(scenarioFileBean, this);
    }

    // Validate
    @Override
    public void validateContext(@NotNull Map<String, Object> context)
    {
        ContextBeanImpl.validate(context, this);
    }

    @Override
    public void validatePlayer(@NotNull Map<String, Object> player)
    {
        PlayerBeanImpl.validate(player, this);
    }

    @Override
    public void validateStage(@NotNull Map<String, Object> stage)
    {
        StageBeanImpl.validate(stage);
    }

    @Override
    public void validateDamage(@NotNull Map<String, Object> damage)
    {
        DamageBeanImpl.validate(damage);
    }

    @Override
    public void validateEntity(@NotNull Map<String, Object> entity)
    {
        EntityBeanImpl.validate(entity);
    }

    @Override
    public void validateHumanEntity(@NotNull Map<String, Object> humanEntity)
    {
        HumanEntityBeanImpl.validate(humanEntity);
    }

    @Override
    public void validateInventory(@NotNull Map<String, Object> inventory)
    {
        InventoryBeanImpl.validate(inventory, this);
    }

    @Override
    public void validateItemStack(@NotNull Map<String, Object> itemStack)
    {
        ItemStackBeanImpl.validate(itemStack);
    }

    @Override
    public void validatePlayerInventory(@NotNull Map<String, Object> playerInventory)
    {
        PlayerInventoryBeanImpl.validate(playerInventory, this);
    }

    @Override
    public void validateBlock(@NotNull Map<String, Object> block)
    {
        BlockBeanImpl.validate(block);
    }

    @Override
    public void validateAction(@NotNull Map<String, Object> action)
    {
        ActionBeanImpl.validate(action);
    }

    @Override
    public void validateScenario(@NotNull Map<String, Object> scenario)
    {
        ScenarioBeanImpl.validate(scenario, this);
    }

    @Override
    public void validateTrigger(@NotNull Map<String, Object> trigger)
    {
        TriggerBeanImpl.validate(trigger, this);
    }

    @Override
    public void validateScenarioFile(@NotNull Map<String, Object> scenarioFile)
    {
        ScenarioFileBeanImpl.validate(scenarioFile, this);
    }

    // Deserialize

    @Override
    @NotNull
    public ContextBean deserializeContext(@NotNull Map<String, Object> context)
    {
        return ContextBeanImpl.deserialize(context, this);
    }

    @Override
    @NotNull
    public PlayerBean deserializePlayer(@NotNull Map<String, Object> player)
    {
        return PlayerBeanImpl.deserialize(player, this);
    }

    @Override
    @NotNull
    public StageBean deserializeStage(@NotNull Map<String, Object> stage)
    {
        return StageBeanImpl.deserialize(stage);
    }

    @Override
    @NotNull
    public DamageBean deserializeDamage(@NotNull Map<String, Object> damage)
    {
        return DamageBeanImpl.deserialize(damage);
    }

    @Override
    @NotNull
    public EntityBean deserializeEntity(@NotNull Map<String, Object> entity)
    {
        return EntityBeanImpl.deserialize(entity, this);
    }

    @Override
    @NotNull
    public HumanEntityBean deserializeHumanEntity(@NotNull Map<String, Object> humanEntity)
    {
        return HumanEntityBeanImpl.deserialize(humanEntity, this);
    }

    @Override
    @NotNull
    public InventoryBean deserializeInventory(@NotNull Map<String, Object> inventory)
    {
        return InventoryBeanImpl.deserialize(inventory, this);
    }

    @Override
    @NotNull
    public ItemStackBean deserializeItemStack(@NotNull Map<String, Object> itemStack)
    {
        return ItemStackBeanImpl.deserialize(itemStack);
    }

    @Override
    @NotNull
    public PlayerInventoryBean deserializePlayerInventory(@NotNull Map<String, Object> playerInventory)
    {
        return PlayerInventoryBeanImpl.deserialize(playerInventory, this);
    }

    @Override
    @NotNull
    public BlockBean deserializeBlock(@NotNull Map<String, Object> block)
    {
        return BlockBeanImpl.deserialize(block);
    }

    @Override
    @NotNull
    public ActionBean deserializeAction(@NotNull Map<String, Object> action)
    {
        return ActionBeanImpl.deserialize(action);
    }

    @Override
    @NotNull
    public ScenarioBean deserializeScenario(@NotNull Map<String, Object> scenario)
    {
        return ScenarioBeanImpl.deserialize(scenario, this);
    }

    @Override
    @NotNull
    public TriggerBean deserializeTrigger(@NotNull Map<String, Object> trigger)
    {
        return TriggerBeanImpl.deserialize(trigger, this);
    }

    @Override
    @NotNull
    public ScenarioFileBean deserializeScenarioFile(@NotNull Map<String, Object> scenarioFile)
    {
        return ScenarioFileBeanImpl.deserialize(scenarioFile, this);
    }
}
