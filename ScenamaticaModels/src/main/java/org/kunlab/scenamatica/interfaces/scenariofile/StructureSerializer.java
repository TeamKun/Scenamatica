package org.kunlab.scenamatica.interfaces.scenariofile;

import org.jetbrains.annotations.NotNull;

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
}
