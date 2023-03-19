package net.kunmc.lab.scenamatica.scenariofile.beans.context;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * シナリオの実行に必要な情報を表すクラスです。
 */
@Value
public class ContextBean
{
    private static final String KEY_PSEUDO_PLAYERS = "pseudoPlayers";
    private static final String KEY_WORLD = "world";

    /**
     * 仮想プレイヤーを定義します。
     */
    @Nullable
    List<PlayerBean> pseudoPlayers;

    /**
     * ワールドを定義します。
     */
    @Nullable
    WorldBean world;

    /**
     * シナリオの実行に必要な情報をMapにシリアライズします。
     *
     * @return シリアライズされた情報
     */
    public static Map<String, Object> serialize(ContextBean bean)
    {
        Map<String, Object> map = new HashMap<>();

        if (bean.pseudoPlayers != null)
        {
            List<Map<String, Object>> pseudoPlayers = new ArrayList<>();
            for (PlayerBean player : bean.pseudoPlayers)
                pseudoPlayers.add(PlayerBean.serialize(player));

            map.put(KEY_PSEUDO_PLAYERS, pseudoPlayers);
        }

        if (bean.world != null)
            map.put(KEY_WORLD, WorldBean.serialize(bean.world));

        return map;
    }

    /**
     * Mapがシナリオの実行に必要な情報を表しているかどうかを検証します。
     *
     * @param map 検証するMap
     * @throws IllegalArgumentException シナリオの実行に必要な情報を表していない場合
     */
    public static void validateMap(Map<String, Object> map)
    {
        if (map.containsKey(KEY_PSEUDO_PLAYERS))
        {
            Object pseudoPlayers = map.get(KEY_PSEUDO_PLAYERS);
            if (!(pseudoPlayers instanceof List))
                throw new IllegalArgumentException("pseudoPlayers must be List");

            for (Object player : (List<?>) pseudoPlayers)
                PlayerBean.validateMap(MapUtils.checkAndCastMap(
                        player,
                        String.class,
                        Object.class
                ));
        }

        if (map.containsKey(KEY_WORLD))
            WorldBean.validateMap(MapUtils.checkAndCastMap(
                    map.get(KEY_WORLD),
                    String.class,
                    Object.class
            ));
    }

    /**
     * シリアライズされた情報をデシリアライズします。
     *
     * @param map シリアライズされた情報
     * @return デシリアライズされた情報
     */
    public static ContextBean deserialize(Map<String, Object> map)
    {
        List<PlayerBean> pseudoPlayerslist = null;
        if (map.containsKey(KEY_PSEUDO_PLAYERS))
        {
            pseudoPlayerslist = new ArrayList<>();
            for (Object player : (List<?>) map.get(KEY_PSEUDO_PLAYERS))
                pseudoPlayerslist.add(PlayerBean.deserialize(MapUtils.checkAndCastMap(
                        player,
                        String.class,
                        Object.class
                )));
        }

        WorldBean world = null;
        if (map.containsKey(KEY_WORLD))
            world = WorldBean.deserialize(MapUtils.checkAndCastMap(
                    map.get(KEY_WORLD),
                    String.class,
                    Object.class
            ));

        return new ContextBean(
                pseudoPlayerslist,
                world
        );
    }
}
