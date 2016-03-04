package annis.visualizers.component.visjs;


import static annis.visualizers.component.grid.GridComponent.MAPPING_ANNOS_KEY;
import static annis.visualizers.component.grid.GridComponent.MAPPING_ANNO_REGEX_KEY;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import annis.libgui.visualizers.VisualizerInput;
import annis.visualizers.component.grid.EventExtractor;
import annis.visualizers.component.kwic.KWICVisualizer;

import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SDominanceRelation;
import org.corpus_tools.salt.common.SPointingRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.ExportFilter;
import org.corpus_tools.salt.util.VisJsVisualizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.server.Scrollable;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.AbstractJavaScriptComponent;





@JavaScript(
		  {"VisJs_Connector.js",
		    "vaadin://jquery.js",
		    "vis.min.js"	    
		  })
@StyleSheet(
  {
	  	"vis.min.css"
  })



public class VisJsComponent extends AbstractJavaScriptComponent implements ExportFilter{	
	
	private String strNodes;
	private String strEdges;
	public static final String MAPPING_EDGES = "edges";
	
	// a HashMap for storage of filter annotations with associated namespaces	
	private Map<String, Set<String>> filterNodeAnnotations = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> filterEdgeAnnotations = new HashMap<String, Set<String>>();
	
	private static String nodeAnnosConfiguration;
	private static String nodeAnnosRegexConfiguration;
	private static String edgeAnnosConfiguration;
	
	
	
	
	private static final Logger log = LoggerFactory.getLogger(VisJsComponent.class);
	
	public VisJsComponent(VisualizerInput visInput){
			nodeAnnosConfiguration = visInput.getMappings().getProperty(MAPPING_ANNOS_KEY);
			nodeAnnosRegexConfiguration = visInput.getMappings().getProperty(MAPPING_ANNO_REGEX_KEY);
			edgeAnnosConfiguration = visInput.getMappings().getProperty(MAPPING_EDGES);
			fillFilterAnnotations(visInput, 0);
			fillFilterAnnotations(visInput, 1);
			
			SDocument doc =  visInput.getDocument();
			
			VisJsVisualizer VisJsVisualizer = new VisJsVisualizer(doc, this);
			 
			OutputStream osNodes = new ByteArrayOutputStream();
			OutputStream osEdges = new ByteArrayOutputStream();
			
			
			VisJsVisualizer.setNodeWriter(osNodes);
			VisJsVisualizer.setEdgeWriter(osEdges);
			VisJsVisualizer.buildJSON();				
			BufferedWriter bw;
			try {
				bw = VisJsVisualizer.getNodeWriter();
				bw.flush();	
				
				bw = VisJsVisualizer.getEdgeWriter();	
				bw.flush();		
		
				
				strNodes = osNodes.toString();
				strEdges = osEdges.toString();
				
				bw.close();			
		       
	      
			}catch(IOException e){
				System.out.println(e.getStackTrace());
			}     
		
	
	}
	
	private void fillFilterAnnotations(VisualizerInput visInput, int type){
		Map<String, Set<String>> myFilterAnnotations;
		List<String> annotations;
		if(type == 0) {
			annotations = EventExtractor.computeDisplayAnnotations(visInput, SNode.class);	
			myFilterAnnotations = filterNodeAnnotations;
		}
		else{
			annotations = computeDisplayedEdgeAnnotations(visInput, SRelation.class);	
			myFilterAnnotations = filterEdgeAnnotations;
		}
		
			
			for(String annotation: annotations)
			{ 	String anno = null;
				String ns = null;
				Set<String> namespaces = null;
			
				if (annotation.contains("::"))
				{
					String [] annotationParts = annotation.split("::");
					if (annotationParts.length == 2)
					{
						anno = annotationParts[1];
						ns = annotationParts[0];
						
					}
					else
					{
						//TODO Error?
					}
					
				}
				else
				{
				anno = annotation;	
				}
				
				
				if (myFilterAnnotations.containsKey(anno))
				{
					 namespaces = myFilterAnnotations.get(anno);
				}
				else
				{
					namespaces = new HashSet<String>();
				}
				
				
				if (ns != null)
				{
					namespaces.add(ns);
				}
				
				myFilterAnnotations.put(anno, namespaces);
				
			}
	}

		  
	  
	    @Override
	    protected VisJsState getState() {
	        return (VisJsState) super.getState();
	    }
	
