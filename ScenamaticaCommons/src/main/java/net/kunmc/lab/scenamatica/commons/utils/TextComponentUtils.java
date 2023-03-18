package net.kunmc.lab.scenamatica.commons.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;

import java.util.List;

public class TextComponentUtils
{
    public static String toColoredText(Component component)
    {
        StringBuilder builder = new StringBuilder();
        appendColoredText(builder, component);

        List<Component> children = component.children();
        for (Component child : children)
            appendColoredText(builder, child);

        return builder.toString();
    }

    private static void appendColoredText(StringBuilder builder, Component component)
    {
        Style style = component.style();
        TextColor color = style.color();
        if (color instanceof NamedTextColor)
            builder.append(color);
        if (style.hasDecoration(TextDecoration.BOLD))
            builder.append(ChatColor.BOLD);
        if (style.hasDecoration(TextDecoration.ITALIC))
            builder.append(ChatColor.ITALIC);
        if (style.hasDecoration(TextDecoration.UNDERLINED))
            builder.append(ChatColor.UNDERLINE);
        if (style.hasDecoration(TextDecoration.STRIKETHROUGH))
            builder.append(ChatColor.STRIKETHROUGH);
        if (style.hasDecoration(TextDecoration.OBFUSCATED))
            builder.append(ChatColor.MAGIC);
        builder.append(((TextComponent) component).content());
    }
}
