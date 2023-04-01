package net.kunmc.lab.scenamatica.scenariofile.beans.context;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.ContextBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.StageBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
public class ContextBeanImpl implements ContextBean
{
    private static final String KEY_ACTORS = "actors";
    private static final String KEY_STAGE = "stage";

    @NotNull
    List<PlayerBean> actors;

    @Nullable
    StageBean world;

    public static Map<String, Object> serialize(ContextBean bean)
    {
        Map<String, Object> map = new HashMap<>();

        if (!bean.getActors().isEmpty())
        {
            List<Map<String, Object>> actors = new ArrayList<>();
            for (PlayerBean player : bean.getActors())
                actors.add(PlayerBeanImpl.serialize(player));

            map.put(KEY_ACTORS, actors);
        }

        if (bean.getWorld() != null)
            map.put(KEY_STAGE, StageBeanImpl.serialize(bean.getWorld()));

        return map;
    }

    public static void validate(Map<String, Object> map)
    {
        if (map.containsKey(KEY_ACTORS))
        {
            Object actors = map.get(KEY_ACTORS);
            if (!(actors instanceof List))
                throw new IllegalArgumentException("actors must be List");

            for (Object player : (List<?>) actors)
                PlayerBeanImpl.validate(MapUtils.checkAndCastMap(
                        player,
                        String.class,
                        Object.class
                ));
        }

        if (map.containsKey(KEY_STAGE))
            StageBeanImpl.validate(MapUtils.checkAndCastMap(
                    map.get(KEY_STAGE),
                    String.class,
                    Object.class
            ));
    }

    public static ContextBean deserialize(Map<String, Object> map)
    {
        List<PlayerBean> actorList = new ArrayList<>();
        if (map.containsKey(KEY_ACTORS) && map.get(KEY_ACTORS) != null)
        {
            for (Object player : (List<?>) map.get(KEY_ACTORS))
                actorList.add(PlayerBeanImpl.deserialize(MapUtils.checkAndCastMap(
                        player,
                        String.class,
                        Object.class
                )));
        }

        StageBean world = null;
        if (map.containsKey(KEY_STAGE))
            world = StageBeanImpl.deserialize(MapUtils.checkAndCastMap(
                    map.get(KEY_STAGE),
                    String.class,
                    Object.class
            ));

        return new ContextBeanImpl(
                actorList,
                world
        );
    }
}
