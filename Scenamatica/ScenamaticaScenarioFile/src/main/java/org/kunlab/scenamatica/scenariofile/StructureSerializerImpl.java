package org.kunlab.scenamatica.scenariofile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.scenariofile.VersionRange;
import org.kunlab.scenamatica.interfaces.structures.context.ContextStructure;
import org.kunlab.scenamatica.interfaces.structures.context.StageStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.DamageStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.PlayerInventoryStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.ProjectileSourceStructure;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;
import org.kunlab.scenamatica.interfaces.structures.scenario.ScenarioStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;
import org.kunlab.scenamatica.scenariofile.structures.ScenarioFileStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.VersionRangeImpl;
import org.kunlab.scenamatica.scenariofile.structures.context.ContextStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.context.StageStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.scenario.ActionStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.scenario.ScenarioStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.trigger.TriggerStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.entity.DamageStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.entity.EntityStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.inventory.InventoryStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.inventory.ItemStackStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.inventory.PlayerInventoryStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.misc.BlockStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.misc.LocationStructureImpl;
import org.kunlab.scenamatica.structures.specifiers.EntitySpecifierImpl;
import org.kunlab.scenamatica.structures.specifiers.PlayerSpecifierImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
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

    private static boolean isEntityRelatedStructure(@Nullable Object value, @Nullable Class<?> clazz)
    {
        return value instanceof EntityStructure || clazz != null && EntityStructure.class.isAssignableFrom(clazz);
    }

    private static boolean isEntityRelatedValue(@Nullable Object value, @Nullable Class<?> clazz)
    {
        return value instanceof Entity || clazz != null && Entity.class.isAssignableFrom(clazz)
                || isEntityRelatedStructure(value, clazz);
    }

    private static boolean canExtend(@Nullable MinecraftVersion targetVersionSince, @Nullable MinecraftVersion targetVersionUntil)
    {
        return MinecraftVersion.current().isInRange(targetVersionSince, targetVersionUntil);
    }

    private static boolean canExtend(@Nullable MinecraftVersion targetVersionSince)
    {
        return canExtend(targetVersionSince, null);
    }

    @Override
    public @NotNull <T extends Structure> Map<String, Object> serialize(@NotNull T structure, @Nullable Class<T> clazz)
    {
        // エンティティの場合は, さらに EntityType で分岐する
        if (isEntityRelatedStructure(structure, clazz))
            // noinspection unchecked
            return SelectiveEntityStructureSerializer.serialize((EntityStructure) structure, this, (Class<? extends EntityStructure>) clazz);

        return this.selectEntry(structure, clazz).getSerializer().apply(structure, this);
    }

    @Override
    public <T extends Structure> @NotNull T deserialize(@NotNull StructuredYamlNode node, @NotNull Class<T> clazz) throws YamlParsingException
    {
        // エンティティの場合は, さらに EntityType で分岐する
        if (isEntityRelatedStructure(null, clazz))
            // noinspection unchecked
            return (T) SelectiveEntityStructureSerializer.deserialize(node, this, (Class<? extends EntityStructure>) clazz);

        return this.selectEntry(clazz).getDeserializer().apply(node, this);
    }

    @Override
    public <T extends Structure> void validate(@NotNull StructuredYamlNode node, @NotNull Class<T> clazz) throws YamlParsingException
    {
        // エンティティの場合は, さらに EntityType で分岐する
        if (isEntityRelatedStructure(null, clazz))
        {
            // noinspection unchecked
            SelectiveEntityStructureSerializer.validate(node, this, (Class<? extends EntityStructure>) clazz);
            return;
        }
        this.selectEntry(clazz).getValidator().accept(node, this);
    }

    @Override
    public <V, T extends Structure> T toStructure(@NotNull V value, @Nullable Class<T> clazz)
    {
        // エンティティの場合は, さらに EntityType で分岐する
        if (isEntityRelatedValue(value, clazz))
            return SelectiveEntityStructureSerializer.toStructure((Entity) value, this);

        return this.selectMappedEntry(value, clazz).getConstructor().apply(value);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean canConvertToStructure(@NotNull Object value)
    {
        if (isEntityRelatedValue(value, null))
            return true;  // エンティティの場合は, 裏でフォールバックが効くので true を返す

        return this.structureEntries.stream()
                .filter(entry -> entry instanceof MappedStructureEntry)
                .map(entry -> (MappedStructureEntry) entry)
                .anyMatch(entry -> entry.getApplicator().test(value));
    }

    @Override
    public <V, T extends Structure> T toStructure(@NotNull V value)
    {
        return this.toStructure(value, null);  // 自動推論
    }

    @Override
    public <E extends Entity> @NotNull EntitySpecifier<E> tryDeserializeEntitySpecifier(@Nullable Object obj, Class<? extends EntityStructure> structureClass)
    {
        return EntitySpecifierImpl.tryDeserialize(obj, this, structureClass);
    }

    @Override
    public @NotNull PlayerSpecifier tryDeserializePlayerSpecifier(@Nullable Object obj) throws YamlParsingException
    {
        return PlayerSpecifierImpl.tryDeserializePlayer(obj, this);
    }

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

    // <editor-fold desc="すべての Structure を登録するメソッド">

    private <T extends Structure> StructureEntry<T> guessEntry(@NotNull T value)
    {
        // noinspection unchecked
        return (StructureEntry<T>) this.structureEntries.stream()
                .filter(entry -> entry.getClazz().isInstance(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown structure class: " + value.getClass()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <V, T extends Structure> MappedStructureEntry<V, T> selectMappedEntry(@NotNull V value, @Nullable Class<T> clazz)
    {
        Predicate<MappedStructureEntry> applicator;
        if (clazz == null)
            applicator = entry -> entry.getApplicator().test(value);
        else
            applicator = entry -> entry.getClazz().equals(clazz);

        return this.structureEntries.stream()
                .filter(entry -> entry instanceof MappedStructureEntry)
                .map(entry -> (MappedStructureEntry) entry)
                .filter(applicator)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown structure class: " + value.getClass()));
    }

    @SneakyThrows
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


        this.registerStructure(
                VersionRange.class,
                VersionRangeImpl::serialize,
                VersionRangeImpl::deserialize,
                VersionRangeImpl::validate
        );


    }

    @SneakyThrows
    private void registerContextStructures()
    {
        this.registerStructure(
                ContextStructure.class,
                ContextStructureImpl::serialize,
                ContextStructureImpl::deserialize,
                ContextStructureImpl::validate
        );/*
        this.registerStructure(
                PlayerStructure.class,
                PlayerStructureImpl::serialize,
                PlayerStructureImpl::deserialize,
                (BiConsumer<Map<String, Object>, StructureSerializer>) PlayerStructureImpl::validate,
                PlayerStructureImpl::of
        );*/
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
                DamageStructureImpl::validate,
                DamageStructureImpl::of,
                DamageStructureImpl::isApplicable
        );
        this.registerStructure(
                EntityStructure.class,
                EntityStructureImpl::serialize,
                EntityStructureImpl::deserialize,
                EntityStructureImpl::validate,
                EntityStructureImpl::of,
                EntityStructureImpl::isApplicable
        );
    }

    @SneakyThrows
    private void registerInventoryStructures()
    {
        this.registerStructure(
                InventoryStructure.class,
                InventoryStructureImpl::serialize,
                InventoryStructureImpl::deserialize,
                InventoryStructureImpl::validate,
                InventoryStructureImpl::of,
                InventoryStructureImpl::isApplicableInventory
        );
        this.registerStructure(
                ItemStackStructure.class,
                ItemStackStructureImpl::serialize,
                ItemStackStructureImpl::deserialize,
                ItemStackStructureImpl::validate,
                ItemStackStructureImpl::of,
                ItemStackStructureImpl::isApplicable
        );
        this.registerStructure(
                PlayerInventoryStructure.class,
                PlayerInventoryStructureImpl::serializePlayerInventory,
                PlayerInventoryStructureImpl::deserializePlayerInventory,
                PlayerInventoryStructureImpl::validatePlayerInventory,
                PlayerInventoryStructureImpl::ofPlayerInventory,
                PlayerInventoryStructureImpl::isApplicablePlayerInventory
        );
    }

    private void registerMiscStructures()
    {
        this.registerStructure(
                BlockStructure.class,
                BlockStructureImpl::serialize,
                BlockStructureImpl::deserialize,
                BlockStructureImpl::validate,
                BlockStructureImpl::of,
                BlockStructureImpl::isApplicable
        );

        this.registerStructure(
                LocationStructure.class,
                LocationStructureImpl::serialize,
                LocationStructureImpl::deserialize,
                LocationStructureImpl::validate,
                LocationStructureImpl::of,
                LocationStructureImpl::isApplicable
        );
    }

    @SneakyThrows
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

    // </editor-fold>


    // <editor-fold desc="Structure 登録用のメソッド"

    @SneakyThrows
    private void registerTriggerStructures()
    {
        this.registerStructure(
                TriggerStructure.class,
                TriggerStructureImpl::serialize,
                TriggerStructureImpl::deserialize,
                TriggerStructureImpl::validate
        );
    }

    @SneakyThrows
    private void registerHelpers()
    {
        this.registerStructure(
                ProjectileSourceStructure.class,
                ProjectileSourceSerializeHelper::serialize,
                ProjectileSourceSerializeHelper::deserialize,
                ProjectileSourceSerializeHelper::validate
        );
    }

    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                         @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                         @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, serializer, deserializer, validator));
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                         @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                         @NotNull ThrowableConsumer<StructuredYamlNode> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, serializer, deserializer, (v, t) -> validator.accept(v)));
    }

    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                         @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                         @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, (v, t) -> serializer.apply(v), deserializer, validator));
    }

    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                         @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                         @NotNull ThrowableConsumer<StructuredYamlNode> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, (v, t) -> serializer.apply(v), deserializer, (v, t) -> validator.accept(v)));
    }

    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                         @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                         @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, (v, t) -> serializer.apply(v), (v, t) -> deserializer.apply(v), validator));
    }

    /* ------------------------------------ */

    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                         @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                         @NotNull ThrowableConsumer<StructuredYamlNode> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, (v, t) -> serializer.apply(v), (v, t) -> deserializer.apply(v), (v, t) -> validator.accept(v)));
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                         @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                         @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                         @NotNull ThrowableConsumer<StructuredYamlNode> validator)
    {
        this.structureEntries.add(new StructureEntry<>(clazz, serializer, (v, t) -> deserializer.apply(v), (v, t) -> validator.accept(v)));
    }

    private <V, T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                            @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                            @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                            @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator,
                                                            @NotNull Function<V, T> constructor,
                                                            @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, serializer, deserializer, validator, constructor, applicator));
    }

    private <V, T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                            @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                            @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                            @NotNull ThrowableConsumer<StructuredYamlNode> validator,
                                                            @NotNull Function<V, T> constructor,
                                                            @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, serializer, deserializer, (v, t) -> validator.accept(v), constructor, applicator));
    }

    private <V, T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                            @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                            @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                            @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator,
                                                            @NotNull Function<V, T> constructor,
                                                            @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, (v, t) -> serializer.apply(v), deserializer, validator, constructor, applicator));
    }

    private <V, T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                            @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                            @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                            @NotNull ThrowableConsumer<StructuredYamlNode> validator,
                                                            @NotNull Function<V, T> constructor,
                                                            @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, (v, t) -> serializer.apply(v), deserializer, (v, t) -> validator.accept(v), constructor, applicator));
    }

    private <V, T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                            @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                            @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                            @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator,
                                                            @NotNull Function<V, T> constructor,
                                                            @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, (v, t) -> serializer.apply(v), (v, t) -> deserializer.apply(v), validator, constructor, applicator));
    }

    /* ------------------------------------ */

    private <V, T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                            @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                            @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                            @NotNull ThrowableConsumer<StructuredYamlNode> validator,
                                                            @NotNull Function<V, T> constructor,
                                                            @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, (v, t) -> serializer.apply(v), (v, t) -> deserializer.apply(v), (v, t) -> validator.accept(v), constructor, applicator));
    }

    private <V, T extends Structure> void registerStructure(@NotNull Class<T> clazz,
                                                            @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                            @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                            @NotNull ThrowableConsumer<StructuredYamlNode> validator,
                                                            @NotNull Function<V, T> constructor,
                                                            @NotNull Predicate<?> applicator)
    {
        this.structureEntries.add(new MappedStructureEntry<>(clazz, serializer, (v, t) -> deserializer.apply(v), (v, t) -> validator.accept(v), constructor, applicator));
    }

    private void removeStructureFor(@NotNull Class<?> structureClazz)
    {
        Iterator<StructureEntry<?>> iterator = this.structureEntries.iterator();
        while (iterator.hasNext())
        {
            StructureEntry<?> entry = iterator.next();
            if (entry.getClazz() == structureClazz) // Class は VM で一意なので == で比較する
            {
                iterator.remove();
                return;
            }
        }
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull MinecraftVersion mcVersionUntil,
                                                       @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                       @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                       @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator);
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull MinecraftVersion mcVersionUntil,
                                                       @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                       @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                       @NotNull ThrowableConsumer<StructuredYamlNode> validator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator);
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull MinecraftVersion mcVersionUntil,
                                                       @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                       @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                       @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator);
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull MinecraftVersion mcVersionUntil,
                                                       @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                       @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                       @NotNull ThrowableConsumer<StructuredYamlNode> validator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator);
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull MinecraftVersion mcVersionUntil,
                                                       @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                       @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                       @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator);
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull MinecraftVersion mcVersionUntil,
                                                       @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                       @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                       @NotNull ThrowableConsumer<StructuredYamlNode> validator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator);
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull MinecraftVersion mcVersionUntil,
                                                       @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                       @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                       @NotNull ThrowableConsumer<StructuredYamlNode> validator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull MinecraftVersion mcVersionUntil,
                                                          @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                          @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                          @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator, constructor, applicator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull MinecraftVersion mcVersionUntil,
                                                          @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                          @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                          @NotNull ThrowableConsumer<StructuredYamlNode> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator, constructor, applicator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull MinecraftVersion mcVersionUntil,
                                                          @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                          @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                          @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator, constructor, applicator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull MinecraftVersion mcVersionUntil,
                                                          @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                          @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                          @NotNull ThrowableConsumer<StructuredYamlNode> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator, constructor, applicator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull MinecraftVersion mcVersionUntil,
                                                          @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                          @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                          @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator, constructor, applicator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull MinecraftVersion mcVersionUntil,
                                                          @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                          @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                          @NotNull ThrowableConsumer<StructuredYamlNode> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator, constructor, applicator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull MinecraftVersion mcVersionUntil,
                                                          @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                          @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                          @NotNull ThrowableConsumer<StructuredYamlNode> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        if (!canExtend(mcVersionSince, mcVersionUntil))
            return;

        this.removeStructureFor(clazz);
        this.registerStructure(clazz, serializer, deserializer, validator, constructor, applicator);
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                       @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                       @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator);
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                       @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                       @NotNull ThrowableConsumer<StructuredYamlNode> validator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator);
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                       @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                       @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator);
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                       @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                       @NotNull ThrowableConsumer<StructuredYamlNode> validator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator);
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                       @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                       @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator);
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                       @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                       @NotNull ThrowableConsumer<StructuredYamlNode> validator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator);
    }

    private <T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                       @NotNull MinecraftVersion mcVersionSince,
                                                       @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                       @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                       @NotNull ThrowableConsumer<StructuredYamlNode> validator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                          @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                          @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator, constructor, applicator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                                          @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                          @NotNull ThrowableConsumer<StructuredYamlNode> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator, constructor, applicator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                          @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                          @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator, constructor, applicator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                          @NotNull ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                                          @NotNull ThrowableConsumer<StructuredYamlNode> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator, constructor, applicator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                          @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                          @NotNull ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator, constructor, applicator);
    }

    private <V, T extends Structure> void extendStructure(@NotNull Class<T> clazz,
                                                          @NotNull MinecraftVersion mcVersionSince,
                                                          @NotNull Function<? super T, ? extends Map<String, Object>> serializer,
                                                          @NotNull ThrowableFunction<StructuredYamlNode, ? extends T> deserializer,
                                                          @NotNull ThrowableConsumer<StructuredYamlNode> validator,
                                                          @NotNull Function<V, T> constructor,
                                                          @NotNull Predicate<?> applicator)
    {
        this.extendStructure(clazz, mcVersionSince, null, serializer, deserializer, validator, constructor, applicator);
    }

    // </editor-fold>

    /* non-public */ interface ThrowableBiFunction<T, U, R>
    {
        R apply(T t, U u) throws YamlParsingException;
    }

    /* non-public */ interface ThrowableBiConsumer<T, U>
    {
        void accept(T t, U u) throws YamlParsingException;
    }

    /* non-public */ interface ThrowableFunction<T, R>
    {
        R apply(T t) throws YamlParsingException;
    }

    /* non-public */ interface ThrowableConsumer<T>
    {
        void accept(T t) throws YamlParsingException;
    }

    @Data
    @NotNull
    @AllArgsConstructor
    public static class StructureEntry<T extends Structure>
    {
        Class<T> clazz;
        BiFunction<T, StructureSerializer, Map<String, Object>> serializer;
        ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer;
        ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator;
    }

    @Value
    @NotNull
    @EqualsAndHashCode(callSuper = true)
    private static class MappedStructureEntry<V, T extends Structure> extends StructureEntry<T>
    {
        Function<V, T> constructor;
        Predicate<?> applicator;

        public MappedStructureEntry(Class<T> clazz, BiFunction<T, StructureSerializer, Map<String, Object>> serializer,
                                    ThrowableBiFunction<StructuredYamlNode, StructureSerializer, T> deserializer,
                                    ThrowableBiConsumer<StructuredYamlNode, StructureSerializer> validator,
                                    Function<V, T> constructor, Predicate<?> applicator)
        {
            super(clazz, serializer, deserializer, validator);
            this.constructor = constructor;
            this.applicator = applicator;
        }
    }
}
