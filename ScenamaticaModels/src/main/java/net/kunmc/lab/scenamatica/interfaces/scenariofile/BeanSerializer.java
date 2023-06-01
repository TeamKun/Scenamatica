package net.kunmc.lab.scenamatica.interfaces.scenariofile;

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
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * シナリオの Bean をシリアライズ・デシリアライズおよびその Map を検証します。
 * <li>{@code serialize}~： 各 Bean を Map にシリアライズします。</li>
 * <li>{@code deserialize}~： Map を各 Bean にデシリアライズします。</li>
 * <li>{@code validate}~： Map がその Bean のパラメタを正しく持っているか検証します。不正な場合は {@link IllegalArgumentException} を投げます。</li>
 */
public interface BeanSerializer
{
    @NotNull Map<String, Object> serializeContext(@NotNull ContextBean contextBean);

    @NotNull Map<String, Object> serializePlayer(@NotNull PlayerBean playerBean);

    @NotNull Map<String, Object> serializeStage(@NotNull StageBean stageBean);

    @NotNull Map<String, Object> serializeDamage(@NotNull DamageBean damageBean);

    @NotNull Map<String, Object> serializeEntity(@NotNull EntityBean entityBean);

    @NotNull Map<String, Object> serializeHumanEntity(@NotNull HumanEntityBean humanEntityBean);

    @NotNull Map<String, Object> serializeInventory(@NotNull InventoryBean inventoryBean);

    @NotNull Map<String, Object> serializeItemStack(@NotNull ItemStackBean itemStackBean);

    @NotNull Map<String, Object> serializePlayerInventory(@NotNull PlayerInventoryBean playerInventoryBean);

    @NotNull Map<String, Object> serializeBlock(@NotNull BlockBean blockBean);

    @NotNull Map<String, Object> serializeAction(@NotNull ActionBean actionBean);

    @NotNull Map<String, Object> serializeScenario(@NotNull ScenarioBean scenarioBean);

    @NotNull Map<String, Object> serializeTrigger(@NotNull TriggerBean trigger);

    @NotNull Map<String, Object> serializeScenarioFile(@NotNull ScenarioFileBean scenarioFileBean);

    // Validate
    void validateContext(@NotNull Map<String, Object> context);

    void validatePlayer(@NotNull Map<String, Object> player);

    void validateStage(@NotNull Map<String, Object> stage);

    void validateDamage(@NotNull Map<String, Object> damage);

    void validateEntity(@NotNull Map<String, Object> entity);

    void validateHumanEntity(@NotNull Map<String, Object> humanEntity);

    void validateInventory(@NotNull Map<String, Object> inventory);

    void validateItemStack(@NotNull Map<String, Object> itemStack);

    void validatePlayerInventory(@NotNull Map<String, Object> playerInventory);

    void validateBlock(@NotNull Map<String, Object> block);

    void validateAction(@NotNull Map<String, Object> action);

    void validateScenario(@NotNull Map<String, Object> scenario);

    void validateTrigger(@NotNull Map<String, Object> trigger);

    void validateScenarioFile(@NotNull Map<String, Object> scenarioFile);

    @NotNull ContextBean deserializeContext(@NotNull Map<String, Object> context);

    @NotNull PlayerBean deserializePlayer(@NotNull Map<String, Object> player);

    @NotNull StageBean deserializeStage(@NotNull Map<String, Object> stage);

    @NotNull DamageBean deserializeDamage(@NotNull Map<String, Object> damage);

    @NotNull EntityBean deserializeEntity(@NotNull Map<String, Object> entity);

    @NotNull HumanEntityBean deserializeHumanEntity(@NotNull Map<String, Object> humanEntity);

    @NotNull InventoryBean deserializeInventory(@NotNull Map<String, Object> inventory);

    @NotNull ItemStackBean deserializeItemStack(@NotNull Map<String, Object> itemStack);

    @NotNull PlayerInventoryBean deserializePlayerInventory(@NotNull Map<String, Object> playerInventory);

    @NotNull BlockBean deserializeBlock(@NotNull Map<String, Object> block);

    @NotNull ActionBean deserializeAction(@NotNull Map<String, Object> action);

    @NotNull ScenarioBean deserializeScenario(@NotNull Map<String, Object> scenario);

    @NotNull TriggerBean deserializeTrigger(@NotNull Map<String, Object> trigger);

    @NotNull ScenarioFileBean deserializeScenarioFile(@NotNull Map<String, Object> scenarioFile);
}
