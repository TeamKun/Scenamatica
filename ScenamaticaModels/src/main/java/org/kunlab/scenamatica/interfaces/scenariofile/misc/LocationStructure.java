package org.kunlab.scenamatica.interfaces.scenariofile.misc;

import org.bukkit.Location;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

/**
 * Location のための Structure です。
 */
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
}
