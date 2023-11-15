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
                actors.add(serializer.serializePlayer(player));

            map.put(KEY_ACTORS, actors);
        }

        if (!bean.getEntities().isEmpty())
        {
            List<Map<String, Object>> entities = new ArrayList<>();
            for (EntityBean entity : bean.getEntities())
                entities.add(serializer.serializeEntity(entity));

            map.put(KEY_ENTITIES, entities);
        }

        if (bean.getWorld() != null)
            map.put(KEY_STAGE, serializer.serializeStage(bean.getWorld()));

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
                serializer.validatePlayer(MapUtils.checkAndCastMap(
                        player,
                        String.class,
                        Object.class
                ));
        }

        if (map.containsKey(KEY_ENTITIES))
        {
            Object entities = map.get(KEY_ENTITIES);
            if (!(entities instanceof List))
                throw new IllegalArgumentException("entities must be List");

            for (Object entity : (List<?>) entities)
                serializer.validateEntity(MapUtils.checkAndCastMap(
                        entity,
                        String.class,
                        Object.class
                ));
        }

        if (map.containsKey(KEY_STAGE))
            serializer.validateStage(MapUtils.checkAndCastMap(
                    map.get(KEY_STAGE),
                    String.class,
                    Object.class
            ));
    }

    @NotNull
    public static ContextBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        List<PlayerBean> actorList = new ArrayList<>();
        if (map.containsKey(KEY_ACTORS) && map.get(KEY_ACTORS) != null)
        {
            for (Object player : (List<?>) map.get(KEY_ACTORS))
                actorList.add(serializer.deserializePlayer(MapUtils.checkAndCastMap(
                        player,
                        String.class,
                        Object.class
                )));
        }

        List<EntityBean> entityList = new ArrayList<>();
        if (map.containsKey(KEY_ENTITIES) && map.get(KEY_ENTITIES) != null)
        {
            for (Object entity : (List<?>) map.get(KEY_ENTITIES))
                entityList.add(serializer.deserializeEntity(MapUtils.checkAndCastMap(
                        entity,
                        String.class,
                        Object.class
                )));
        }

        StageBean world = null;
        if (map.containsKey(KEY_STAGE))
            world = serializer.deserializeStage(MapUtils.checkAndCastMap(
                    map.get(KEY_STAGE),
                    String.class,
                    Object.class
            ));

        return new ContextBeanImpl(
                actorList,
                entityList,
                world
        );
    }
}
