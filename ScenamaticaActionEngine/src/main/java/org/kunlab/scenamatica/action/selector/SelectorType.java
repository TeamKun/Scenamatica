package org.kunlab.scenamatica.action.selector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum SelectorType
{
    PLAYER_ALL("a", Bukkit::getOnlinePlayers),
    PLAYER_RANDOM("r", () -> {
        List<? extends Entity> entities = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(entities);
        return entities;
    }),
    PLAYER_NEAREST("p", Bukkit::getOnlinePlayers),
    PLAYER_SELF("s", Bukkit::getOnlinePlayers),
    ENTITY_ALL("e", () -> Bukkit.getWorlds().stream()
            .flatMap(world -> world.getEntities().stream()).collect(Collectors.toList())
    );

    private final String type;
    private final Supplier<? extends Collection<? extends Entity>> entitiesSupplier;

    public static SelectorType of(String type)
    {
        for (SelectorType selectorType : SelectorType.values())
            if (selectorType.getType().equalsIgnoreCase(type))
                return selectorType;

        throw new IllegalArgumentException("Unknown selector type: " + type);
    }

    public List<Entity> enumerate(@Nullable Player basis, @NotNull BiPredicate<? super Player, ? extends Entity> predicate)
    {
        switch (this)
        {
            case PLAYER_SELF:
                if (basis == null || !predicate.test(basis, basis))
                    return Collections.emptyList();
                else
                    return Collections.singletonList(basis);
            case PLAYER_NEAREST:
                if (basis == null)
                    return Collections.emptyList();
                // noinspection unchecked, rawtypes
                return this.entitiesSupplier.get().stream()
                        .filter(entity -> entity instanceof Player)
                        .filter(entity -> ((BiPredicate) predicate).test(basis, entity))
                        .sorted((o1, o2) -> {
                            double d1 = o1.getLocation().distanceSquared(basis.getLocation());
                            double d2 = o2.getLocation().distanceSquared(basis.getLocation());
                            return Double.compare(d1, d2);
                        })
                        .collect(Collectors.toList());
            default:
                return new ArrayList<>(this.entitiesSupplier.get()).stream()
                        .filter(entity -> entity instanceof Player)
                        .collect(Collectors.toList());

        }
    }
}
