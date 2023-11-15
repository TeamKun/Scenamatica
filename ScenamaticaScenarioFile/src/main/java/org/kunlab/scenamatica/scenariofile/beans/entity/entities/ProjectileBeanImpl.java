package org.kunlab.scenamatica.scenariofile.beans.entity.entities;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileBean;
import org.kunlab.scenamatica.scenariofile.beans.entity.EntityBeanImpl;

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
        Map<String, Object> map = serializer.serialize(bean, EntityBean.class);

        if (bean.getShooter() != null)
            MapUtils.putMapIfNotEmpty(map, KEY_SHOOTER, serializer.serialize(bean.getShooter(), EntityBean.class));
        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        serializer.validate(map, EntityBean.class);

        if (map.containsKey(KEY_SHOOTER))
        {
            Map<String, Object> shooterMap = MapUtils.checkAndCastMap(map.get(KEY_SHOOTER));
            serializer.validate(shooterMap, EntityBean.class);
        }
    }

    @NotNull
    public static ProjectileBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        validate(map, serializer);

        EntityBean entity = serializer.deserialize(map, EntityBean.class);

        EntityBean shooter = null;
        if (map.containsKey(KEY_SHOOTER))
        {
            Map<String, Object> shooterMap = MapUtils.checkAndCastMap(map.get(KEY_SHOOTER));
            shooter = serializer.deserialize(shooterMap, EntityBean.class);
        }

        return new ProjectileBeanImpl(entity, shooter);
    }
}
