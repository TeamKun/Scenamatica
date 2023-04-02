package net.kunmc.lab.scenamatica.action.utils;

import net.md_5.bungee.api.chat.TextComponent;

public class TextUtil
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
