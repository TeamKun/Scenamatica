package org.kunlab.scenamatica.reporter.packets;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    String exception;
    @NotNull
    String message;
    @NotNull
    String[] stackTrace;

    public PacketScenamaticaError(@NotNull Throwable throwable)
    {
        super(GENRE, TYPE);

        this.exception = throwable.getClass().getName();
        this.message = throwable.getMessage();

        this.stackTrace = Arrays.stream(throwable.getStackTrace())
                .map(StackTraceElement::toString)
                .toArray(String[]::new);
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> result = super.serialize();

        result.put(KEY_EXCEPTION, this.exception);
        result.put(KEY_MESSAGE, this.message);
        result.put(KEY_STACK_TRACE, this.stackTrace);

        return result;
    }
}
