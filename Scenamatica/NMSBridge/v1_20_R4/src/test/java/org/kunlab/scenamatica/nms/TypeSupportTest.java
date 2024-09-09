package org.kunlab.scenamatica.nms;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSMoveType;
import org.kunlab.scenamatica.nms.impl.v1_20_R4.TypeSupportImpl;

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
        EnumMap<NMSMoveType, MoverType> map = new EnumMap<>(NMSMoveType.class);
        map.put(NMSMoveType.SELF, MoverType.SELF);
        map.put(NMSMoveType.PLAYER, MoverType.PLAYER);
        map.put(NMSMoveType.PISTON, MoverType.PISTON);
        map.put(NMSMoveType.SHULKER_BOX, MoverType.SHULKER_BOX);
        map.put(NMSMoveType.SHULKER, MoverType.SHULKER);

        for (Map.Entry<NMSMoveType, MoverType> entry : map.entrySet())
            testConvertingNMSTypes(entry.getKey(), entry.getValue(),
                    NMSMoveType.class, MoverType.class
            );
    }

    @Test
    public void testConvertingNMSHand()
    {
        testConvertingNMSTypes(NMSHand.MAIN_HAND, InteractionHand.MAIN_HAND, NMSHand.class, InteractionHand.class);
        testConvertingNMSTypes(NMSHand.OFF_HAND, InteractionHand.OFF_HAND, NMSHand.class, InteractionHand.class);
    }
}
