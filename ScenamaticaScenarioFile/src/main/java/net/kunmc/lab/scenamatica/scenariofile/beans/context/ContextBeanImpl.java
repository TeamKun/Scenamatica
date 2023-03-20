package net.kunmc.lab.scenamatica.scenariofile.beans.context;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.scenariofile.interfaces.context.ContextBean;
import net.kunmc.lab.scenamatica.scenariofile.interfaces.context.PlayerBean;
import net.kunmc.lab.scenamatica.scenariofile.interfaces.context.WorldBean;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
public class ContextBeanImpl implements ContextBean
{
    private static final String KEY_PSEUDO_PLAYERS = "pseudoPlayers";
    private static final String KEY_WORLD = "world";

    @Nullable
    List<PlayerBean> pseudoPlayers;

    @Nullable
    WorldBean world;

    public static Map<String, Object> serialize(ContextBean bean)
    {
        Map<String, Object> map = new HashMap<>();

        if (bean.getPseudoPlayers() != null)
        {
            List<Map<String, Object>> pseudoPlayers = new ArrayList<>();
            for (PlayerBean player : bean.getPseudoPlayers())
                pseudoPlayers.add(PlayerBeanImpl.serialize(player));

            map.put(KEY_PSEUDO_PLAYERS, pseudoPlayers);
        }

        if (bean.getWorld() != null)
            map.put(KEY_WORLD, WorldBeanImpl.serialize(bean.getWorld()));

        return map;
    }

    public static void validate(Map<String, Object> map)
    {
        if (map.containsKey(KEY_PSEUDO_PLAYERS))
        {
            Object pseudoPlayers = map.get(KEY_PSEUDO_PLAYERS);
            if (!(pseudoPlayers instanceof List))
                throw new IllegalArgumentException("pseudoPlayers must be List");

            for (Object player : (List<?>) pseudoPlayers)
                PlayerBeanImpl.validate(MapUtils.checkAndCastMap(
                        player,
                        String.class,
                        Object.class
                ));
        }

        if (map.containsKey(KEY_WORLD))
            WorldBeanImpl.validate(MapUtils.checkAndCastMap(
                    map.get(KEY_WORLD),
                    String.class,
                    Object.class
            ));
    }

    public static ContextBean deserialize(Map<String, Object> map)
    {
        List<PlayerBean> pseudoPlayerslist = null;
        if (map.containsKey(KEY_PSEUDO_PLAYERS) && map.get(KEY_PSEUDO_PLAYERS) != null)
        {
            pseudoPlayerslist = new ArrayList<>();
            for (Object player : (List<?>) map.get(KEY_PSEUDO_PLAYERS))
                pseudoPlayerslist.add(PlayerBeanImpl.deserialize(MapUtils.checkAndCastMap(
                        player,
                        String.class,
                        Object.class
                )));
        }

        WorldBean world = null;
        if (map.containsKey(KEY_WORLD))
            world = WorldBeanImpl.deserialize(MapUtils.checkAndCastMap(
                    map.get(KEY_WORLD),
                    String.class,
                    Object.class
            ));

        return new ContextBeanImpl(
                pseudoPlayerslist,
                world
        );
    }
}
