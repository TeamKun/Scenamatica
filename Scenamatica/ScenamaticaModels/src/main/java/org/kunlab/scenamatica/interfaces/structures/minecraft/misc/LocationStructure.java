package org.kunlab.scenamatica.interfaces.structures.minecraft.misc;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

/**
 * Location のための Structure です。
 */@TypeDoc(
        name = "Location",
        description = "オブジェクトの位置情報を格納します。",
        mappingOf = Location.class,
        properties = {
                @TypeProperty(
                        name = LocationStructure.KEY_X,
                        description = "X 座標です。",
                        type = double.class
                ),
                @TypeProperty(
                        name = LocationStructure.KEY_Y,
                        description = "Y 座標です。",
                        type = double.class
                ),
                @TypeProperty(
                        name = LocationStructure.kEY_Z,
                        description = "Z 座標です。",
                        type = double.class
                ),
                @TypeProperty(
                        name = LocationStructure.KEY_YAW,
                        description = "水平方向の向きです。",
                        type = float.class
                ),
                @TypeProperty(
                        name = LocationStructure.KEY_PITCH,
                        description = "垂直方向の向きです。",
                        type = float.class
                ),
                @TypeProperty(
                        name = LocationStructure.KEY_WORLD,
                        description = "ワールド名です。",
                        type = String.class,
                        pattern = "[a-zA-Z0-9_]+"
                )
        }
)
@Category(inherit = MiscCategoryInfo.class)
public interface LocationStructure extends Structure, Mapped<Location>, Creatable<Location>
{
    String KEY_X = "x";
    String KEY_Y = "y";
    String kEY_Z = "z";
    String KEY_YAW = "yaw";
    String KEY_PITCH = "pitch";
    String KEY_WORLD = "world";

    /**
     * X 座標を取得します。
     *
     * @return X 座標
     */
    Double getX();

    /**
     * Y 座標を取得します。
     *
     * @return Y 座標
     */
    Double getY();

    /**
     * Z 座標を取得します。
     *
     * @return Z 座標
     */
    Double getZ();

    /**
     * Yaw(水平方向の向き) を取得します。
     * 0 <= yaw < 360
     *
     * @return Yaw
     */
    Float getYaw();

    /**
     * Pitch(垂直方向の向き) を取得します。
     * -90 <= pitch <= 90
     *
     * @return Pitch
     */
    Float getPitch();

    /**
     * ワールド名を取得します。
     *
     * @return ワールド名
     */
    String getWorld();

    /**
     * ワールドを変更し、あたらしい LocationStructure を返します。
     *
     * @param world ワールド名
     * @return 新しい LocationStructure
     */
    LocationStructure changeWorld(String world);

    /**
     * ワールドを動的に指定して Location を作成します。
     * @param world ワールド
     * @return Location
     */
    Location create(@Nullable World world);

    @Override
    Location create();

    @Override
    default boolean canApplyTo(@Nullable Object target)
    {
        return target instanceof Location;
    }
}
