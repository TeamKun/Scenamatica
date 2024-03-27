package org.kunlab.scenamatica.interfaces.scenariofile;

import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.jetbrains.annotations.Nullable;

/**
 * バージョンの範囲を表すインターフェースです。
 */
public interface VersionRange extends Structure
{
    String KEY_SINCE = "since";
    String KEY_UNTIL = "until";

    /**
     * このバージョン範囲の最小のバージョンを取得します。
     *
     * @return 最小のバージョン
     */
    @Nullable
    Version getSince();

    /**
     * このバージョン範囲の最大のバージョンを取得します。
     *
     * @return 最大のバージョン
     */
    @Nullable
    Version getUntil();

    /**
     * 指定されたバージョンがこの範囲に含まれるかどうかを判定します。
     *
     * @param version バージョン
     * @return 含まれる場合は true
     */
    boolean isInRange(Version version);
}
