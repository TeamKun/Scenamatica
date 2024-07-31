package org.kunlab.scenamatica.bookkeeper.utils;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.ScenamaticaClassLoader;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;

public class ClassAnalyser
{

    @NotNull
    public static List<ClassNode> getSuperClasses(ScenamaticaClassLoader classLoader, ClassNode node)
    {
        List<ClassNode> superClasses = new ArrayList<>();
        if (!canTraceSuper(node))
            return superClasses;

        ClassNode superClass = classLoader.getClassByName(node.superName);
        while (superClass != null)
        {
            superClasses.add(superClass);
            if (canTraceSuper(superClass))
                superClass = classLoader.getClassByName(superClass.superName);
            else
                break;
        }
        return superClasses;
    }

    private static boolean canTraceSuper(ClassNode node)
    {
        return !(node.superName == null || node.superName.equals("java/lang/Object") || node.superName.equals("java/lang/Enum"));
    }
}
