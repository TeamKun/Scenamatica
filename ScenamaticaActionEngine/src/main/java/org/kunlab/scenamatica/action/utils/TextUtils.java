package org.kunlab.scenamatica.action.utils;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.TextComponent;

@UtilityClass
public class TextUtils
{
    public static boolean isSameContent(TextComponent bungeeComponent, String content)
    {
        return (bungeeComponent.toPlainText().equals(content) || bungeeComponent.toLegacyText().equals(content));
    }

    public static boolean isSameContent(net.kyori.adventure.text.Component adventureComponent, String content)
    {
        net.kyori.adventure.text.TextComponent component;
        if (adventureComponent instanceof net.kyori.adventure.text.TextComponent)
            component = (net.kyori.adventure.text.TextComponent) adventureComponent;
        else
            component = net.kyori.adventure.text.TextComponent.ofChildren(adventureComponent);

        return component.content().equals(content);
    }
}
