package org.kunlab.scenamatica.selector.predicates;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.kunlab.scenamatica.selector.compiler.AmbiguousString;

import java.util.Map;

public class TeamPredicate extends AbstractGeneralEntitySelectorPredicate
{
    public static final String KEY_TEAM = "team";

    @Override
    public boolean test(Player basis, Entity entity, Map<? super String, Object> properties)
    {
        String entryName;
        if (entity instanceof Player)
            entryName = entity.getName();
        else
            entryName = entity.getUniqueId().toString();

        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(entryName);
        AmbiguousString teamString = (AmbiguousString) properties.get(KEY_TEAM);

        if (teamString.isEmpty())
            return team == null && !teamString.doNegate();

        if (team == null)
            return teamString.doNegate();

        return teamString.test(team.getName());
    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {
        AmbiguousString.normalizeMap(KEY_TEAM, properties);
    }

    @Override
    public String[] getUsingAmbiguousStringKeys()
    {
        return new String[]{
                KEY_TEAM
        };
    }

    @Override
    public String[] getUsingKeys()
    {
        return new String[]{
                KEY_TEAM
        };
    }
}
