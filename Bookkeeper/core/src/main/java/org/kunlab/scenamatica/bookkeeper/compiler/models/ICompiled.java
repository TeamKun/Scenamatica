package org.kunlab.scenamatica.bookkeeper.compiler.models;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.compiler.SerializingContext;

import java.util.Map;

public interface ICompiled
{
    Map<String, Object> serialize(@NotNull SerializingContext ctxt);
}
