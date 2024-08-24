package org.kunlab.scenamatica.interfaces.scenariofile;

import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;

/**
 * バージョンの範囲を表すインターフェースです。
 */
@TypeDoc(
        name = "バージョン範囲",
        description = "バージョンの範囲を表します。",
        properties = {
                @TypeProperty(
                        name = VersionRange.KEY_SINCE,
                        type = String.class,
                        pattern = "^v?(?<major>0|[1-9]\\d*)\\.(?<minor>0|[1-9]\\d*)(?:\\.(?<patch>0|[1-9]\\d*))?(?:-(?<preRelease>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+(?<buildMetadata>[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$",
                        description = "最小のバージョンを指定します。"
                ),
                @TypeProperty(
                        name = VersionRange.KEY_UNTIL,
                        type = String.class,
                        pattern = "^v?(?<major>0|[1-9]\\d*)\\.(?<minor>0|[1-9]\\d*)(?:\\.(?<patch>0|[1-9]\\d*))?(?:-(?<preRelease>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+(?<buildMetadata>[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$",
                        description = "最大のバージョンを指定します。"
                ),

        }
)
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
