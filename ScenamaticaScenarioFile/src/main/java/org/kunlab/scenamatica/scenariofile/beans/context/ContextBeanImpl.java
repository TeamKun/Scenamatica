package org.kunlab.scenamatica.scenariofile.beans.context;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.context.ContextBean;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.kunlab.scenamatica.interfaces.scenariofile.context.StageBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
public class ContextBeanImpl implements ContextBean
{
    public static final String KEY_ACTORS = "actors";
    public static final String KEY_ENTITIES = "entities";
    public static final String KEY_STAGE = "stage";

    @NotNull
    List<PlayerBean> actors;
    @NotNull
    List<EntityBean> entities;

    StageBean world;

    @NotNull
    public static Map<String, Object> serialize(@NotNull ContextBean bean, @NotNull BeanSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();

        if (!bean.getActors().isEmpty())
        {
            List<Map<String, Object>> actors = new ArrayList<>();
            for (PlayerBean player : bean.getActors())
                actors.add(serializer.serialize(player, PlayerBean.class));

            map.put(KEY_ACTORS, actors);
        }

        if (!bean.getEntities().isEmpty())
        {
            List<Map<String, Object>> entities = new ArrayList<>();
            for (EntityBean entity : bean.getEntities())
                entities.add(serializer.serialize(entity, EntityBean.class));

            map.put(KEY_ENTITIES, entities);
        }

        if (bean.getWorld() != null)
            map.put(KEY_STAGE, serializer.serialize(bean.getWorld(), StageBean.class));

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        if (map.containsKey(KEY_ACTORS))
        {
            Object actors = map.get(KEY_ACTORS);
            if (!(actors instanceof List))
                throw new IllegalArgumentException("actors must be List");

            for (Object player : (List<?>) actors)
                serializer.validate(
                        MapUtils.checkAndCastMap(player),
                        PlayerBean.class
                );
        }

        if (map.containsKey(KEY_ENTITIES))
        {
            Object entities = map.get(KEY_ENTITIES);
            if (!(entities instanceof List))
                throw new IllegalArgumentException("entities must be List");

            for (Object entity : (List<?>) entities)
                serializer.validate(
                        MapUtils.checkAndCastMap(entity),
                        EntityBean.class
                );
        }

        if (map.containsKey(KEY_STAGE))
            serializer.validate(
                    MapUtils.checkAndCastMap(map.get(KEY_STAGE)),
                    StageBean.class
            );
    }

    @NotNull
    public static ContextBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        List<PlayerBean> actorList = new ArrayList<>();
        if (map.containsKey(KEY_ACTORS) && map.get(KEY_ACTORS) != null)
        {
            for (Object player : (List<?>) map.get(KEY_ACTORS))
                actorList.add(serializer.deserialize(
                        MapUtils.checkAndCastMap(player), PlayerBean.class));
        }

        List<EntityBean> entityList = new ArrayList<>();
        if (map.containsKey(KEY_ENTITIES) && map.get(KEY_ENTITIES) != null)
        {
            for (Object entity : (List<?>) map.get(KEY_ENTITIES))
                entityList.add(serializer.deserialize(
                        MapUtils.checkAndCastMap(entity), EntityBean.class));
        }

        StageBean world = null;
        if (map.containsKey(KEY_STAGE))
            world = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_STAGE)), StageBean.class);

        return new ContextBeanImpl(
                actorList,
                entityList,
                world
        );
    }
}
