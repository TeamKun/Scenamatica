package org.kunlab.scenamatica.settings;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

@Value
@Builder
public class ActorSettingsImpl implements ActorSettings
{
    @Builder.Default
    long maxActors = 100;
    @Builder.Default
    int defaultOPLevel = 4;
    @Singular
    List<String> defaultPermissions;
    @Singular
    List<String> defaultScoreboardTags;

    @Builder.Default
    String defaultSocketAddress = "127.0.0.1";
    @Builder.Default
    int defaultSocketPort = 1919;

    public static ActorSettings fromConfig(FileConfiguration configuration)
    {
        long maxActors = configuration.getLong("actor.maxActors");
        int defaultOP = configuration.getInt("actor.defaultOP");

        List<String> defaultPermissions = configuration.getStringList("actor.permissions");
        List<String> defaultScoreboardTags = configuration.getStringList("actor.scoreboardTags");

        String defaultSocketAddress = configuration.getString("actor.defaultSocketAddress");
        int defaultSocketPort = configuration.getInt("actor.defaultSocketPort");

        return new ActorSettingsImpl(
                maxActors,
                defaultOP,
                defaultPermissions,
                defaultScoreboardTags,
                defaultSocketAddress,
                defaultSocketPort
        );
    }
}
