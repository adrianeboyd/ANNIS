/*
 * Copyright 2016 SFB 632.
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
package de.hu_berlin.german.korpling.annis.kickstarter;

import annis.service.internal.AnnisServiceRunner;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author thomas
 */
public class KickstartRunner
{
  
  private AnnisServiceRunner runner;
  private int oldTimeout;

  
  protected void resetRunner()
  {
    this.runner = null;
  }
  
  public void startService() throws Exception
  {
    // starts RMI service at bean creation
    runner = new AnnisServiceRunner();
    runner.setUseAuthentification(false);
    runner.start(true);
  }

  public void startJetty() throws Exception
  {
    // disable jetty logging
    Log.setLog(new JettyNoLogger());
    Server jetty = new Server(8080);
    // add context for our bundled webapp
    String annisHome = System.getProperty("annis.home");
    if(annisHome == null || annisHome.isEmpty())
    {
      annisHome = ".";
    }
    WebAppContext context = new WebAppContext(annisHome +  "/webapp/", "/annis-gui");
    context.setInitParameter("managerClassName",
      "annis.security.TestSecurityManager");
    String webxmlOverrride = annisHome + "/conf/override-web.xml"; //ClassLoader.getSystemResource("webxmloverride.xml").toString();
    List<String> listWebXMLOverride = new LinkedList<String>();
    listWebXMLOverride.add(webxmlOverrride);
    context.setOverrideDescriptors(listWebXMLOverride);
    // Exclude some jersey classes explicitly from the web application classpath.
    // If they still exists some automatic dependency resolution of Jersey will
    // fail.
    // Whenever we add new dependencies on jersey classes for the service but
    // not for the GUI and "Missing dependency" errors occur, add the classes
    // to the server class list
    context.addServerClass("com.sun.jersey.json.");
    context.addServerClass("com.sun.jersey.server.");
    jetty.setHandler(context);
    // start
    jetty.start();
  }

  public void setTimeoutDisabled(boolean disabled)
  {
    if(runner != null)
    {
      if(disabled)
      {
        // unset timeout
        oldTimeout = runner.getTimeout();
        runner.setTimeout(-1);
      }
      else
      {
        // restore timeout
        runner.setTimeout(oldTimeout);
      }
    }
  }
  
  
}
