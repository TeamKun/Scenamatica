package org.kunlab.scenamatica.structures;

import lombok.Getter;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Tag;

@Getter
public class ObjectNode extends Node
{
    private final Class<?> clazz;
    private final Object object;

    public ObjectNode(Class<?> clazz, Object value)
    {
        super(Tag.MAP, null, null);

        this.clazz = clazz;
        this.object = value;
    }

    @Override
    public NodeId getNodeId()
    {
        return NodeId.scalar;
    }

    public <T> boolean isType(Class<? extends T> clazz)
    {
        return clazz.isInstance(this.object);
    }
}
