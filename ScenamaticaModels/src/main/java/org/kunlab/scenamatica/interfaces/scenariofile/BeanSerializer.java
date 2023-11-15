package org.kunlab.scenamatica.interfaces.scenariofile;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * シナリオの Bean をシリアライズ、デシリアライズまたはその Map を検証します。
 */
public interface BeanSerializer
{
    /**
     * Bean をシリアライズします。
     *
     * @param bean  シリアライズする Bean
     * @param clazz シリアライズする Bean のクラス
     * @param <T>   シリアライズする Bean の型
     * @return シリアライズされた Bean の Map
     * @throws IllegalArgumentException シリアライズに失敗した場合
     */
    @NotNull <T extends Bean> Map<String, Object> serialize(@NotNull T bean, @NotNull Class<T> clazz);

    /**
     * Bean をデシリアライズします。
     *
     * @param map   デシリアライズする Bean の Map
     * @param clazz デシリアライズする Bean のクラス
     * @param <T>   デシリアライズする Bean の型
     * @return デシリアライズされた Bean
     * @throws IllegalArgumentException デシリアライズに失敗した場合
     */
    @NotNull <T extends Bean> T deserialize(@NotNull Map<String, Object> map, @NotNull Class<T> clazz);

    /**
     * Bean を検証します。
     *
     * @param map   検証する Bean の Map
     * @param clazz 検証する Bean のクラス
     * @param <T>   検証する Bean の型
     * @throws IllegalArgumentException 検証に失敗した場合
     */
    <T extends Bean> void validate(@NotNull Map<String, Object> map, @NotNull Class<T> clazz);
}
