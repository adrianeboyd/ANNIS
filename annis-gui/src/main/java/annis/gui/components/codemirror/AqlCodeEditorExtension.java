/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.components.codemirror;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.AbstractJavaScriptExtension;
import com.vaadin.ui.TextArea;

/**
 * A Codemirror2 based code editor for AQL.
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
@JavaScript(
{
  "lib/codemirror.js", "mode/properties/properties.js", "mode/aql/aql.js", "AqlCodeEditorExtension.js"
})
@StyleSheet(
{
  "lib/codemirror.css", "AqlCodeEditor.css"
})
public class AqlCodeEditorExtension extends AbstractJavaScriptExtension
{

  public AqlCodeEditorExtension()
  {
  }

  public void extend(TextArea target)
  {
    super.extend(target);
    target.addTextChangeListener(new FieldEvents.TextChangeListener()
    {

      @Override
      public void textChange(FieldEvents.TextChangeEvent event)
      {
        boolean b = false;
      }
    });
    target.addValueChangeListener(new Property.ValueChangeListener()
    {

      @Override
      public void valueChange(Property.ValueChangeEvent event)
      {
        callFunction("updateText", event.getProperty().getValue());
      }
    });
  }
}
