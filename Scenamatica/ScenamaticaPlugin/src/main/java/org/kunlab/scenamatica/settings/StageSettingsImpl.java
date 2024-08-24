package org.kunlab.scenamatica.settings;

import lombok.Builder;
import lombok.Value;
import org.bukkit.configuration.file.FileConfiguration;

@Value
@Builder
public class StageSettingsImpl implements StageSettings
{
    @Builder.Default
    boolean usePatchedWorldGeneration = true;

    public static StageSettings fromConfig(FileConfiguration configuration)
    {
        boolean usePatchedWorldGeneration = configuration.getBoolean("stage.usePatchedWorldGeneration");

        return new StageSettingsImpl(
                usePatchedWorldGeneration
        );
    }

    @Override
    public boolean usePatchedWorldGeneration()
    {
        return usePatchedWorldGeneration;
    }
}
