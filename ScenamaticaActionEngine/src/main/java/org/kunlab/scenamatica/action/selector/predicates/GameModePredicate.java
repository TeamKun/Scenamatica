package org.kunlab.scenamatica.action.selector.predicates;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.action.selector.compiler.NegateSupport;
import org.kunlab.scenamatica.action.selector.compiler.parser.NegativeValue;

import java.util.Map;

public class GameModePredicate extends AbstractSelectorPredicate<Player>
{
    public static final String KEY_GAME_MODE = "gamemode";
    public static final String KEY_GAME_MODE_2 = "m";

    @Override
    public boolean test(Player basis, Player entity, Map<? super String, Object> properties)
    {
        Object gameModeRaw = properties.get(KEY_GAME_MODE);
        GameMode mode = NegateSupport.toRawCast(gameModeRaw);
        boolean shouldNegate = NegateSupport.shouldNegate(gameModeRaw);

        return entity.getGameMode() == mode ^ shouldNegate;
    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {
        integrateAlias(properties, KEY_GAME_MODE, KEY_GAME_MODE_2);

        Object mayGameMode = NegateSupport.getRaw(KEY_GAME_MODE, properties);
        boolean doNegate = NegateSupport.shouldNegate(KEY_GAME_MODE, properties);

        GameMode gameMode;
        if (mayGameMode instanceof Number)
        {
            int intGamemode = ((Number) mayGameMode).intValue();
            if (intGamemode < 0 || intGamemode > 3)
                throw new IllegalArgumentException("GameMode must be between 0 and 3");
            // noinspection deprecation
            gameMode = GameMode.getByValue(intGamemode);
        }
        else if (mayGameMode instanceof String)
            gameMode = GameMode.valueOf(((String) mayGameMode).toUpperCase());
        else
            throw new IllegalArgumentException("GameMode must be a number or a string");

        properties.put(KEY_GAME_MODE, doNegate ? new NegativeValue(gameMode): gameMode);
    }

    @Override
    public String[] getUsingKeys()
    {
        return new String[]{
                KEY_GAME_MODE,
                KEY_GAME_MODE_2
        };
    }

    @Override
    public Class<? extends Player> getApplicableClass()
    {
        return Player.class;
    }
}
