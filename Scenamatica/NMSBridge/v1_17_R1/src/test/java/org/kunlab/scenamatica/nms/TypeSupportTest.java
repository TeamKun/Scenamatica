package org.kunlab.scenamatica.nms;

import net.minecraft.world.EnumHand;
import net.minecraft.world.entity.EnumMoveType;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;
import org.kunlab.scenamatica.nms.enums.entity.NMSMoveType;
import org.kunlab.scenamatica.nms.impl.v1_17_R1.TypeSupportImpl;

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
    public void testConvertingNMSMoveType()
    {
        EnumMap<NMSMoveType, EnumMoveType> map = new EnumMap<>(NMSMoveType.class);
        map.put(NMSMoveType.SELF, EnumMoveType.a);
        map.put(NMSMoveType.PLAYER, EnumMoveType.b);
        map.put(NMSMoveType.PISTON, EnumMoveType.c);
        map.put(NMSMoveType.SHULKER_BOX, EnumMoveType.d);
        map.put(NMSMoveType.SHULKER, EnumMoveType.e);

        for (Map.Entry<NMSMoveType, EnumMoveType> entry : map.entrySet())
            testConvertingNMSTypes(entry.getKey(), entry.getValue(),
                    NMSMoveType.class, EnumMoveType.class
            );
    }

    @Test
    public void testConvertingNMSHand()
    {
        testConvertingNMSTypes(NMSHand.MAIN_HAND, EnumHand.a, NMSHand.class, EnumHand.class);
        testConvertingNMSTypes(NMSHand.OFF_HAND, EnumHand.b, NMSHand.class, EnumHand.class);
    }
}
