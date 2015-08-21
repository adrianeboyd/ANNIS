/*
 * Copyright 2015 SFB 632.
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
package annis.visualizers.component.archdependency;

import annis.libgui.VisualizationToggle;
import annis.libgui.visualizers.AbstractVisualizer;
import annis.libgui.visualizers.VisualizerInput;
import com.vaadin.ui.Component;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@PluginImplementation
public class ArchDependencyVisualizer extends AbstractVisualizer<Component>
{

  @Override
  public String getShortName()
  {
    return "arch_dependency";
  }

  @Override
  public Component createComponent(VisualizerInput visInput,
    VisualizationToggle visToggle)
  {
    VakyarthaDependencyTree component = new VakyarthaDependencyTree();
    
    return component;
  }
  
}
