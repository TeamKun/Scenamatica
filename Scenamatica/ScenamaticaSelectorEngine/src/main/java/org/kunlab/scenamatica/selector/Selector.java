package org.kunlab.scenamatica.selector;

import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.ThreadingUtil;
import org.kunlab.scenamatica.selector.compiler.SelectorCompilationErrorException;
import org.kunlab.scenamatica.selector.compiler.SelectorCompiler;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Value
@NotNull
public class Selector
{
    String original;
    SelectorType type;
    BiPredicate<? super Player, ? extends Entity> predicate;

    @NotNull
    public static Selector compile(@NotNull String selector, boolean canProvideBasis)
    {
        return SelectorCompiler.getInstance().compile(selector, canProvideBasis);
    }

    @NotNull
    public static Selector compile(@NotNull String selector)
    {
        return compile(selector, false);
    }

    public static Optional<Selector> tryCompile(@NotNull String selector, boolean canProvideBasis)
    {
        try
        {
            return Optional.of(compile(selector, canProvideBasis));
        }
        catch (SelectorCompilationErrorException e)
        {
            return Optional.empty();
        }
    }

    public static Optional<Selector> tryCompile(String selector)
    {
        return tryCompile(selector, false);
    }

    public boolean test(@Nullable Player basis, @NotNull Entity entity)
    {
        try
        {
            // noinspection unchecked
            return ((BiPredicate<Player, Entity>) this.predicate).test(basis, entity);
        }
        catch (ClassCastException e)
        {
            return false;
        }
    }

    public List<Entity> select(@Nullable Player basis)
    {
        return this.enumerateSynced(basis);
    }

    public List<Entity> select()
    {
        return this.select((Player) null);
    }

    public Optional<Entity> selectOne(@Nullable Player basis)
    {
        return this.enumerateSynced(basis).stream().findFirst();
    }

    private List<Entity> enumerateSynced(@Nullable Player basis)
    {
        if (Bukkit.isPrimaryThread())
            return this.type.enumerate(basis, this.predicate);
        else
            return ThreadingUtil.waitFor(() -> this.type.enumerate(basis, this.predicate));
    }

    public <T extends Entity> Optional<T> selectOne(@Nullable Player basis, Class<? extends T> clazz)
    {
        Entity entity = this.selectOne(basis).orElse(null);
        if (clazz.isInstance(entity))
            return Optional.of(clazz.cast(entity));
        else
            return Optional.empty();
    }

    public List<Entity> select(@NotNull List<? extends Entity> entities)
    {
        return this.enumerateSynced(null).stream()
                .filter(entities::contains)
                .collect(Collectors.toList());
    }

    @Override
    public String toString()
    {
        return this.original;
    }
}
