package org.kunlab.scenamatica.scenariofile;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.Bean;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.kunlab.scenamatica.interfaces.scenariofile.context.ContextBean;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.kunlab.scenamatica.interfaces.scenariofile.context.StageBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.DamageBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.HumanEntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.kunlab.scenamatica.scenariofile.beans.ScenarioFileBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.context.ContextBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.context.PlayerBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.context.StageBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.entity.DamageBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.entity.EntityBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.entity.entities.EntityItemBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.entity.entities.HumanEntityBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.entity.entities.ProjectileBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.inventory.InventoryBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.inventory.ItemStackBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.inventory.PlayerInventoryBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.misc.BlockBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.scenario.ActionBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.scenario.ScenarioBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.trigger.TriggerBeanImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class BeanSerializerImpl implements BeanSerializer
{
    private static final BeanSerializer INSTANCE;

    static
    {
        INSTANCE = new BeanSerializerImpl();  // シングルトン
    }

    private final List<BeanEntry<?>> beanEntries;

    private BeanSerializerImpl()
    {
        this.beanEntries = new ArrayList<>();

        this.registerBeans();
    }

    @NotNull
    public static BeanSerializer getInstance()
    {
        return BeanSerializerImpl.INSTANCE;
    }

    @Override
    public @NotNull <T extends Bean> Map<String, Object> serialize(@NotNull T bean, @NotNull Class<T> clazz)
    {
        return this.selectEntry(clazz).getSerializer().apply(bean, this);
    }

    @Override
    public <T extends Bean> @NotNull T deserialize(@NotNull Map<String, Object> map, @NotNull Class<T> clazz)
    {
        return this.selectEntry(clazz).getDeserializer().apply(map, this);
    }

    @Override
    public <T extends Bean> void validate(@NotNull Map<String, Object> map, @NotNull Class<T> clazz)
    {
        this.selectEntry(clazz).getValidator().accept(map, this);
    }

    // <editor-fold desc="Bean 登録用のメソッド">

    private <T extends Bean> void registerBean(@NotNull Class<T> clazz,
                                               @NotNull BiFunction<T, BeanSerializer, Map<String, Object>> serializer,
                                               @NotNull BiFunction<Map<String, Object>, BeanSerializer, T> deserializer,
                                               @NotNull BiConsumer<Map<String, Object>, BeanSerializer> validator)
    {
        this.beanEntries.add(new BeanEntry<>(clazz, serializer, deserializer, validator));
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends Bean> void registerBean(@NotNull Class<T> clazz,
                                               @NotNull BiFunction<T, BeanSerializer, Map<String, Object>> serializer,
                                               @NotNull BiFunction<Map<String, Object>, BeanSerializer, T> deserializer,
                                               @NotNull Consumer<? super Map<String, Object>> validator)
    {
        this.beanEntries.add(new BeanEntry<>(clazz, serializer, deserializer, (v, t) -> validator.accept(v)));
    }

    private <T extends Bean> void registerBean(@NotNull Class<T> clazz,
                                               @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                               @NotNull BiFunction<Map<String, Object>, BeanSerializer, T> deserializer,
                                               @NotNull BiConsumer<Map<String, Object>, BeanSerializer> validator)
    {
        this.beanEntries.add(new BeanEntry<>(clazz, (v, t) -> serializer.apply(v), deserializer, validator));
    }

    private <T extends Bean> void registerBean(@NotNull Class<T> clazz,
                                               @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                               @NotNull BiFunction<Map<String, Object>, BeanSerializer, T> deserializer,
                                               @NotNull Consumer<? super Map<String, Object>> validator)
    {
        this.beanEntries.add(new BeanEntry<>(clazz, (v, t) -> serializer.apply(v), deserializer, (v, t) -> validator.accept(v)));
    }

    private <T extends Bean> void registerBean(@NotNull Class<T> clazz,
                                               @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                               @NotNull Function<? super Map<String, Object>, ? extends T> deserializer,
                                               @NotNull BiConsumer<Map<String, Object>, BeanSerializer> validator)
    {
        this.beanEntries.add(new BeanEntry<>(clazz, (v, t) -> serializer.apply(v), (v, t) -> deserializer.apply(v), validator));
    }

    private <T extends Bean> void registerBean(@NotNull Class<T> clazz,
                                               @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                               @NotNull Function<? super Map<String, Object>, ? extends T> deserializer,
                                               @NotNull Consumer<? super Map<String, Object>> validator)
    {
        this.beanEntries.add(new BeanEntry<>(clazz, (v, t) -> serializer.apply(v), (v, t) -> deserializer.apply(v), (v, t) -> validator.accept(v)));
    }

    @SuppressWarnings("SameParameterValue")

    private <T extends Bean> void registerBean(@NotNull Class<T> clazz,
                                               @NotNull BiFunction<T, BeanSerializer, Map<String, Object>> serializer,
                                               @NotNull Function<? super Map<String, Object>, ? extends T> deserializer,
                                               @NotNull Consumer<? super Map<String, Object>> validator)
    {
        this.beanEntries.add(new BeanEntry<>(clazz, serializer, (v, t) -> deserializer.apply(v), (v, t) -> validator.accept(v)));
    }

    // </editor-fold>

    private <T extends Bean> BeanEntry<T> selectEntry(@NotNull Class<T> clazz)
    {
        // noinspection unchecked
        return (BeanEntry<T>) this.beanEntries.stream().parallel()
                .filter(entry -> entry.getClazz().equals(clazz))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown bean class: " + clazz.getName()));
    }

    // <editor-fold desc="すべての Bean を登録するメソッド">

    private void registerBeans()
    {
        this.registerContextBeans();
        this.registerEntityBeans();
        this.registerInventoryBeans();
        this.registerMiscBeans();
        this.registerScenarioBeans();
        this.registerTriggerBeans();

        this.registerBean(
                ScenarioFileBean.class,
                ScenarioFileBeanImpl::serialize,
                ScenarioFileBeanImpl::deserialize,
                ScenarioFileBeanImpl::validate
        );

    }

    private void registerContextBeans()
    {
        this.registerBean(
                ContextBean.class,
                ContextBeanImpl::serialize,
                ContextBeanImpl::deserialize,
                ContextBeanImpl::validate
        );
        this.registerBean(
                PlayerBean.class,
                PlayerBeanImpl::serialize,
                PlayerBeanImpl::deserialize,
                (BiConsumer<Map<String, Object>, BeanSerializer>) PlayerBeanImpl::validate
        );
        this.registerBean(
                StageBean.class,
                StageBeanImpl::serialize,
                StageBeanImpl::deserialize,
                StageBeanImpl::validate
        );
    }

    private void registerEntityBeans()
    {
        this.registerEntityEntitiesBeans();

        this.registerBean(
                DamageBean.class,
                DamageBeanImpl::serialize,
                DamageBeanImpl::deserialize,
                DamageBeanImpl::validate
        );
        this.registerBean(
                EntityBean.class,
                EntityBeanImpl::serialize,
                EntityBeanImpl::deserialize,
                EntityBeanImpl::validate
        );
    }

    private void registerEntityEntitiesBeans()
    {
        this.registerBean(
                EntityItemBean.class,
                EntityItemBeanImpl::serialize,
                EntityItemBeanImpl::deserialize,
                (BiConsumer<Map<String, Object>, BeanSerializer>) EntityItemBeanImpl::validate
        );
        this.registerBean(
                HumanEntityBean.class,
                HumanEntityBeanImpl::serialize,
                HumanEntityBeanImpl::deserialize,
                (BiConsumer<Map<String, Object>, BeanSerializer>) HumanEntityBeanImpl::validate
        );

        this.registerBean(
                ProjectileBean.class,
                ProjectileBeanImpl::serialize,
                ProjectileBeanImpl::deserialize,
                (BiConsumer<Map<String, Object>, BeanSerializer>) ProjectileBeanImpl::validate
        );
    }

    private void registerInventoryBeans()
    {
        this.registerBean(
                InventoryBean.class,
                InventoryBeanImpl::serialize,
                InventoryBeanImpl::deserialize,
                InventoryBeanImpl::validate
        );
        this.registerBean(
                ItemStackBean.class,
                ItemStackBeanImpl::serialize,
                ItemStackBeanImpl::deserialize,
                ItemStackBeanImpl::validate
        );
        this.registerBean(
                PlayerInventoryBean.class,
                PlayerInventoryBeanImpl::serialize,
                PlayerInventoryBeanImpl::deserialize,
                PlayerInventoryBeanImpl::validate
        );
    }

    private void registerMiscBeans()
    {
        this.registerBean(
                BlockBean.class,
                BlockBeanImpl::serialize,
                BlockBeanImpl::deserialize,
                BlockBeanImpl::validate
        );
    }

    private void registerScenarioBeans()
    {
        this.registerBean(
                ActionBean.class,
                ActionBeanImpl::serialize,
                ActionBeanImpl::deserialize,
                ActionBeanImpl::validate
        );
        this.registerBean(
                ScenarioBean.class,
                ScenarioBeanImpl::serialize,
                ScenarioBeanImpl::deserialize,
                ScenarioBeanImpl::validate
        );
    }

    private void registerTriggerBeans()
    {
        this.registerBean(
                TriggerBean.class,
                TriggerBeanImpl::serialize,
                TriggerBeanImpl::deserialize,
                TriggerBeanImpl::validate
        );
    }

    // </editor-fold>

    @Value
    @NotNull
    private static class BeanEntry<T extends Bean>
    {
        Class<T> clazz;
        BiFunction<T, BeanSerializer, Map<String, Object>> serializer;
        BiFunction<Map<String, Object>, BeanSerializer, T> deserializer;
        BiConsumer<Map<String, Object>, BeanSerializer> validator;
    }
}
