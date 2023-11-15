package org.kunlab.scenamatica.scenariofile.beans.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.ProjectileBean;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Value
public class ProjectileBeanImpl extends EntityBeanImpl implements ProjectileBean
{

    EntityBean shooter;  // ProjectileSource だが, launchProjectile 以外存在しないので EntityBean で代用。

    public ProjectileBeanImpl(@NotNull EntityBean original, @Nullable EntityBean shooter)
    {
        super(original);
        this.shooter = shooter;
    }

    public static Map<String, Object> serialize(@NotNull ProjectileBean bean, @NotNull BeanSerializer serializer)
    {
        Map<String, Object> map = serializer.serializeEntity(bean);

        if (bean.getShooter() != null)
            MapUtils.putIfNotNull(map, KEY_SHOOTER, serializer.serializeEntity(bean.getShooter()));
        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        serializer.validateEntity(map);

        if (map.containsKey(KEY_SHOOTER))
        {
            Map<String, Object> shooterMap = MapUtils.checkAndCastMap(map.get(KEY_SHOOTER), String.class, Object.class);
            serializer.validateEntity(shooterMap);
        }
    }

    @NotNull
    public static ProjectileBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        validate(map, serializer);

        EntityBean entity = serializer.deserializeEntity(map);

        EntityBean shooter = null;
        if (map.containsKey(KEY_SHOOTER))
        {
            Map<String, Object> shooterMap = MapUtils.checkAndCastMap(
                    map.get(KEY_SHOOTER),
                    String.class,
                    Object.class
            );
            shooter = serializer.deserializeEntity(shooterMap);
        }

        return new ProjectileBeanImpl(entity, shooter);
    }
}
