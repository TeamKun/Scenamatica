package org.kunlab.scenamatica.scenariofile.structures;

import lombok.Value;
import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.scenariofile.VersionRange;

import java.util.HashMap;
import java.util.Map;

@Value
public class VersionRangeImpl implements VersionRange
{
    Version since;
    Version until;

    @NotNull
    public static Map<String, Object> serialize(VersionRange range)
    {
        Map<String, Object> map = new HashMap<>();
        if (range.getSince() != null)
            map.put(KEY_SINCE, range.getSince().toString());
        if (range.getUntil() != null)
            map.put(KEY_UNTIL, range.getUntil().toString());

        return map;
    }

    @NotNull
    public static VersionRange deserialize(StructuredYamlNode node) throws YamlParsingException
    {
        Version since = node.get(KEY_SINCE).getAs(n -> Version.of(n.asString()), null);
        Version until = node.get(KEY_UNTIL).getAs(n -> Version.of(n.asString()), null);

        return new VersionRangeImpl(since, until);
    }

    public static void validate(StructuredYamlNode node) throws YamlParsingException
    {
        node.get(KEY_UNTIL).ensureTypeOfIfExists(YAMLNodeType.STRING);
        node.get(KEY_UNTIL).validateIfExists(n -> {
            if (!Version.isValidVersionString(n.asString()))
                throw new IllegalArgumentException("version string is invalid");
            return null;
        });

        node.get(KEY_UNTIL).ensureTypeOfIfExists(YAMLNodeType.STRING);
        node.get(KEY_UNTIL).validateIfExists(n -> {
            if (!Version.isValidVersionString(n.asString()))
                throw new IllegalArgumentException("version string is invalid");
            return null;
        });
    }

    @Override
    public boolean isInRange(Version version)
    {
        if (this.since == null && this.until == null)
            return true;

        if (this.since == null) /* assert this.until != null */
            return version.isOlderThanOrEqualTo(this.until);
        if (this.until == null) /* assert this.since != null */
            return version.isNewerThanOrEqualTo(this.since);

        return version.isNewerThanOrEqualTo(this.since) && version.isOlderThanOrEqualTo(this.until);
    }

    @Override
    public String toString()
    {
        return (this.since == null ? "": this.since.toString()) + " ~ " + (this.until == null ? "": this.until.toString());
    }
}
