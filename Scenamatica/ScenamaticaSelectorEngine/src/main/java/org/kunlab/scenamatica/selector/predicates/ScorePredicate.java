package org.kunlab.scenamatica.selector.predicates;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.selector.compiler.RangedNumber;

import java.util.HashMap;
import java.util.Map;

public class ScorePredicate extends AbstractGeneralEntitySelectorPredicate
{
    public static final String KEY_SCORE = "score";

    @Override
    public boolean test(Player basis, Entity entity, Map<? super String, Object> properties)
    {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        String scoreName;
        if (entity instanceof OfflinePlayer)
            scoreName = entity.getName();
        else
            scoreName = entity.getUniqueId().toString();

        for (Map.Entry<String, Object> entry : MapUtils.checkAndCastMap(properties.get(KEY_SCORE)).entrySet())
        {
            String key = entry.getKey();
            RangedNumber score = (RangedNumber) entry.getValue();

            Objective objective = scoreboard.getObjective(key);
            if (objective == null)
                throw new IllegalArgumentException("Objective " + key + " is not found");

            if (!score.test(objective.getScore(scoreName).getScore()))
                return false;
        }

        return true;
    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {
        Map<String, Object> scoreStruct = MapUtils.checkAndCastMap(properties.get(KEY_SCORE));
        Map<String, Object> normalizedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : scoreStruct.entrySet())
        {
            String key = entry.getKey();
            normalizedMap.put(key, RangedNumber.fromMap(key, scoreStruct));
        }

        properties.put(KEY_SCORE, normalizedMap);
    }

    @Override
    public String[] getUsingKeys()
    {
        return new String[]{
                KEY_SCORE
        };
    }
}
