package org.kunlab.scenamatica.nms;

import net.minecraft.server.v1_15_R1.EnumHand;
import net.minecraft.server.v1_15_R1.EnumMoveType;
import net.minecraft.server.v1_15_R1.PacketPlayInUseEntity;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;
import org.kunlab.scenamatica.nms.enums.entity.NMSMoveType;
import org.kunlab.scenamatica.nms.impl.v1_15_R1.TypeSupportImpl;

import java.util.EnumMap;
import java.util.Map;

public class TypeSupportTest
{
    private static <T extends NMSElement, U> void testConvertingNMSTypes(T mine, U nms, Class<? extends T> clazz, Class<U> nmsClazz)
    {
        TypeSupport typeSupport = new TypeSupportImpl();
        Object nmsConverted = typeSupport.toNMS(mine, nmsClazz);
        assert nmsConverted.equals(nms);

        T mineConverted = typeSupport.fromNMS(nms, clazz);
        assert mineConverted == mine;
    }

    @Test
    public void testConvertingNMSEntityUseAction()
    {
        EnumMap<NMSEntityUseAction, PacketPlayInUseEntity.EnumEntityUseAction> map = new EnumMap<>(NMSEntityUseAction.class);
        map.put(NMSEntityUseAction.INTERACT, PacketPlayInUseEntity.EnumEntityUseAction.INTERACT);
        map.put(NMSEntityUseAction.ATTACK, PacketPlayInUseEntity.EnumEntityUseAction.ATTACK);
        map.put(NMSEntityUseAction.INTERACT_AT, PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT);

        for (Map.Entry<NMSEntityUseAction, PacketPlayInUseEntity.EnumEntityUseAction> entry : map.entrySet())
            testConvertingNMSTypes(entry.getKey(), entry.getValue(),
                    NMSEntityUseAction.class, PacketPlayInUseEntity.EnumEntityUseAction.class
            );
    }

    @Test
    public void testConvertingNMSMoveType()
    {
        EnumMap<NMSMoveType, EnumMoveType> map = new EnumMap<>(NMSMoveType.class);
        map.put(NMSMoveType.SELF, EnumMoveType.SELF);
        map.put(NMSMoveType.PLAYER, EnumMoveType.PLAYER);
        map.put(NMSMoveType.PISTON, EnumMoveType.PISTON);
        map.put(NMSMoveType.SHULKER_BOX, EnumMoveType.SHULKER_BOX);
        map.put(NMSMoveType.SHULKER, EnumMoveType.SHULKER);

        for (Map.Entry<NMSMoveType, EnumMoveType> entry : map.entrySet())
            testConvertingNMSTypes(entry.getKey(), entry.getValue(),
                    NMSMoveType.class, EnumMoveType.class
            );
    }

    @Test
    public void testConvertingNMSHand()
    {
        testConvertingNMSTypes(NMSHand.MAIN_HAND, EnumHand.MAIN_HAND, NMSHand.class, EnumHand.class);
        testConvertingNMSTypes(NMSHand.OFF_HAND, EnumHand.OFF_HAND, NMSHand.class, EnumHand.class);
    }
}
