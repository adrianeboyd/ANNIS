/*
 * Copyright 2012 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.frequency;

import annis.gui.CorpusSelectionChangeListener;
import annis.gui.QueryController;
import annis.gui.model.PagedResultQuery;
import annis.libgui.Helper;
import annis.model.QueryAnnotation;
import annis.model.QueryNode;
import annis.service.objects.FrequencyTableEntry;
import annis.service.objects.FrequencyTableEntryType;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class FrequencyQueryPanel extends VerticalLayout implements Serializable, FieldEvents.TextChangeListener
{
  private Table tblFrequencyDefinition;
  private final Button btAdd;
  private final Button btReset;
  private final CheckBox cbAutomaticMode;
  private Button btDeleteRow;
  private Button btShowFrequencies;
  private int counter;
  private FrequencyResultPanel resultPanel;
  private Button btShowQuery;
  private VerticalLayout queryLayout;
  private final QueryController controller;
  private final Label lblCorpusList;
  private final Label lblAQL;
  private final Label lblErrorOrMsg;
  
  public FrequencyQueryPanel(final QueryController controller)
  {
    this.controller = controller;
    
    setWidth("99%");
    setHeight("99%");
    setMargin(true);
    
    queryLayout = new VerticalLayout();
    queryLayout.setWidth("100%");
    queryLayout.setHeight("-1px");
    
    HorizontalLayout queryDescriptionLayout = new HorizontalLayout();
    queryDescriptionLayout.setSpacing(true);
    queryDescriptionLayout.setWidth("100%");
    queryDescriptionLayout.setHeight("-1px");
    queryLayout.addComponent(queryDescriptionLayout);
    
    lblCorpusList = new Label("");
    lblCorpusList.setCaption("selected corpora:");
    lblCorpusList.setWidth("100%");
    
    lblAQL = new Label("");
    lblAQL.setCaption("query to analyze:");
    lblAQL.setWidth("100%");
    lblAQL.addStyleName("corpus-font-force");
    
    queryDescriptionLayout.addComponent(lblCorpusList);
    queryDescriptionLayout.addComponent(lblAQL);
    
    queryDescriptionLayout.setComponentAlignment(lblCorpusList,
      Alignment.MIDDLE_LEFT);
    queryDescriptionLayout.setComponentAlignment(lblAQL,
      Alignment.MIDDLE_RIGHT);
    
    tblFrequencyDefinition = new Table();
    tblFrequencyDefinition.setImmediate(true);
    tblFrequencyDefinition.setSortEnabled(false);
    tblFrequencyDefinition.setSelectable(true);
    tblFrequencyDefinition.setMultiSelect(true);
    //tblFrequencyDefinition.setEditable(true);
    tblFrequencyDefinition.addValueChangeListener(new Property.ValueChangeListener() 
    {
      @Override
      public void valueChange(ValueChangeEvent event)
      {
        if(tblFrequencyDefinition.getValue() == null 
          || ((Set<Object>) tblFrequencyDefinition.getValue()).isEmpty())
        {
          btDeleteRow.setEnabled(false);
        }
        else
        {
          btDeleteRow.setEnabled(true);
        }
      }
    });
    
    lblErrorOrMsg = new Label(
      "No node with explicit name in OR expression found! "
      + "When using OR expression you need to explicitly name the nodes "
      + "you want to include in the frequency analysis with \"#\", "
      + "like e.g. in <br />"
      + "<pre>"
      + "(n1#tok=\"fun\" | n1#tok=\"severity\")"
      + "</pre>");
    lblErrorOrMsg.setContentMode(ContentMode.HTML);
    lblErrorOrMsg.addStyleName("warning");
    lblErrorOrMsg.setWidth("100%");
    lblErrorOrMsg.setVisible(false);
    queryLayout.addComponent(lblErrorOrMsg);
    
    tblFrequencyDefinition.setWidth("100%");
    tblFrequencyDefinition.setHeight("-1px");
    
    
    tblFrequencyDefinition.addContainerProperty("nr", TextField.class, null);
    tblFrequencyDefinition.addContainerProperty("annotation", TextField.class, null);
    tblFrequencyDefinition.addContainerProperty("comment", String.class, "manually created");
    
    tblFrequencyDefinition.setColumnHeader("nr", "Node number/name");
    tblFrequencyDefinition.setColumnHeader("annotation", "Selected annotation of node");
    tblFrequencyDefinition.setColumnHeader("comment", "Comment");
    
    tblFrequencyDefinition.addStyleName("corpus-font-force");
    
    tblFrequencyDefinition.setRowHeaderMode(Table.RowHeaderMode.INDEX);
    
    if(controller != null)
    {
      createAutomaticEntriesForQuery(controller.getQueryDraft());
      updateQueryInfo(controller.getQueryDraft());
    }
    
    tblFrequencyDefinition.setColumnExpandRatio("nr", 0.15f);
    tblFrequencyDefinition.setColumnExpandRatio("annotation", 0.35f);
    tblFrequencyDefinition.setColumnExpandRatio("comment", 0.5f);
    
    queryLayout.addComponent(tblFrequencyDefinition);
    
    HorizontalLayout layoutButtons = new HorizontalLayout();
    
    btAdd = new Button("Add");
    btAdd.addClickListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        cbAutomaticMode.setValue(Boolean.FALSE);
        
        int nr = 1;
        // get the highest number of values from the existing defitions
        for(Object id : tblFrequencyDefinition.getItemIds())
        {
          AbstractField textNr = (AbstractField) tblFrequencyDefinition.getItem(id)
            .getItemProperty("nr").getValue();
          try
          {
            nr = Math.max(nr, Integer.parseInt((String) textNr.getValue()));
          }
          catch(NumberFormatException ex)
          {
            // was not a number but a named node
          }
        }
        if(controller != null)
        {
          List<QueryNode> nodes = parseQuery(controller.getQueryDraft());
          nr = Math.min(nr, nodes.size()-1);
          int id = counter++;
          tblFrequencyDefinition.addItem(createNewTableRow(
            tblFrequencyDefinition, id,
            "" +(nr+1),
            FrequencyTableEntryType.span, "", ""), id);
        }
      }
    });
    layoutButtons.addComponent(btAdd);
    
    btDeleteRow = new Button("Delete selected row(s)");
    btDeleteRow.setEnabled(false);
    btDeleteRow.addClickListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        Set<Object> selected = new HashSet((Set<Object>) tblFrequencyDefinition.getValue());
        for(Object o : selected)
        {
          cbAutomaticMode.setValue(Boolean.FALSE);
          tblFrequencyDefinition.removeItem(o);
        }
      }
    });
    layoutButtons.addComponent(btDeleteRow);
    
    cbAutomaticMode = new CheckBox("Automatic mode", true);
    cbAutomaticMode.setImmediate(true);
    cbAutomaticMode.addValueChangeListener(new Property.ValueChangeListener()
    {
      @Override
      public void valueChange(ValueChangeEvent event)
      {
        btShowFrequencies.setEnabled(true);
        if(cbAutomaticMode.getValue())
        {
          tblFrequencyDefinition.removeAllItems();
          if(controller != null)
          {
            createAutomaticEntriesForQuery(controller.getQueryDraft());
          }
        }
      }
    });
    layoutButtons.addComponent(cbAutomaticMode);
    
    btReset = new Button("Reset to default");
    btReset.addClickListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        cbAutomaticMode.setValue(Boolean.TRUE);
        btShowFrequencies.setEnabled(true);
        tblFrequencyDefinition.removeAllItems();
        if(controller != null)
        {
          createAutomaticEntriesForQuery(controller.getQueryDraft());
        }
      }
    });
    //layoutButtons.addComponent(btReset);
    
    layoutButtons.setComponentAlignment(btAdd, Alignment.MIDDLE_LEFT);
    layoutButtons.setComponentAlignment(btDeleteRow, Alignment.MIDDLE_LEFT);
    layoutButtons.setComponentAlignment(cbAutomaticMode, Alignment.MIDDLE_RIGHT);
    layoutButtons.setExpandRatio(btAdd, 0.0f);
    layoutButtons.setExpandRatio(btDeleteRow, 0.0f);
    layoutButtons.setExpandRatio(cbAutomaticMode, 1.0f);
    
    layoutButtons.setMargin(true);
    layoutButtons.setSpacing(true);
    layoutButtons.setHeight("-1px");
    layoutButtons.setWidth("100%");
    
    queryLayout.addComponent(layoutButtons);
    
    btShowFrequencies = new Button("Perform frequency analysis");
    btShowFrequencies.setDisableOnClick(true);
    btShowFrequencies.addClickListener(new Button.ClickListener() 
    {
      @Override
      public void buttonClick(ClickEvent event)
      {
        ArrayList<FrequencyTableEntry> freqDefinition = new ArrayList<>();
        for(Object oid : tblFrequencyDefinition.getItemIds())
        {
          FrequencyTableEntry entry = new FrequencyTableEntry();
          
          Item item = tblFrequencyDefinition.getItem(oid);
          AbstractField textNr = (AbstractField) item.getItemProperty("nr").getValue();
          AbstractField textKey = (AbstractField) item.getItemProperty("annotation").getValue();
          
          entry.setKey((String) textKey.getValue());
          entry.setReferencedNode((String) textNr.getValue());
          if(textKey.getValue() != null && "tok".equals(textKey.getValue()))
          {
            entry.setType(FrequencyTableEntryType.span);
          }
          else
          {
            entry.setType(FrequencyTableEntryType.annotation);
          }
          freqDefinition.add(entry);
        }
        
        if(controller != null)
        {
          controller.setQueryFromUI();
          try
          {
            executeFrequencyQuery(freqDefinition);
          }
          catch(Exception ex)
          {
            btShowFrequencies.setEnabled(true);
          }
        }
          
      }
    });
    queryLayout.addComponent(btShowFrequencies);
    
    queryLayout.setComponentAlignment(tblFrequencyDefinition, Alignment.TOP_CENTER);
    queryLayout.setComponentAlignment(layoutButtons, Alignment.TOP_CENTER);
    queryLayout.setComponentAlignment(btShowFrequencies, Alignment.TOP_CENTER);
    
    queryLayout.setExpandRatio(tblFrequencyDefinition, 1.0f);
    queryLayout.setExpandRatio(layoutButtons, 0.0f);
    queryLayout.setExpandRatio(btShowFrequencies, 0.0f);
    
    btShowQuery = new Button("New Analysis", new Button.ClickListener() 
    {

      @Override
      public void buttonClick(ClickEvent event)
      {
        btShowQuery.setVisible(false);
        queryLayout.setVisible(true);
        resultPanel.setVisible(false);
      }
    });
    btShowQuery.setVisible(false);
    
    addComponent(queryLayout);
    addComponent(btShowQuery);
    
    setComponentAlignment(btShowQuery, Alignment.TOP_CENTER);
   
    if(controller != null)
    {
      controller.addCorpusSelectionChangeListener(new CorpusSelectionChangeListener()
      {

        @Override
        public void onCorpusSelectionChanged(Set<String> selectedCorpora)
        {
          if (cbAutomaticMode.getValue())
          {
            createAutomaticEntriesForQuery(controller.getQueryDraft());
          }
          updateQueryInfo(controller.getQueryDraft());
        }
      });
    }
  }
  
  private Object[] createNewTableRow(
    final Table tbl, final Object rowID,
    String nodeVariable, FrequencyTableEntryType type, 
    String annotation, String comment)
  {
    TextField txtNode = new TextField();
    txtNode.setValue(nodeVariable);
    txtNode.addValidator(new IntegerValidator("Node reference must be a valid number"));
    txtNode.setWidth("100%");
    if(tbl != null && rowID != null)
    {
      txtNode.addFocusListener(new FieldEvents.FocusListener()
      {
        @Override
        public void focus(FieldEvents.FocusEvent event)
        {
          tbl.setValue(null);
          tbl.select(rowID);
        }
      });
    }
    
    final TextField txtAnno = new TextField();
    
    if(type == FrequencyTableEntryType.span)
    {
      txtAnno.setInputPrompt("tok");
      txtAnno.setValue("tok");
    }
    else
    {
      txtAnno.setValue(annotation);
    }
    
    txtAnno.setWidth("100%");
    if(tbl != null && rowID != null)
    {
      txtAnno.addFocusListener(new FieldEvents.FocusListener()
      {
        @Override
        public void focus(FieldEvents.FocusEvent event)
        {
          tbl.setValue(null);
          tbl.select(rowID);
        }
      });
    }
    
    return new Object[] {txtNode, txtAnno, comment == null ? ""  : comment};
  }

  @Override
  public void textChange(FieldEvents.TextChangeEvent event)
  {
    if(cbAutomaticMode.getValue())
    {
      createAutomaticEntriesForQuery(event.getText());
    }
    updateQueryInfo(event.getText());
  }
  
  

  public void executeFrequencyQuery(List<FrequencyTableEntry> freqDefinition)
  {
    if (controller != null && controller.getPreparedQuery() != null)
    {
      PagedResultQuery preparedQuery = controller.getPreparedQuery();
      
      if (preparedQuery.getCorpora()== null || preparedQuery.getCorpora().isEmpty())
      {
        Notification.show("Please select a corpus", Notification.Type.WARNING_MESSAGE);
        btShowFrequencies.setEnabled(true);
        return;
      }
      if ("".equals(preparedQuery.getQuery()))
      {
        Notification.show("Empty query",  Notification.Type.WARNING_MESSAGE);
        btShowFrequencies.setEnabled(true);
        return;
      }
      if(resultPanel != null)
      {
        removeComponent(resultPanel);
      }
      resultPanel = new FrequencyResultPanel(preparedQuery.getQuery(), preparedQuery.getCorpora(),
        freqDefinition, this);
      addComponent(resultPanel);
      setExpandRatio(resultPanel, 1.0f);
      
      queryLayout.setVisible(false);
    }
  }
  
  private List<QueryNode> parseQuery(String query)
  {
    if(query == null || query.isEmpty())
    {
      return new LinkedList<>();
    }
    // let the service parse the query
    WebResource res = Helper.getAnnisWebResource();
    List<QueryNode> nodes = res.path("query/parse/nodes").queryParam("q", query)
      .get(new GenericType<List<QueryNode>>() {});
    
    return nodes;
  }
  
  private void createAutomaticEntriesForQuery(String query)
  {
    if(query == null || query.isEmpty())
    {
      return;
    }
    
    try
    { 

      tblFrequencyDefinition.removeAllItems();
      lblErrorOrMsg.setVisible(false);
      
      counter = 0;
      List<QueryNode> nodes = parseQuery(query);
      Collections.sort(nodes, new Comparator<QueryNode>()
      {

        @Override
        public int compare(QueryNode o1, QueryNode o2)
        {
          if(o1.getVariable() == null)
          {
            return o2 == null ? 0 : -1;
          }
          return o1.getVariable().compareTo(o2.getVariable());
        }
      });
      
      // calculate the nodes that are part of every alternative
      Multimap<String, Integer> alternativesOfVariable = LinkedHashMultimap.create();
      int maxAlternative = 0;
      for(QueryNode n : nodes)
      {
        if(n.getAlternativeNumber() != null)
        {
          maxAlternative = Math.max(n.getAlternativeNumber(), maxAlternative);
          alternativesOfVariable.put(n.getVariable(), n.getAlternativeNumber());
        }
      }
      Set<String> allowedVariables = new LinkedHashSet<>();
      for(QueryNode n : nodes)
      {
        // we assume that the alternative numbering is continuous and without gaps
        if(alternativesOfVariable.get(n.getVariable()).size() == (maxAlternative+1))
        {
          allowedVariables.add(n.getVariable());
        }
      }
      
      if(maxAlternative > 0 && allowedVariables.isEmpty())
      {
        lblErrorOrMsg.setVisible(true);
      }
      
      
      for(QueryNode n : nodes)
      {
        if(!n.isArtificial() && allowedVariables.contains(n.getVariable()))
        {
          if(n.getNodeAnnotations().isEmpty())
          {
            int id = counter++;
            tblFrequencyDefinition.addItem(
              createNewTableRow(
                tblFrequencyDefinition, id,
                n.getVariable(),
                FrequencyTableEntryType.span, "",
                "automatically created from " + n.toAQLNodeFragment()),
              id);
          }
          else
          {
            int id = counter++;
            QueryAnnotation firstAnno = n.getNodeAnnotations().iterator().next();
            tblFrequencyDefinition.addItem(
              createNewTableRow(
                tblFrequencyDefinition, id,
                n.getVariable(),
                FrequencyTableEntryType.annotation, firstAnno.getName(),
                "automatically created from " + n.toAQLNodeFragment()),
              id);

          }
        }
      }
    }
    catch(UniformInterfaceException ex)
    {
      // non-valid query, ignore
    }
    
  }
  
  private void updateQueryInfo(String query)
  {
    Set<String> selectedCorpora = controller.getSelectedCorpora();
    if(selectedCorpora.isEmpty())
    {
      lblCorpusList.setValue("none");
    }
    else
    {
      lblCorpusList.
        setValue(Joiner.on(", ").join(selectedCorpora));
    }
    if(query == null || query.isEmpty())
    {
      lblAQL.setValue("<empty query>");
    }
    else
    {
      lblAQL.setValue(query.replaceAll("[\n\r]+", " "));
    }
  }
  
  public void notifiyQueryFinished()
  {
    btShowFrequencies.setEnabled(true);
    btShowQuery.setVisible(true);
  }
  
}
