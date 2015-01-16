/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
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

import annis.gui.components.ExceptionDialog;
import annis.libgui.Helper;
import annis.model.AqlParseError;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * A code editor component for the ANNIQ Query Language.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@JavaScript(
  {
    "vaadin://jquery.js",
    "lib/codemirror.js",
    "mode/aql/aql.js",
    "lib/edit/matchbrackets.js",
    "lib/lint/lint.js",
    "lib/display/placeholder.js",
    "AqlCodeEditor.js"
  })
@StyleSheet(
  {
    "lib/codemirror.css",
    "lib/lint/lint.css",
    "AqlCodeEditor.css"
  })
public class AqlCodeEditor extends AbstractJavaScriptComponent
  implements FieldEvents.TextChangeNotifier, Property.Viewer, Property.ValueChangeListener
{

  private int timeout;
  private Property<String> dataSource;

  public AqlCodeEditor()
  {
    addFunction("textChanged", new TextChangedFunction());
    addStyleName("aql-code-editor");
    
    AqlCodeEditor.this.setPropertyDataSource(new ObjectProperty("", String.class));
  }

  @Override
  public void setPropertyDataSource(Property newDataSource)
  {
    if(newDataSource == null)
    {
      throw new IllegalArgumentException("Data source must not be null");
    }
    
    if(this.dataSource instanceof Property.ValueChangeNotifier)
    {
      ((Property.ValueChangeNotifier) this.dataSource).removeValueChangeListener(this);
    }
        
    this.dataSource = newDataSource;
   
    if (newDataSource instanceof Property.ValueChangeNotifier)
    {
      ((Property.ValueChangeNotifier) this.dataSource).
        addValueChangeListener(this);
    }

  }

  @Override
  public Property getPropertyDataSource()
  {
    return this.dataSource;
  }

  @Override
  public void valueChange(Property.ValueChangeEvent event)
  {
    String oldText = getState().text;
    getState().text = this.dataSource.getValue();
    if(oldText == null || !oldText.equals(getState().text))
    {
      markAsDirty();
    }
  }

  private class TextChangedFunction implements JavaScriptFunction
  {

    @Override
    public void call(JSONArray args) throws JSONException
    {
      getState().text = args.getString(0);
      getPropertyDataSource().setValue(args.getString(0));
      getState().clientText = getState().text;
      
      validate(dataSource.getValue());
      final String textCopy = dataSource.getValue();
      final int cursorPos = args.getInt(1);
      fireEvent(new FieldEvents.TextChangeEvent(AqlCodeEditor.this)
      {

        @Override
        public String getText()
        {
          return textCopy;
        }

        @Override
        public int getCursorPosition()
        {
          return cursorPos;
        }
      });
    }
  }

  private void validate(String query)
  {
    setErrors(null);
    if(query == null || query.isEmpty())
    {
      // don't validate the empty query
      return;
    }
    try
    {
      AsyncWebResource annisResource = Helper.getAnnisAsyncWebResource();
      Future<String> future = annisResource.path("query").path("check").
        queryParam("q", query)
        .get(String.class);

      // wait for maximal one seconds
      try
      {
        String result = future.get(1, TimeUnit.SECONDS);

        if (!"ok".equalsIgnoreCase(result))
        {
          AqlParseError testError = new AqlParseError();
          testError.startLine = 0;
          testError.endLine = 0;
          testError.startColumn = 0;
          testError.endColumn = 4;
          testError.message = result;
          setErrors(Lists.newArrayList(testError));
        }
      }
      catch (InterruptedException ex)
      {
      }
      catch (ExecutionException ex)
      {
        if (ex.getCause() instanceof UniformInterfaceException)
        {
          UniformInterfaceException cause = (UniformInterfaceException) ex.
            getCause();
          if (cause.getResponse().getStatus() == 400)
          {
            List<AqlParseError> errorsFromServer = 
                cause.getResponse().getEntity(new GenericType<List<AqlParseError>>() {});
            
            setErrors(errorsFromServer);
          }
        }
      }
      catch (TimeoutException ex)
      {
      }
    }
    catch (ClientHandlerException ex)
    {
    }
  }

  public void setInputPrompt(String prompt)
  {
    getState().inputPrompt = prompt;
    markAsDirty();
  }

  public void setTextChangeTimeout(int timeout)
  {
    callFunction("setChangeDelayTime", timeout);
    this.timeout = timeout;
  }

  public int getTextChangeTimeout()
  {
    return this.timeout;
  }

  @Override
  public void addTextChangeListener(FieldEvents.TextChangeListener listener)
  {
    addListener(FieldEvents.TextChangeListener.EVENT_ID,
      FieldEvents.TextChangeEvent.class,
      listener, FieldEvents.TextChangeListener.EVENT_METHOD);
  }

  @Override
  public void addListener(FieldEvents.TextChangeListener listener)
  {
    addTextChangeListener(listener);
  }

  @Override
  public void removeTextChangeListener(FieldEvents.TextChangeListener listener)
  {
    removeListener(FieldEvents.TextChangeListener.EVENT_ID,
      FieldEvents.TextChangeEvent.class,
      listener);
  }

  @Override
  public void removeListener(FieldEvents.TextChangeListener listener)
  {
    removeTextChangeListener(listener);
  }

  public String getValue()
  {
    return dataSource.getValue();
  }

  public void setValue(String value)
  {
    dataSource.setValue(value);
  }

  @Override
  protected AqlCodeEditorState getState()
  {
    return (AqlCodeEditorState) super.getState();
  }

  public void setErrors(List<AqlParseError> errors)
  {
    if (errors == null)
    {
      getState().errors = new LinkedList<>();
    }
    else
    {
      getState().errors = new ArrayList<>(errors);
    }
    markAsDirty();
  }

}
