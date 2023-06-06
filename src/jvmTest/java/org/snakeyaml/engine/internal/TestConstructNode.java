package org.snakeyaml.engine.internal;

import org.jetbrains.annotations.NotNull;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.Node;

public abstract class TestConstructNode implements ConstructNode {

  @Override
  public void constructRecursive(@NotNull Node node, @NotNull Object object) {
    if (node.isRecursive()) {
      throw new IllegalStateException("Not implemented in class " + getClass().getName());
    }
    throw new YamlEngineException("Unexpected recursive structure for Node " + node);
  }
}
