package org.kunlab.scenamatica.commons.specifiers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerSpecifierImpl extends EntitySpecifierImpl<Player> implements PlayerSpecifier
{
    public static final PlayerSpecifier EMPTY = new PlayerSpecifierImpl(null);

    public PlayerSpecifierImpl(@Nullable Object mayTarget)
    {
        super(mayTarget);

        if (!(this.targetStructure == null || this.targetStructure instanceof PlayerStructure))
            throw new IllegalArgumentException("Target must be Player");
    }

    @NotNull
    public static PlayerSpecifier tryDeserializePlayer(
            @Nullable Object obj,
            @NotNull StructureSerializer serializer
    )
    {
        if (obj == null)
            return EMPTY;

        if (obj instanceof String || obj instanceof PlayerStructure)
            return new PlayerSpecifierImpl(obj);

        if (obj instanceof Map)
        {
            // noinspection unchecked
            Map<String, Object> map = (Map<String, Object>) obj;

            return new PlayerSpecifierImpl(serializer.deserialize(map, PlayerStructure.class));
        }

        throw new IllegalArgumentException("Cannot deserialize PlayerSpecifier from " + obj);
    }

    @Override
    public PlayerStructure getTargetStructure()
    {
        return (PlayerStructure) super.getTargetStructure();
    }

    @Override
    public Player selectTarget(@NotNull Context context)
    {
        // Player も一応 Entity なので、親クラスのメソッドを呼び出す。
        Entity entity = super.selectTargetRaw(context);

        if (!(entity instanceof Player))
            return null;

        Player player = (Player) entity;

        // プレイヤとしての追加のマッチングを行う。
        if (!(this.targetStructure == null || this.getTargetStructure().isAdequate(player)))
            return null;

        return player;
    }

    @Override
    protected List<? extends Player> selectEntities(String specifier)
    {
        return super.selectEntities(specifier).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.toList());
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
        return super.checkMatchedEntity(player);
    }
}
