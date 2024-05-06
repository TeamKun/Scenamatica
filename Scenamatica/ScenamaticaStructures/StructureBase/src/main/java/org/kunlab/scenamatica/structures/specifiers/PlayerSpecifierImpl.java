package org.kunlab.scenamatica.structures.specifiers;

import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.selector.Selector;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
public class PlayerSpecifierImpl extends EntitySpecifierImpl<Player> implements PlayerSpecifier
{
    public static final PlayerSpecifier EMPTY = new PlayerSpecifierImpl();

    private final String mayName;

    private PlayerSpecifierImpl(@Nullable UUID mayUUID, @Nullable Selector selector, @Nullable EntityStructure targetStructure, @Nullable String mayName)
    {
        super(mayUUID, selector, targetStructure);
        this.mayName = mayName;

        if (!(this.targetStructure == null || this.targetStructure instanceof PlayerStructure))
            throw new IllegalArgumentException("Target must be Player");
    }

    private PlayerSpecifierImpl(@Nullable Selector selector, @Nullable String mayName)
    {
        this(null, selector, null, mayName);
    }

    private PlayerSpecifierImpl(@NotNull EntityStructure targetStructure)
    {
        this(null, null, targetStructure, null);
    }

    private PlayerSpecifierImpl(@NotNull UUID mayUUID)
    {
        this(mayUUID, null, null, null);
    }

    private PlayerSpecifierImpl()
    {
        this(null, null, null, null);
    }

    @NotNull
    public static PlayerSpecifier tryDeserializePlayer(
            @Nullable Object obj,
            @NotNull StructureSerializer serializer
    )
    {
        if (obj == null)
            return EMPTY;

        if (obj instanceof Selector)
            return new PlayerSpecifierImpl((Selector) obj, null);
        else if (obj instanceof EntityStructure)
            return new PlayerSpecifierImpl((EntityStructure) obj);
        else if (obj instanceof UUID)
            return new PlayerSpecifierImpl((UUID) obj);
        else if (obj instanceof Map)
        {
            // noinspection unchecked
            Map<String, Object> map = (Map<String, Object>) obj;

            return new PlayerSpecifierImpl(serializer.deserialize(map, PlayerStructure.class));
        }
        else if (obj instanceof String)
        {
            UUID mayUUID = tryConvertToUUID((String) obj);

            if (mayUUID == null)
                return new PlayerSpecifierImpl(
                        Selector.tryCompile((String) obj)
                                .orElse(null),
                        (String) obj
                );
            else
                return new PlayerSpecifierImpl(mayUUID);
        }


        throw new IllegalArgumentException("Cannot deserialize PlayerSpecifier from " + obj);
    }

    public static PlayerSpecifier ofPlayer(@Nullable Player player)
    {
        if (player == null)
            return EMPTY;
        else
            return new PlayerSpecifierImpl(player.getUniqueId());
    }

    public static PlayerSpecifier ofPlayer(@Nullable UUID uuid)
    {
        if (uuid == null)
            return EMPTY;
        else
            return new PlayerSpecifierImpl(uuid);
    }

    @Override
    public boolean canProvideTarget()
    {
        return super.canProvideTarget() || this.mayName != null;
    }

    @Override
    public PlayerStructure getTargetStructure()
    {
        return (PlayerStructure) super.getTargetStructure();
    }

    @Override
    public Optional<Player> selectTarget(@Nullable Context context)
    {
        // Player も一応 Entity なので、親クラスのメソッドを呼び出す。
        Entity entity = super.selectTargetRaw(null, context);
        if (entity == null)
        {
            if (context != null)
            {
                List<Player> actors = context.getActors().stream()
                        .map(Actor::getPlayer)
                        .collect(Collectors.toList());
                for (Player actor : actors)
                    if (this.checkMatchedPlayer(actor))
                    {
                        entity = actor;
                        break;
                    }
            }
            else if (this.mayName != null)
                entity = Bukkit.getPlayer(this.mayName);
        }

        if (!(entity instanceof Player))
            return Optional.empty();

        Player player = (Player) entity;

        // プレイヤとしての追加のマッチングを行う。
        if (!(this.targetStructure == null || this.getTargetStructure().isAdequate(player)))
            return Optional.empty();

        return Optional.of(player);
    }

    @Override
    protected boolean isAdequate(EntityStructure structure, @NotNull Entity actualEntity)
    {
        if (!(structure instanceof PlayerStructure))
            return false;

        return ((PlayerStructure) structure).isAdequate((Player) actualEntity);
    }

    @Override
    public boolean checkMatchedPlayer(Player player)
    {
        return super.checkMatchedEntity(player)
                || (this.mayName != null && player.getName().equals(this.mayName));
    }

    @Override
    public boolean hasName()
    {
        return this.mayName != null;
    }

    @Override
    public String getName()
    {
        return this.mayName;
    }
}
