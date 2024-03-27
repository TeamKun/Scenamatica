package org.kunlab.scenamatica;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.kunlab.scenamatica.interfaces.ExceptionHandler;
import org.kunlab.scenamatica.reporter.packets.PacketScenamaticaError;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

@AllArgsConstructor
public class SimpleExceptionHandler implements ExceptionHandler
{
    private final Logger logger;
    private final boolean isRaw;

    @Override
    public void report(Throwable e)
    {
        if (!this.isRaw)
        {
            this.logger.log(Level.WARNING, "An unexpected exception occurred while operating Scenamatica daemon.", e);
            return;
        }

        Gson gson = new Gson();
        PrintStream out = new PrintStream(new FileOutputStream(FileDescriptor.out));

        out.println(gson.toJson(new PacketScenamaticaError(e)));
    }
}
