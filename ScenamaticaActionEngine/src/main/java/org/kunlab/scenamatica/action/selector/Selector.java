package org.kunlab.scenamatica.action.selector;

import lombok.Value;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.selector.compiler.SelectorCompiler;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Value
@NotNull
public class Selector
{
    String original;
    SelectorType type;
    Predicate<? super Entity> predicate;

    public static Selector compile(String selector)
    {
        return SelectorCompiler.compile(selector);
    }

    public List<Entity> select(@Nullable Player basis)
    {
        return this.type.enumerate(basis, this.predicate);
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
        return "Selector{'" + this.original + "'}";
    }
}