	    @Override
	    public void attach() {
	      super.attach();
	      //set an initial size
	      //it will be adjusted to the size of panel by VisJs_Connector.js
	      setHeight("400px");
	      setWidth("1000px");
	      getState().strNodes = strNodes;
	      getState().strEdges = strEdges;
	     
	    }


		
		
		
		/**
		   * Returns the annotations to display according to the mappings configuration.
		   *
		   * This will check the "edge" parameter for determining the annotations to display. 
		   * It also iterates over all nodes of the graph
		   * matching the type.
		   *
		   * @param input The input for the visualizer.
		   * @param type Which type of relations to include
		   * @return
		   */
		  public static List<String> computeDisplayedEdgeAnnotations(VisualizerInput input,  Class<? extends SRelation> type) {
		    if (input == null) {
		      return new LinkedList<>();
		    }

		    SDocumentGraph graph = input.getDocument().getDocumentGraph();

		    Set<String> annotationPool = getEdgeLevelSet(graph, null, type);
		    List<String> confAnnotations = new LinkedList<>(annotationPool);
		  
		    
		    if (edgeAnnosConfiguration != null && edgeAnnosConfiguration.trim().length() > 0) {
		      String[] confSplit = edgeAnnosConfiguration.split(",");
		      confAnnotations.clear();
		      for (String entry : confSplit) {
		        entry = entry.trim();
		        // is regular expression?
		        if (entry.startsWith("/") && entry.endsWith("/")) {
		          // go over all remaining items in our pool of all annotations and
		          // check if they match
		          Pattern regex = Pattern.compile(StringUtils.strip(entry, "/"));

		          LinkedList<String> matchingAnnotations = new LinkedList<>();
		          for (String anno : annotationPool) {
		            if (regex.matcher(anno).matches()) {
		              matchingAnnotations.add(anno);
		            
		            }
		          }
		          confAnnotations.addAll(matchingAnnotations);
		          annotationPool.removeAll(matchingAnnotations);

		        } else {
		        	confAnnotations.add(entry);
		        	annotationPool.remove(entry);
		        }
		      }
		    }		    
		    return confAnnotations;
		  }

		
		
		
		
		 /**
		   * Get the qualified name of all annotations belonging to relations having a
		   * specific namespace.
		   *
		   * @param graph The graph.
		   * @param namespace The namespace of the relation (not the annotation) to search
		   * for.
		   * @param type Which type of relation to include
		   * @return
		   *
		   */
		
		// TODO test
		  public static Set<String> getEdgeLevelSet(SDocumentGraph graph,
		          String namespace, Class<? extends SRelation> type) {
		    Set<String> result = new TreeSet<>();
		    

		    if (graph != null) {
		      List<? extends SRelation> edges = null;
		      // catch most common cases directly
		      if (type == SDominanceRelation.class) {
		        edges = graph.getDominanceRelations();
		      } else if (type == SPointingRelation.class){
		    	  edges = graph.getPointingRelations();
		      } else if (type == SSpanningRelation.class){
		    	  edges = graph.getSpanningRelations();
		      }
		      else {
		    	  edges = graph.getRelations();
		      }
		      
		      
		      if (edges != null) {
		        for (SRelation<?, ?> edge : edges) {
		        	  Set <SLayer> layers = edge.getLayers();
		            for (SLayer layer : layers) {
		              if (namespace == null || namespace.equals(layer.getName())) {
		                for (SAnnotation anno : edge.getAnnotations()) {
		                  result.add(anno.getQName());
		                }
		                // we got all annotations of this edge, jump to next edge
		                break;
		              } // end if namespace equals layer name
		            } // end for each layer
		        } // end for each node
		      }
		    }

		    return result;
		  }
		  
	  @Override
		public boolean excludeNode(SNode node) {
			// if node is a token or no configuration set, output the node
			if (node instanceof SToken || (nodeAnnosConfiguration == null && nodeAnnosRegexConfiguration == null))
			{
				return false;
			}
					
			Set<SAnnotation> nodeAnnotations =  node.getAnnotations();
			
			for (SAnnotation nodeAnnotation : nodeAnnotations)
			{
				String nodeAnno = nodeAnnotation.getName();
				String nodeNs = nodeAnnotation.getNamespace();
				
				if (filterNodeAnnotations.containsKey(nodeAnno))
				{
					if (filterNodeAnnotations.get(nodeAnno).isEmpty())
					{
						return false;
					}
					else if (filterNodeAnnotations.get(nodeAnno).contains(nodeNs))
					{
						return false;
					}
				}
				
			}
			
			return true;
		}
		


		@Override
		public boolean excludeRelation(SRelation relation) {
			// if no configuration set, output the relation
			if (edgeAnnosConfiguration == null){
				return false;
			}
			
			Set<SAnnotation> edgeAnnotations =  relation.getAnnotations();
			
			for (SAnnotation edgeAnnotation : edgeAnnotations)
			{
				String edgeAnno = edgeAnnotation.getName();
				String edgeNs = edgeAnnotation.getNamespace();
				
				if (filterEdgeAnnotations.containsKey(edgeAnno))
				{
					if (filterEdgeAnnotations.get(edgeAnno).isEmpty())
					{
						return false;
					}
					else if (filterEdgeAnnotations.get(edgeAnno).contains(edgeNs))
					{
						return false;
					}
				}
				
			}
			return true;
		}

}
