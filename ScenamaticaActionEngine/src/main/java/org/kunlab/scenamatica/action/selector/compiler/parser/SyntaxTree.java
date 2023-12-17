package org.kunlab.scenamatica.action.selector.compiler.parser;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Value
@AllArgsConstructor
public class SyntaxTree
{
    @NotNull
    SyntaxType type;
    @Nullable
    String value;
    SyntaxTree[] children;

    public SyntaxTree(@NotNull SyntaxType type, @Nullable String value)
    {
        this(type, value, null);
    }

    public SyntaxTree(@NotNull SyntaxType type, @Nullable SyntaxTree[] children)
    {
        this(type, null, children);
    }

    public SyntaxTree[] getChildren()
    {
        return this.children == null ? new SyntaxTree[0]: this.children;
    }

    public SyntaxTree getChild(SyntaxType type)
    {
        if (this.children == null)
            return null;

        for (SyntaxTree child : this.children)
            if (child.type == type)
                return child;
        return null;
    }

    public SyntaxTree[] getChildren(SyntaxType type)
    {
        if (this.children == null)
            return new SyntaxTree[0];
        List<SyntaxTree> children = new ArrayList<>();
        for (SyntaxTree child : this.children)
            if (child.type == type)
                children.add(child);
        return children.toArray(new SyntaxTree[0]);
    }

    public boolean has(SyntaxType type)
    {
        if (this.children == null)
            return false;

        for (SyntaxTree child : this.children)
            if (child.type == type)
                return true;
        return false;
    }

    public int childrenCount()
    {
        return this.children.length;
    }

    public boolean hasChildren()
    {
        return this.children != null && this.children.length > 0;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(this.type);
        if (this.value != null)
            builder.append(": ").append(this.value);
        if (this.children != null && this.children.length > 0)
        {
            builder.append(" [");
            for (SyntaxTree child : this.children)
                builder.append("\n\t").append(child.toString().replaceAll("\n", "\n\t"));
            builder.append("\n]");
        }
        return builder.toString();
    }
}
