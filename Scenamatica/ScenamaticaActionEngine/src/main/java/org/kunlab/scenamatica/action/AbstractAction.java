package org.kunlab.scenamatica.action;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.input.InputBoardImpl;
import org.kunlab.scenamatica.action.input.InputTokenImpl;
import org.kunlab.scenamatica.action.input.TraverserImpl;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.action.utils.PlayerLikeCommandSenders;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.InputTraverser;
import org.kunlab.scenamatica.interfaces.action.input.Traverser;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractAction implements Action
{
    @SuppressWarnings("unchecked")
    private static final Traverser<?, PlayerSpecifier>[] PLAYER_SPECIFIER_TRAVERSERS = Arrays.asList(
            TraverserImpl.of(
                    InputTypeToken.ofMap(String.class, Object.class),
                    StructureSerializer::tryDeserializePlayerSpecifier
            ),
            TraverserImpl.of(
                    PlayerStructure.class,
                    (ser, ps) -> ser.tryDeserializePlayerSpecifier(ps.getUuid())
            ),
            TraverserImpl.of(
                    CommandSender.class,
                    (ser, ps) -> {
                        if (ps instanceof Player)
                            return ser.tryDeserializePlayerSpecifier(ps);
                        else return ser.tryDeserializePlayerSpecifier(PlayerLikeCommandSenders.CONSOLE_SENDER);
                    }
            ),
            TraverserImpl.of(
                    String.class,
                    StructureSerializer::tryDeserializePlayerSpecifier
            )
    ).toArray(new Traverser[0]);

    protected static <T> InputToken<T> ofInput(@NotNull String name, @NotNull Class<T> clazz)
    {
        return InputTokenImpl.of(name, clazz);
    }

    protected static <T> InputToken<T> ofInput(@NotNull String name, @NotNull Class<T> clazz, @NotNull Traverser<?, T> traverser, @NotNull T defaultValue)
    {
        return InputTokenImpl.of(name, clazz, traverser, defaultValue);
    }

    @SafeVarargs
    protected static <T> InputToken<T> ofInput(@NotNull String name, @NotNull Class<T> clazz, @NotNull Traverser<?, T>... traversers)
    {
        return InputTokenImpl.of(name, clazz, traversers);
    }

    protected static <T> InputToken<T> ofInput(@NotNull String name, @NotNull Class<T> clazz, @NotNull T defaultValue)
    {
        return InputTokenImpl.of(name, clazz, defaultValue);
    }

    protected static <T extends Enum<T>> InputToken<T> ofEnumInput(@NotNull String name, @NotNull Class<T> clazz)
    {
        return InputTokenImpl.of(name, clazz, ofEnum(clazz));
    }

    protected static <I, O> Traverser<I, O> ofTraverser(@NotNull Class<? extends I> inputClazz, @NotNull InputTraverser<? super I, ? extends O> traverser)
    {
        return TraverserImpl.of(inputClazz, traverser);
    }

    protected static <I extends Map<String, Object>, O extends Structure> Traverser<I, O> ofDeserializer(@NotNull Class<? extends O> clazz)
    {
        // noinspection unchecked,rawtypes
        return (Traverser) TraverserImpl.of(Map.class, (ser, map) -> ser.deserialize(
                MapUtils.checkAndCastMap(map),
                clazz
        ));
    }

    protected static <O extends EntityStructure> Traverser<Object, EntitySpecifier<Entity>> ofSpecifier(@NotNull Class<? extends O> clazz)
    {
        return TraverserImpl.of(Object.class, (ser, obj) -> ser.tryDeserializeEntitySpecifier(
                obj,
                clazz
        ));
    }

    protected static Traverser<?, PlayerSpecifier>[] ofPlayer()
    {
        return PLAYER_SPECIFIER_TRAVERSERS;
    }

    protected static <T extends Enum<T>> Traverser<String, T> ofEnum(@NotNull Class<T> clazz)
    {
        return TraverserImpl.of(String.class, (ser, str) -> Enum.valueOf(clazz, str.toUpperCase()));
    }

    protected static InputBoard ofInputs(@NotNull ScenarioType type, InputToken<?>... tokens)
    {
        return new InputBoardImpl(type, tokens);
    }

    protected static <T> Predicate<T> and(Predicate<? super T> p1, Predicate<? super T> p2)
    {
        return t -> p1.test(t) && p2.test(t);
    }

    protected static <T extends Entity> InputToken<EntitySpecifier<T>> ofInput(String name, Class<T> entityClass, Class<? extends EntityStructure> structureClass)
    {
        return ofInput(
                name,
                InputTypeToken.ofEntity(entityClass),
                ofTraverser(
                        Map.class,
                        (ser, map) -> ser.tryDeserializeEntitySpecifier(map, structureClass)
                ),
                ofTraverser(
                        EntityStructure.class,
                        (ser, structure) -> ser.tryDeserializeEntitySpecifier(structure.getUuid(), structureClass)
                ),
                ofTraverser(
                        String.class,
                        (ser, str) -> ser.tryDeserializeEntitySpecifier(str, structureClass)
                )
        );
    }

    protected static InputToken<EntitySpecifier<Entity>> ofSpecifier(String name)
    {
        return ofInput(name, Entity.class, EntityStructure.class);
    }
}
