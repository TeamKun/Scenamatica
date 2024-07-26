package org.kunlab.scenamatica.bookkeeper.compiler;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.BookkeeperCore;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.IReference;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SerializingContext
{
    private static final MessageDigest DIGEST;
    private final Map<? super String, Object> references;
    @Getter
    private final BookkeeperCore core;
    @Getter
    private final boolean isJSONSchema;
    private final Map<String, Object> sessionData;

    static
    {
        try
        {
            DIGEST = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public SerializingContext(@NotNull Map<? super String, Object> references, BookkeeperCore core, boolean isJSONSchema)
    {
        this.references = references;
        this.core = core;
        this.isJSONSchema = isJSONSchema;
        this.sessionData = new HashMap<>();
    }

    public SerializingContext(@NotNull BookkeeperCore core)
    {
        this.references = null;
        this.core = core;
        this.isJSONSchema = false;
        this.sessionData = new HashMap<>();
    }

    public String createReference(IReference<?> type)
    {
        if (!this.isJSONSchema)
            throw new IllegalStateException("Cannot create references in non-JSON schema context");
        assert this.references != null;

        String referenceHash = sha256(type.getReference());
        this.references.putIfAbsent(referenceHash, type.getResolved().serialize(this));
        return "#/definitions/" + referenceHash;
    }

    public String createReference(@NotNull Map<? super String, Object> target, @NotNull IReference<?> ref)
    {
        String createdRef = this.createReference(ref);
        target.put("$ref", createdRef);
        return createdRef;
    }

    public String createReference(@NotNull Map<? super String, Object> target, @NotNull Object object)
    {
        if (!this.isJSONSchema)
            throw new IllegalStateException("Cannot create references in non-JSON schema context");
        assert this.references != null;

        int hash = Objects.hashCode(object);
        this.references.putIfAbsent(String.valueOf(hash), object);
        String createdRef = "#/definitions/" + hash;
        target.put("$ref", createdRef);
        return createdRef;
    }

    public void putSessionData(String key, Object value)
    {
        this.sessionData.put(key, value);
    }

    public Object getSessionData(String key)
    {
        return this.sessionData.get(key);
    }

    public boolean hasSessionData(String key)
    {
        return this.sessionData.containsKey(key);
    }

    public void removeSessionData(String key)
    {
        this.sessionData.remove(key);
    }

    private static String sha256(String base)
    {
        byte[] hash = DIGEST.digest(base.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash)
        {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

