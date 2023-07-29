package org.kunlab.scenamatica.reporter.packets;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
public class PacketScenamaticaError extends AbstractRawPacket
{
    private static final String GENRE = "general";
    private static final String TYPE = "error";

    private static final String KEY_EXCEPTION = "exception";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_STACK_TRACE = "stackTrace";
    private static final String KEY_CAUSED_BY = "causedBy";

    @NotNull
    String exception;
    @NotNull
    String message;
    @NotNull
    String[] stackTrace;
    @Nullable
    PacketScenamaticaError causedBy;

    public PacketScenamaticaError(@NotNull Throwable throwable)
    {
        super(GENRE, TYPE);

        this.exception = throwable.getClass().getName();
        this.message = throwable.getMessage();

        this.stackTrace = Arrays.stream(throwable.getStackTrace())
                .map(StackTraceElement::toString)
                .toArray(String[]::new);

        if (throwable.getCause() != null)
            this.causedBy = new PacketScenamaticaError(throwable.getCause());
        else
            this.causedBy = null;
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_EXCEPTION, this.exception);
        result.put(KEY_MESSAGE, this.message);
        result.put(KEY_STACK_TRACE, this.stackTrace);
        if (this.causedBy != null)
            result.put(KEY_CAUSED_BY, this.causedBy.serialize());
        else
            result.put(KEY_CAUSED_BY, null);

        return result;
    }
}
