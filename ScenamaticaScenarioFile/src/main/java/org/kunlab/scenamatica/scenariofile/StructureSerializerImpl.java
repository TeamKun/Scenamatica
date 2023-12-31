package org.kunlab.scenamatica.scenariofile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.context.ContextStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.context.StageStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.DamageStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.AEntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.GenericInventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.ProjectileSourceStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;
import org.kunlab.scenamatica.scenariofile.specifiers.EntitySpecifierImpl;
import org.kunlab.scenamatica.scenariofile.specifiers.PlayerSpecifierImpl;
import org.kunlab.scenamatica.scenariofile.structures.ScenarioFileStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.context.ContextStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.context.PlayerStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.context.StageStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.DamageStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.SelectiveEntityStructureSerializer;
import org.kunlab.scenamatica.scenariofile.structures.entity.entities.AEntityStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.inventory.GenericInventoryStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.inventory.InventoryStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.inventory.ItemStackStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.inventory.PlayerInventoryStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.misc.BlockStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.misc.LocationStructureImpl;
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
import java.util.function.Predicate;

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

    private static boolean isEntityRelated(@Nullable Object value, @Nullable Class<?> clazz)
    {
        return value instanceof EntityStructure || clazz != null && EntityStructure.class.isAssignableFrom(clazz);
    }

    @Override
    public @NotNull <T extends Structure> Map<String, Object> serialize(@NotNull T structure, @Nullable Class<T> clazz)
    {
        // エンティティの場合は, さらに EntityType で分岐する
        if (isEntityRelated(structure, clazz))
            // noinspection unchecked
            return SelectiveEntityStructureSerializer.serialize((EntityStructure) structure, this, (Class<? extends EntityStructure>) clazz);

        return this.selectEntry(structure, clazz).getSerializer().apply(structure, this);
    }

    @Override
    public <T extends Structure> @NotNull T deserialize(@NotNull Map<String, Object> map, @NotNull Class<T> clazz)
    {
        // エンティティの場合は, さらに EntityType で分岐する
        if (isEntityRelated(null, clazz))
            // noinspection unchecked
            return (T) SelectiveEntityStructureSerializer.deserialize(map, this, (Class<? extends EntityStructure>) clazz);

        return this.selectEntry(clazz).getDeserializer().apply(map, this);
    }

    @Override
    public <T extends Structure> void validate(@NotNull Map<String, Object> map, @NotNull Class<T> clazz)
    {
        // エンティティの場合は, さらに EntityType で分岐する
        if (isEntityRelated(null, clazz))
        {
            // noinspection unchecked
            SelectiveEntityStructureSerializer.validate(map, this, (Class<? extends EntityStructure>) clazz);
            return;
        }
        this.selectEntry(clazz).getValidator().accept(map, this);
    }

    @Override
    public <V, T extends Mapped<V> & Structure> T toStructure(@NotNull V value, @Nullable Class<T> clazz)
    {
        // エンティティの場合は, さらに EntityType で分岐する
        if (isEntityRelated(value, clazz))
            return (T) SelectiveEntityStructureSerializer.toStructure((Entity) value, this);

        return this.selectMappedEntry(value, clazz).getConstructor().apply(value);
    }

    @Override
    public <E extends Entity> @NotNull EntitySpecifier<E> tryDeserializeEntitySpecifier(@Nullable Object obj, Class<? extends EntityStructure> structureClass)
    {
        return EntitySpecifierImpl.tryDeserialize(obj, this, structureClass);
    }

    @Override
    public @NotNull PlayerSpecifier tryDeserializePlayerSpecifier(@Nullable Object obj)
    {
        return PlayerSpecifierImpl.tryDeserializePlayer(obj, this);
    }

    // <editor-fold desc="Structure 登録用のメソッド"

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

    /* ------------------------------------ */

    private <V, T extends Mapped<V> & Structure> void registerStructure(@NotNull Class<T> clazz,
                                                                        @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                                        @NotNull BiFunction<Map<String, Object>, StructureSerializer, T> deserializer,
                                                                        @NotNull BiConsumer<Map<String, Object>, StructureSerializer> validator,
                                                                        @NotNull Function<V, T> constructor,
                                                                        @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, serializer, deserializer, validator, constructor, applicator));
    }

    private <V, T extends Mapped<V> & Structure> void registerStructure(@NotNull Class<T> clazz,
                                                                        @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                                        @NotNull BiFunction<Map<String, Object>, StructureSerializer, T> deserializer,
                                                                        @NotNull Consumer<? super Map<String, Object>> validator,
                                                                        @NotNull Function<V, T> constructor,
                                                                        @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, serializer, deserializer, (v, t) -> validator.accept(v), constructor, applicator));
    }

    private <V, T extends Mapped<V> & Structure> void registerStructure(@NotNull Class<T> clazz,
                                                                        @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                                        @NotNull BiFunction<Map<String, Object>, StructureSerializer, T> deserializer,
                                                                        @NotNull BiConsumer<Map<String, Object>, StructureSerializer> validator,
                                                                        @NotNull Function<V, T> constructor,
                                                                        @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, (v, t) -> serializer.apply(v), deserializer, validator, constructor, applicator));
    }

    private <V, T extends Mapped<V> & Structure> void registerStructure(@NotNull Class<T> clazz,
                                                                        @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                                        @NotNull BiFunction<Map<String, Object>, StructureSerializer, T> deserializer,
                                                                        @NotNull Consumer<? super Map<String, Object>> validator,
                                                                        @NotNull Function<V, T> constructor,
                                                                        @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, (v, t) -> serializer.apply(v), deserializer, (v, t) -> validator.accept(v), constructor, applicator));
    }

    private <V, T extends Mapped<V> & Structure> void registerStructure(@NotNull Class<T> clazz,
                                                                        @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                                        @NotNull Function<? super Map<String, Object>, ? extends T> deserializer,
                                                                        @NotNull BiConsumer<Map<String, Object>, StructureSerializer> validator,
                                                                        @NotNull Function<V, T> constructor,
                                                                        @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, (v, t) -> serializer.apply(v), (v, t) -> deserializer.apply(v), validator, constructor, applicator));
    }

    private <V, T extends Mapped<V> & Structure> void registerStructure(@NotNull Class<T> clazz,
                                                                        @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                                        @NotNull Function<? super Map<String, Object>, ? extends T> deserializer,
                                                                        @NotNull Consumer<? super Map<String, Object>> validator,
                                                                        @NotNull Function<V, T> constructor,
                                                                        @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, (v, t) -> serializer.apply(v), (v, t) -> deserializer.apply(v), (v, t) -> validator.accept(v), constructor, applicator));
    }

    // </editor-fold>

    private <T extends Structure> StructureEntry<T> selectEntry(@NotNull T value, @Nullable Class<T> clazz)
    {
        if (clazz != null)
            return this.selectEntry(clazz);
        else
            return this.guessEntry(value);
    }

    private <T extends Structure> StructureEntry<T> selectEntry(@NotNull Class<T> clazz)
    {
        // noinspection unchecked
        return (StructureEntry<T>) this.structureEntries.stream()
                .filter(entry -> entry.getClazz().equals(clazz))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown structure class: " + clazz));
    }

    private <T extends Structure> StructureEntry<T> guessEntry(@NotNull T value)
    {
        // noinspection unchecked
        return (StructureEntry<T>) this.structureEntries.stream()
                .filter(entry -> entry.getClazz().isInstance(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown structure class: " + value.getClass()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <V, T extends Mapped<V> & Structure> MappedStructureEntry<V, T> selectMappedEntry(@NotNull V value, @Nullable Class<T> clazz)
    {
        Predicate<MappedStructureEntry> applicator;
        if (clazz == null)
            applicator = entry -> entry.getApplicator().test(value);
        else
            applicator = entry -> entry.getClazz().equals(clazz);

        //noinspection DataFlowIssue <- 誤検出
        return this.structureEntries.stream()
                .filter(entry -> entry instanceof MappedStructureEntry)
                .map(entry -> (MappedStructureEntry) entry)
                .filter(applicator)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown structure class: " + value.getClass()));
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
        this.registerHelpers();

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
                AEntityStructure.class,
                AEntityStructureImpl::serialize,
                AEntityStructureImpl::deserialize,
                AEntityStructureImpl::validate
        );
    }

    private void registerInventoryStructures()
    {
        this.registerStructure(
                GenericInventoryStructure.class,
                GenericInventoryStructureImpl::serialize,
                GenericInventoryStructureImpl::deserialize,
                GenericInventoryStructureImpl::validate
        );
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
                PlayerInventoryStructureImpl::deserializePlayerInventory,
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

        this.registerStructure(
                LocationStructure.class,
                LocationStructureImpl::serialize,
                LocationStructureImpl::deserialize,
                LocationStructureImpl::validate
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

    private void registerHelpers()
    {
        this.registerStructure(
                ProjectileSourceStructure.class,
                ProjectileSourceSerializeHelper::serialize,
                ProjectileSourceSerializeHelper::deserialize,
                ProjectileSourceSerializeHelper::validate
        );
    }

    // </editor-fold>

    @Data
    @NotNull
    @AllArgsConstructor
    public static class StructureEntry<T extends Structure>
    {
        Class<T> clazz;
        BiFunction<T, StructureSerializer, Map<String, Object>> serializer;
        BiFunction<Map<String, Object>, StructureSerializer, T> deserializer;
        BiConsumer<Map<String, Object>, StructureSerializer> validator;
    }

    @Value
    @NotNull
    @EqualsAndHashCode(callSuper = true)
    public static class MappedStructureEntry<V, T extends Mapped<V> & Structure> extends StructureEntry<T>
    {
        Function<V, T> constructor;
        Predicate<?> applicator;

        public MappedStructureEntry(Class<T> clazz, BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                    BiFunction<Map<String, Object>, StructureSerializer, T> deserializer,
                                    BiConsumer<Map<String, Object>, StructureSerializer> validator,
                                    Function<V, T> constructor, Predicate<?> applicator)
        {
            super(clazz, serializer, deserializer, validator);
            this.constructor = constructor;
            this.applicator = applicator;
        }
    }
}
