package org.kunlab.scenamatica.scenariofile.structures;

import lombok.Value;
import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.jetbrains.annotations.NotNull;
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
    public static VersionRange deserialize(Map<String, Object> map)
    {
        Version since = null;
        Version until = null;

        if (map.containsKey(KEY_SINCE))
            since = Version.of((String) map.get(KEY_SINCE));
        if (map.containsKey(KEY_UNTIL))
            until = Version.of((String) map.get(KEY_UNTIL));

        return new VersionRangeImpl(since, until);
    }

    public static void validate(Map<String, Object> map)
    {
        if (map.containsKey(KEY_SINCE))
        {
            if (!(map.get(KEY_SINCE) instanceof String))
                throw new IllegalArgumentException("since must be a string");
            if (!Version.isValidVersionString((String) map.get(KEY_SINCE)))
                throw new IllegalArgumentException("since must be a valid version string");
        }

        if (map.containsKey(KEY_UNTIL))
        {
            if (!(map.get(KEY_UNTIL) instanceof String))
                throw new IllegalArgumentException("until must be a string");
            if (!Version.isValidVersionString((String) map.get(KEY_UNTIL)))
                throw new IllegalArgumentException("until must be a valid version string");
        }
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
