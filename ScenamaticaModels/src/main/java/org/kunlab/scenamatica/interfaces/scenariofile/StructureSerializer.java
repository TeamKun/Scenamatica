package org.kunlab.scenamatica.interfaces.scenariofile;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.Map;

/**
 * シナリオの Structure をシリアライズ、デシリアライズまたはその Map を検証します。
 */
public interface StructureSerializer
{
    /**
     * Structure をシリアライズします。
     *
     * @param structure シリアライズする Structure
     * @param clazz     シリアライズする Structure のクラス
     * @param <T>       シリアライズする Structure の型
     * @return シリアライズされた Structure の Map
     * @throws IllegalArgumentException シリアライズに失敗した場合
     */
    @NotNull <T extends Structure> Map<String, Object> serialize(@NotNull T structure, @NotNull Class<T> clazz);

    /**
     * Structure をデシリアライズします。
     *
     * @param map   デシリアライズする Structure の Map
     * @param clazz デシリアライズする Structure のクラス
     * @param <T>   デシリアライズする Structure の型
     * @return デシリアライズされた Structure
     * @throws IllegalArgumentException デシリアライズに失敗した場合
     */
    @NotNull <T extends Structure> T deserialize(@NotNull Map<String, Object> map, @NotNull Class<T> clazz);

    /**
     * Structure を検証します。
     *
     * @param map   検証する Structure の Map
     * @param clazz 検証する Structure のクラス
     * @param <T>   検証する Structure の型
     * @throws IllegalArgumentException 検証に失敗した場合
     */
    <T extends Structure> void validate(@NotNull Map<String, Object> map, @NotNull Class<T> clazz);

    /**
     * エンティティ指定子をデシリアライズします。
     *
     * @param obj            エンティティ指定子を表すオブジェクト
     * @param structureClass エンティティ指定子の構造クラス
     * @param <E>            エンティティの型
     * @return デシリアライズされたエンティティ指定子
     */
    @NotNull <E extends Entity> EntitySpecifier<E> tryDeserializeEntitySpecifier(
            @Nullable Object obj,
            Class<? extends EntityStructure> structureClass
    );

    /**
     * エンティティ指定子をデシリアライズします。
     *
     * @param obj エンティティ指定子を表すオブジェクト
     */
    @NotNull
    default EntitySpecifier<Entity> tryDeserializeEntitySpecifier(@Nullable Object obj)
    {
        return this.tryDeserializeEntitySpecifier(obj, EntityStructure.class);
    }

    /**
     * プレイヤ指定子をデシリアライズします。
     *
     * @param obj プレイヤ指定子を表すオブジェクト
     * @return デシリアライズされたプレイヤ指定子
     */
    @NotNull
    PlayerSpecifier tryDeserializePlayerSpecifier(@Nullable Object obj);

}
