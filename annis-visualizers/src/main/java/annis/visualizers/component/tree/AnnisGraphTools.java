/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.visualizers.component.tree;

import annis.CommonHelper;
import annis.libgui.Helper;
import annis.libgui.visualizers.VisualizerInput;
import annis.model.AnnisNode;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnnisGraphTools implements Serializable
{

  private static final String PRIMEDGE_SUBTYPE = "edge";
  private static final String SECEDGE_SUBTYPE = "secedge";
  private final VisualizerInput input;

  public AnnisGraphTools(VisualizerInput input)
  {
      this.input = input;
  }

  public List<DirectedGraph<AnnisNode, Edge>> getSyntaxGraphs()
  {
    AnnotationGraph ag = input.getResult().getGraph();
    String namespace = input.getMappings().getProperty("node_ns", input.
      getNamespace());
    List<DirectedGraph<AnnisNode, Edge>> resultGraphs =
      new ArrayList<DirectedGraph<AnnisNode, Edge>>();

    for (AnnisNode n : ag.getNodes())
    {
      if (isRootNode(n, namespace))
      {
        resultGraphs.add(extractGraph(ag, n));
      }
    }
    return resultGraphs;
  }

  private boolean copyNode(DirectedGraph<AnnisNode, Edge> graph, AnnisNode n)
  {
    boolean addToGraph = n.isToken();
    for (Edge e : n.getOutgoingEdges())
    {
      if (includeEdge(e) && copyNode(graph, e.getDestination()))
      {
        addToGraph |= true;
        graph.addEdge(e, n, e.getDestination());
      }
    }
    if (addToGraph)
    {
      graph.addVertex(n);
    }
    return addToGraph;
  }

  private boolean isRootNode(AnnisNode n, String namespace)
  {
    if (!n.getNamespace().equals(namespace))
    {
      return false;
    }
    for (Edge e : n.getIncomingEdges())
    {
      if (hasEdgeSubtype(e, getPrimEdgeSubType()) && e.getSource()
        != null)
      {
        return false;
      }
    }
    return true;
  }

  private DirectedGraph<AnnisNode, Edge> extractGraph(AnnotationGraph ag,
    AnnisNode n)
  {
    DirectedGraph<AnnisNode, Edge> graph =
      new DirectedSparseGraph<AnnisNode, Edge>();
    copyNode(graph, n);
    for (Edge e : ag.getEdges())
    {
      if (hasEdgeSubtype(e, getSecEdgeSubType()) && graph.
        containsVertex(e.getDestination())
        && graph.containsVertex(e.getSource()))
      {
        graph.addEdge(e, e.getSource(), e.getDestination());
      }
    }
    return graph;
  }

  private boolean includeEdge(Edge e)
  {
    return hasEdgeSubtype(e, getPrimEdgeSubType());
  }

  public boolean hasEdgeSubtype(Edge e, String edgeSubtype)
  {
    String name = e.getName();

    if (getPrimEdgeSubType().equals(edgeSubtype))
    {
      edgeSubtype = input.getMappings().getProperty("edge") != null
        ? input.getMappings().getProperty("edge") : getPrimEdgeSubType();
    }

    if (getSecEdgeSubType().equals(edgeSubtype))
    {
      edgeSubtype = input.getMappings().getProperty("secedge") != null
        ? input.getMappings().getProperty("secedge") : getSecEdgeSubType();
    }

    return e.getEdgeType() == Edge.EdgeType.DOMINANCE && name != null && name.
      equals(edgeSubtype);
  }

  public static HorizontalOrientation detectLayoutDirection(AnnotationGraph ag)
  {
    if(Helper.isRTLDisabled())
    {
      return HorizontalOrientation.LEFT_TO_RIGHT;
    }
    
    int withhRTL = 0;
    for (AnnisNode token : ag.getTokens())
    {
      if (CommonHelper.containsRTLText(token.getSpannedText()))
      {
        withhRTL += 1;
      }
    }
    return (withhRTL > ag.getTokens().size() / 3)
      ? HorizontalOrientation.RIGHT_TO_LEFT
      : HorizontalOrientation.LEFT_TO_RIGHT;
  }

  /**
   * Gets the edge type and takes into account the user defined mappings.
   *
   * @return the name of the edge type. Is never null.
   */
  public String getPrimEdgeSubType()
  {
    return input.getMappings().getProperty("edge_type", PRIMEDGE_SUBTYPE);
  }

  /**
   * Gets the secedge type and takes into account the user defined mappings.
   *
   * @return the name of the secedge type. Is never null.
   */
  public String getSecEdgeSubType()
  {
    return input.getMappings().getProperty("secedge_type", SECEDGE_SUBTYPE);
  }
}