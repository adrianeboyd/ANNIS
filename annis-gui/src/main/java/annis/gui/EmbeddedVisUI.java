/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui;

import annis.gui.docbrowser.DocBrowserController;
import annis.libgui.visualizers.VisualizerInput;
import annis.service.objects.Visualizer;
import annis.visualizers.htmlvis.HTMLVis;
import com.google.common.base.Splitter;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class EmbeddedVisUI extends CommonUI
{
  private static final Logger log = LoggerFactory.getLogger(EmbeddedVisUI.class);
  
  public static final String PREFIX = "/embeddedvis";
  

  @Override
  protected void init(VaadinRequest request)
  {
    super.init(request);
    
    String rawPath = request.getPathInfo();
    List<String> splittedPath = new LinkedList<>();
    if(rawPath != null)
    {
      rawPath = rawPath.substring(PREFIX.length());
      splittedPath = Splitter.on("/").omitEmptyStrings().trimResults().limit(
        3).splitToList(rawPath);
    }
    
    if(splittedPath.size() >= 3)
    {
      if("htmldoc".equals(splittedPath.get(0)))
      {
        showHtmlDoc(splittedPath.get(1), splittedPath.get(2), request.getParameterMap());
      }
      else
      {
        setContent(new Label("unknown visualizer \"" + splittedPath.get(0) + "\", only \"htmldoc\" is supported yet."));
      }
    }
    else
    {
      displayGeneralHelp();
    }
  }
  
  private void displayGeneralHelp()
  {
    setContent(new Label("<h1>Path not complete</h1>"
      + "<p>"
      + "You have to specify what visualizer to use and which document of which corpus you want to visualizer by giving the correct path:<br />"
      + "<code>http://example.com/annis/embeddedvis/&lt;vis&gt;/&lt;corpus&gt;/&lt;doc&gt;</code>"
      + "<ul>"
      + "<li><code>vis</code>: visualizer name (currently only \"htmldoc\" is supported)</li>"
      + "<li><code>corpus</code>: corpus name</li>"
      + "<li><code>doc</code>: name of the document to visualize</li>"
      + "</ul>"
      + "</p>",
      ContentMode.HTML));
  }
  
  
  private void showHtmlDoc(String corpus, String doc, Map<String, String[]> args)
  {
    // do nothing for empty fragments
    if (args == null || args.isEmpty() )
    {
      return;
    }

    if (args.get("config") != null && args.get("config").length > 0)
    {
      String config = args.get("config")[0];

      // get input parameters
      HTMLVis visualizer;
      visualizer = new HTMLVis();

      VisualizerInput input;
      Visualizer visConfig;
      visConfig = new Visualizer();
      visConfig.setDisplayName(" ");
      visConfig.setMappings("config:" + config);
      visConfig.setNamespace(null);
      visConfig.setType("htmldoc");

      //create input
      try
      {
        input = DocBrowserController.createInput(corpus, doc, visConfig, false, null);
        //create components, put in a panel
        Panel viszr = visualizer.createComponent(input, null);

        // Set the panel as the content of the UI
        setContent(viszr);
      }
      catch(UniformInterfaceException ex)
      {
        setContent(new Label("Could not query document, error was \"" 
          + ex.getMessage() + "\" (detailed error is available in the server log-files "));
        log.error("Could not get document for embedded visualizer", ex);
      }

    }
    else
    {
      setContent(new Label("<h1>Missing required argument for visualizer \"htmldoc\"</h1>"
        + "<p>"
        + "The following arguments are required:"
        + "<ul>"
        + "<li><code>config</code>: the internal config file to use (same as <a href=\"http://korpling.github.io/ANNIS/doc/classannis_1_1visualizers_1_1htmlvis_1_1HTMLVis.html\">\"config\" mapping parameter)</a></li>"
        + "</ul>"
        + "</p>", 
        ContentMode.HTML ));
    }
  }
  
}
