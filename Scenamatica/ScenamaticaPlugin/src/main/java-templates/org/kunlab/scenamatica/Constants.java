package org.kunlab.scenamatica;

public class Constants
{
    @SuppressWarnings("ConstantValue")  // Maven による置換で値が埋め込まれる
    public static final boolean DEBUG_BUILD = "${project.debug}".equals("true");
}
