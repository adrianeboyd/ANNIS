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

import annis.gui.components.SettingsStorage;
import annis.gui.requesthandler.BinaryRequestHandler;
import annis.gui.requesthandler.LoginServletRequestHandler;
import annis.gui.requesthandler.ResourceRequestHandler;
import annis.gui.servlets.ResourceServlet;
import annis.libgui.AnnisBaseUI;
import com.vaadin.server.VaadinRequest;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.util.uri.ClassURI;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CommonUI extends AnnisBaseUI
{
  //private static final Logger log = LoggerFactory.getLogger(CommonUI.class);
  
  private SettingsStorage settings;
  
  private final String urlPrefix;
  
  public CommonUI(String urlPrefix)
  {
    this.urlPrefix = urlPrefix;
  }
  
  @Override
  protected void init(VaadinRequest request)
  {
    super.init(request);
    
    getSession().addRequestHandler(new LoginServletRequestHandler(urlPrefix));    
    getSession().addRequestHandler(new ResourceRequestHandler(urlPrefix));
    getSession().addRequestHandler(new BinaryRequestHandler(urlPrefix));

    settings = new SettingsStorage(this);
  }

  @Override
  protected void addCustomUIPlugins(PluginManager pluginManager)
  {
    super.addCustomUIPlugins(pluginManager);        
    pluginManager.addPluginsFrom(new ClassURI(ResourceServlet.class).toURI());
  }
  
  
  
  public SettingsStorage getSettings()
  {
    if(settings == null)
    {
      settings = new SettingsStorage(this);
    }
    return settings;
  }
  
  
}
