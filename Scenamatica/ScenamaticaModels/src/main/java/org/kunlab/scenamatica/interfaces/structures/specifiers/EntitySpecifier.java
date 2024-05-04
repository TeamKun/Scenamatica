package org.kunlab.scenamatica.interfaces.structures.specifiers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;

import java.util.List;
import java.util.Optional;

/**
 * エンティティの指定子を提供します。
 *
 * @param <E> エンティティの型
 */
public interface EntitySpecifier<E extends Entity> extends Structure
{
    /**
     * エンティティを選択可能かどうか取得します。
     *
     * @return エンティティを選択可能かどうか
     */
    boolean isSelectable();

    /**
     * エンティティを選択します。
     *
     * @param context コンテキスト
     * @return エンティティ
     */
    Optional<E> selectTarget(@Nullable Context context);

    /**
     * エンティティを選択します。
     *
     * @param basis   セレクタの基準となるエンティティ
     * @param context コンテキスト
     * @return エンティティ
     */
    Optional<E> selectTarget(@Nullable Player basis, @Nullable Context context);

    /**
     * エンティティを選択します。
     *
     * @param context コンテキスト
     * @return エンティティ
     */
    @NotNull
    List<? extends Entity> selectTargets(@Nullable Context context);

    /**
     * エンティティを選択します。
     *
     * @param basis   セレクタの基準となるエンティティ
     * @param context コンテキスト
     * @return エンティティ
     */
    @NotNull
    List<? extends Entity> selectTargets(@Nullable Player basis, @Nullable Context context);

    /**
     * セレクタを取得します。
     *
     * @return セレクタ
     */
    String getSelectorString();

    /**
     * セレクタ・エンティティ構造の生の値を取得します。
     *
     * @return 生の値
     */
    Object getTargetRaw();

    /**
     * ターゲットを提供できるかを取得します。
     *
     * @return ターゲットを提供できるか
     * @see #hasStructure()
     * @see #isSelectable()
     */
    boolean canProvideTarget();

    /**
     * 引数としての文字列を取得します。
     *
     * @return 引数としての文字列
     */
    String getArgumentString();

    /**
     * エンティティがセレクタにマッチするか確かめます。
     *
     * @param entity 対象のエンティティ
     * @return 結果
     */
    boolean checkMatchedEntity(Entity entity);

    /**
     * エンティティ構造を持っているかどうか取得します。
     *
     * @return エンティティ構造を持っているかどうか
     */
    boolean hasStructure();

    /**
     * ターゲットの構造を取得します。
     *
     * @return 構造
     */
    EntityStructure getTargetStructure();
}
