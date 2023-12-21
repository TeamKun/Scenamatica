package org.kunlab.scenamatica.selector;

import lombok.Value;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.selector.compiler.SelectorCompilationErrorException;
import org.kunlab.scenamatica.selector.compiler.SelectorCompiler;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

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
        return this.type.enumerate(basis, this.predicate);
    }

    public List<Entity> select()
    {
        return this.select(null);
    }

    public Optional<Entity> selectOne(@Nullable Player basis)
    {
        return this.type.enumerate(basis, this.predicate).stream().findFirst();
    }

    public <T extends Entity> Optional<T> selectOne(@Nullable Player basis, Class<? extends T> clazz)
    {
        Entity entity = this.selectOne(basis).orElse(null);
        if (clazz.isInstance(entity))
            return Optional.of(clazz.cast(entity));
        else
            return Optional.empty();
    }

    @Override
    public String toString()
    {
        return this.original;
    }
}
