package org.kunlab.scenamatica.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum MinecraftVersion
{
    V1_13("1.13"),
    V1_13_1("1.13.1"),
    V1_13_2("1.13.2"),
    V1_14("1.14"),
    V1_14_1("1.14.1"),
    V1_14_2("1.14.2"),
    V1_14_3("1.14.3"),
    V1_14_4("1.14.4"),
    V1_15("1.15"),
    V1_15_1("1.15.1"),
    V1_15_2("1.15.2"),
    V1_16("1.16"),
    V1_16_1("1.16.1"),
    V1_16_2("1.16.2"),
    V1_16_3("1.16.3"),
    V1_16_4("1.16.4"),
    V1_16_5("1.16.5"),
    V1_17("1.17"),
    V1_17_1("1.17.1"),
    V1_18("1.18"),
    V1_18_1("1.18.1"),
    V1_18_2("1.18.2"),
    V1_19("1.19"),
    V1_19_1("1.19.1"),
    V1_19_2("1.19.2"),
    V1_19_3("1.19.3"),
    V1_19_4("1.19.4"),
    V1_20("1.20"),
    V1_20_1("1.20.1"),
    V1_20_2("1.20.2"),
    V1_20_3("1.20.3"),
    V1_20_4("1.20.4");

    private static final MinecraftVersion CURRENT;

    static
    {
        String serverVersion = Bukkit.getServer().getBukkitVersion();

        CURRENT = Arrays.stream(values())
                // 逆順にする
                .sorted((v1, v2) -> -v1.getVersion().compareTo(v2.getVersion()))
                .filter(v -> serverVersion.contains(v.getVersion()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unsupported Minecraft version: " + serverVersion));
    }

    private final String version;

    public static MinecraftVersion fromString(String version)
    {
        for (MinecraftVersion v : values())
        {
            if (v.getVersion().equals(version))
                return v;
        }
        return null;
    }

    public static MinecraftVersion current()
    {
        return CURRENT;
    }

    public boolean isInRange(MinecraftVersion min, MinecraftVersion max)
    {
        return this.compareTo(min) >= 0 && this.compareTo(max) <= 0;
    }

    public boolean isAtLeast(MinecraftVersion minecraftVersion)
    {
        return this.compareTo(minecraftVersion) >= 0;
    }

    public boolean isAtMost(MinecraftVersion minecraftVersion)
    {
        return this.compareTo(minecraftVersion) <= 0;
    }
}
