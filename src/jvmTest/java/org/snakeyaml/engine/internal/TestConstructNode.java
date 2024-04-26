package org.snakeyaml.engine.internal;

import org.jetbrains.annotations.NotNull;
import it.krzeminski.snakeyaml.engine.kmp.api.ConstructNode;
import it.krzeminski.snakeyaml.engine.kmp.exceptions.YamlEngineException;
import it.krzeminski.snakeyaml.engine.kmp.nodes.Node;

public abstract class TestConstructNode implements ConstructNode {

  @Override
  public void constructRecursive(@NotNull Node node, @NotNull Object object) {
    if (node.isRecursive()) {
      throw new IllegalStateException("Not implemented in class " + getClass().getName());
    }
    throw new YamlEngineException("Unexpected recursive structure for Node " + node);
  }
}
