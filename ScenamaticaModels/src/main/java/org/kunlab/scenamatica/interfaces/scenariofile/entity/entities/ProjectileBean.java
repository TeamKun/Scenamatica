package org.kunlab.scenamatica.interfaces.scenariofile.entity.entities;

import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityBean;

/**
 * 投射物のインターフェースです。
 */
public interface ProjectileBean extends EntityBean
{
    String KEY_SHOOTER = "shooter";

    /**
     * この投射物を撃ったエンティティを取得します。
     *
     * @return 撃ったエンティティ
     */
    EntityBean getShooter();
}
