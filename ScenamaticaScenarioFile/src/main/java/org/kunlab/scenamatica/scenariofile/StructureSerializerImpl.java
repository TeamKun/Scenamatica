package org.kunlab.scenamatica.scenariofile;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.context.ContextStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.context.StageStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.DamageStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;
import org.kunlab.scenamatica.scenariofile.structures.ScenarioFileStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.context.ContextStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.context.PlayerStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.context.StageStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.DamageStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.EntityStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.SelectingEntityStructureSerializer;
import org.kunlab.scenamatica.scenariofile.structures.inventory.InventoryStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.inventory.ItemStackStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.inventory.PlayerInventoryStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.misc.BlockStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.scenario.ActionStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.scenario.ScenarioStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.trigger.TriggerStructureImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class StructureSerializerImpl implements StructureSerializer
{
    private static final StructureSerializer INSTANCE;

    static
    {
        INSTANCE = new StructureSerializerImpl();  // シングルトン
    }

    private final List<StructureEntry<?>> structureEntries;

    private StructureSerializerImpl()
    {
        this.structureEntries = new ArrayList<>();

        this.registerStructures();
    }

    @NotNull
    public static StructureSerializer getInstance()
    {
        return StructureSerializerImpl.INSTANCE;
    }

    @Override
    public @NotNull <T extends Structure> Map<String, Object> serialize(@NotNull T structure, @NotNull Class<T> clazz)
    {
        // エンティティの場合は, さらに EntityType で分岐する
        if (!clazz.getName().equals(EntityStructure.class.getName()) && EntityStructure.class.isAssignableFrom(clazz))
            return SelectingEntityStructureSerializer.serialize((EntityStructure<?>) structure, this);

        return this.selectEntry(clazz).getSerializer().apply(structure, this);
    }

    @Override
    public <T extends Structure> @NotNull T deserialize(@NotNull Map<String, Object> map, @NotNull Class<T> clazz)
    {
        // エンティティの場合は, さらに EntityType で分岐する
        if (!clazz.getName().equals(EntityStructure.class.getName()) && EntityStructure.class.isAssignableFrom(clazz))
            // noinspection unchecked
            return (T) SelectingEntityStructureSerializer.deserialize((Class<? extends EntityStructure<?>>) clazz, map, this);

        return this.selectEntry(clazz).getDeserializer().apply(map, this);
    }

    @Override
    public <T extends Structure> void validate(@NotNull Map<String, Object> map, @NotNull Class<T> clazz)
    {
        // エンティティの場合は, さらに EntityType で分岐する
        if (!clazz.getName().equals(EntityStructure.class.getName()) && EntityStructure.class.isAssignableFrom(clazz))
        {
            SelectingEntityStructureSerializer.validate(map, this);
            return;
        }
        this.selectEntry(clazz).getValidator().accept(map, this);
    }

    // <editor-fold desc="Structure 登録用のメソッド">

    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                         @NotNull BiFunction<Map<String, Object>, StructureSerializer, T> deserializer,
                                                         @NotNull BiConsumer<Map<String, Object>, StructureSerializer> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, serializer, deserializer, validator));
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                         @NotNull BiFunction<Map<String, Object>, StructureSerializer, T> deserializer,
                                                         @NotNull Consumer<? super Map<String, Object>> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, serializer, deserializer, (v, t) -> validator.accept(v)));
    }

    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                         @NotNull BiFunction<Map<String, Object>, StructureSerializer, T> deserializer,
                                                         @NotNull BiConsumer<Map<String, Object>, StructureSerializer> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, (v, t) -> serializer.apply(v), deserializer, validator));
    }

    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                         @NotNull BiFunction<Map<String, Object>, StructureSerializer, T> deserializer,
                                                         @NotNull Consumer<? super Map<String, Object>> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, (v, t) -> serializer.apply(v), deserializer, (v, t) -> validator.accept(v)));
    }

    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                         @NotNull Function<? super Map<String, Object>, ? extends T> deserializer,
                                                         @NotNull BiConsumer<Map<String, Object>, StructureSerializer> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, (v, t) -> serializer.apply(v), (v, t) -> deserializer.apply(v), validator));
    }

    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                         @NotNull Function<? super Map<String, Object>, ? extends T> deserializer,
                                                         @NotNull Consumer<? super Map<String, Object>> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, (v, t) -> serializer.apply(v), (v, t) -> deserializer.apply(v), (v, t) -> validator.accept(v)));
    }

    @SuppressWarnings("SameParameterValue")

    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                         @NotNull Function<? super Map<String, Object>, ? extends T> deserializer,
                                                         @NotNull Consumer<? super Map<String, Object>> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, serializer, (v, t) -> deserializer.apply(v), (v, t) -> validator.accept(v)));
    }

    // </editor-fold>

    private <T extends Structure> StructureEntry<T> selectEntry(@NotNull Class<T> clazz)
    {
        // noinspection unchecked
        return (StructureEntry<T>) this.structureEntries.stream().parallel()
                .filter(entry -> entry.getClazz().equals(clazz))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown structure class: " + clazz));
    }

    // <editor-fold desc="すべての Structure を登録するメソッド">

    private void registerStructures()
    {
        this.registerContextStructures();
        this.registerEntityStructures();
        this.registerInventoryStructures();
        this.registerMiscStructures();
        this.registerScenarioStructures();
        this.registerTriggerStructures();

        this.registerStructure(
                ScenarioFileStructure.class,
                ScenarioFileStructureImpl::serialize,
                ScenarioFileStructureImpl::deserialize,
                ScenarioFileStructureImpl::validate
        );

    }

    private void registerContextStructures()
    {
        this.registerStructure(
                ContextStructure.class,
                ContextStructureImpl::serialize,
                ContextStructureImpl::deserialize,
                ContextStructureImpl::validate
        );
        this.registerStructure(
                PlayerStructure.class,
                PlayerStructureImpl::serialize,
                PlayerStructureImpl::deserialize,
                (BiConsumer<Map<String, Object>, StructureSerializer>) PlayerStructureImpl::validate
        );
        this.registerStructure(
                StageStructure.class,
                StageStructureImpl::serialize,
                StageStructureImpl::deserialize,
                StageStructureImpl::validate
        );
    }

    private void registerEntityStructures()
    {
        this.registerStructure(
                DamageStructure.class,
                DamageStructureImpl::serialize,
                DamageStructureImpl::deserialize,
                DamageStructureImpl::validate
        );
        this.registerStructure(
                EntityStructure.class,
                EntityStructureImpl::serialize,
                EntityStructureImpl::deserialize,
                EntityStructureImpl::validate
        );
    }

    private void registerInventoryStructures()
    {
        this.registerStructure(
                InventoryStructure.class,
                InventoryStructureImpl::serialize,
                InventoryStructureImpl::deserialize,
                InventoryStructureImpl::validate
        );
        this.registerStructure(
                ItemStackStructure.class,
                ItemStackStructureImpl::serialize,
                ItemStackStructureImpl::deserialize,
                ItemStackStructureImpl::validate
        );
        this.registerStructure(
                PlayerInventoryStructure.class,
                PlayerInventoryStructureImpl::serializePlayerInventory,
                PlayerInventoryStructureImpl::deserialize,
                PlayerInventoryStructureImpl::validate
        );
    }

    private void registerMiscStructures()
    {
        this.registerStructure(
                BlockStructure.class,
                BlockStructureImpl::serialize,
                BlockStructureImpl::deserialize,
                BlockStructureImpl::validate
        );
    }

    private void registerScenarioStructures()
    {
        this.registerStructure(
                ActionStructure.class,
                ActionStructureImpl::serialize,
                ActionStructureImpl::deserialize,
                ActionStructureImpl::validate
        );
        this.registerStructure(
                ScenarioStructure.class,
                ScenarioStructureImpl::serialize,
                ScenarioStructureImpl::deserialize,
                ScenarioStructureImpl::validate
        );
    }

    private void registerTriggerStructures()
    {
        this.registerStructure(
                TriggerStructure.class,
                TriggerStructureImpl::serialize,
                TriggerStructureImpl::deserialize,
                TriggerStructureImpl::validate
        );
    }

    // </editor-fold>

    @Value
    @NotNull
    public static class StructureEntry<T extends Structure>
    {
        Class<T> clazz;
        BiFunction<T, StructureSerializer, Map<String, Object>> serializer;
        BiFunction<Map<String, Object>, StructureSerializer, T> deserializer;
        BiConsumer<Map<String, Object>, StructureSerializer> validator;
    }
}
