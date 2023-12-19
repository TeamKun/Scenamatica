package org.kunlab.scenamatica.selector.predicates;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.NamespaceUtils;
import org.kunlab.scenamatica.selector.compiler.NegateSupport;

import java.util.HashMap;
import java.util.Map;

public class AdvancementsPredicate extends AbstractPlayerSelectorPredicate
{
    public static final String KEY_ADVANCEMENTS = "advancements";

    private static boolean isAchieved(Player player, Advancement adv, Object pred)
    {
        AdvancementProgress progress = player.getAdvancementProgress(adv);
        if (pred instanceof Boolean)
            return progress.isDone() == (Boolean) pred;

        assert pred instanceof Map/*<String, Boolean>*/;

        Map<String, Boolean> criterion = MapUtils.checkAndCastMap(pred, String.class, Boolean.class);
        for (Map.Entry<String, Boolean> entry : criterion.entrySet())
        {
            String criterionName = entry.getKey();
            boolean criterionValue = entry.getValue();

            if (progress.getAwardedCriteria().contains(criterionName) != criterionValue)
                return false;
        }

        return true;
    }

    private static Object normalizeAdvancementCriterion(Object mayCriterion)
    {
        if (NegateSupport.isNegative(mayCriterion))
            throw new IllegalArgumentException("advancement criterion cannot be negated");
        else if (mayCriterion instanceof Boolean)
            return mayCriterion;
        else if (!(mayCriterion instanceof Map))
            throw new IllegalArgumentException("advancement criterion must be like {<criterion name>: <true/false>}");

        Map<String, Boolean> newCriterion = new HashMap<>();
        Map<String, Object> criterionMap = MapUtils.checkAndCastMap(mayCriterion);
        for (Map.Entry<String, Object> entry : criterionMap.entrySet())
        {
            String criterionName = entry.getKey();
            Object mayCriterionValue = entry.getValue();

            if (NegateSupport.isNegative(mayCriterionValue))
                throw new IllegalArgumentException("advancement criterion cannot be negated");
            else if (!(mayCriterionValue instanceof Boolean))
                throw new IllegalArgumentException("advancement criterion must be like {<criterion name>: <true/false>}");

            newCriterion.put(criterionName, (Boolean) mayCriterionValue);
        }

        return newCriterion;
    }

    private static Advancement getAdvancementOrThrow(String advancementName)
    {
        Advancement advancement = Bukkit.getAdvancement(NamespaceUtils.fromString(advancementName));
        if (advancement == null)
            throw new IllegalArgumentException("advancement " + advancementName + " does not exist");
        return advancement;
    }

    @Override
    public boolean test(Player basis, Player entity, Map<? super String, Object> properties)
    {
        Map<Advancement, Object> advancements = MapUtils.checkAndCastMap(
                properties.get(KEY_ADVANCEMENTS),
                Advancement.class,
                Object.class
        );
        for (Map.Entry<Advancement, Object> entry : advancements.entrySet())
        {
            Advancement advancement = entry.getKey();
            Object mayCriterion = entry.getValue();

            if (!isAchieved(entity, advancement, mayCriterion))
                return false;
        }

        return true;
    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {
        Object advancements = properties.get(KEY_ADVANCEMENTS);
        if (NegateSupport.isNegative(advancements))
            throw new IllegalArgumentException("advancements properties cannot be negated");
        else if (!(advancements instanceof Map))
            throw new IllegalArgumentException("advancements property must be like {<advancement name>: <true/false | {<criterion name>: <true/false>}>}");

        Map<Advancement, Object> newAdvancements = new HashMap<>();
        Map<String, Object> advancementsMap = MapUtils.checkAndCastMap(advancements);
        for (Map.Entry<String, Object> entry : advancementsMap.entrySet())
        {
            String advancementName = entry.getKey();
            Object mayCriterion = entry.getValue();

            Advancement advancement = getAdvancementOrThrow(advancementName);
            // Boolean か, {<criterion name>: <true/false>} をコンパイル
            Object criterion = normalizeAdvancementCriterion(mayCriterion);

            newAdvancements.put(advancement, criterion);
        }

        properties.put(KEY_ADVANCEMENTS, newAdvancements);
    }

    @Override
    public String[] getUsingKeys()
    {
        return new String[]{
                KEY_ADVANCEMENTS
        };
    }
}
