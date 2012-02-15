/*
 * Copyright 2011 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.guvnor.client.decisiontable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.guvnor.client.asseteditor.drools.modeldriven.ui.BindingTextBox;
import org.drools.guvnor.client.common.FormStylePopup;
import org.drools.guvnor.client.common.ImageButton;
import org.drools.guvnor.client.common.InfoPopup;
import org.drools.guvnor.client.common.SmallLabel;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.resources.Images;
import org.drools.ide.common.client.modeldriven.FieldAccessorsAndMutators;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.dt52.ActionCol52;
import org.drools.ide.common.client.modeldriven.dt52.ActionInsertFactCol52;
import org.drools.ide.common.client.modeldriven.dt52.ActionWorkItemCol52;
import org.drools.ide.common.client.modeldriven.dt52.ActionWorkItemInsertFactCol52;
import org.drools.ide.common.client.modeldriven.dt52.ConditionCol52;
import org.drools.ide.common.client.modeldriven.dt52.GuidedDecisionTable52;
import org.drools.ide.common.client.modeldriven.dt52.Pattern52;
import org.drools.ide.common.shared.workitems.PortableParameterDefinition;
import org.drools.ide.common.shared.workitems.PortableWorkDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * A popup to define an Action to insert a new Fact and set one of its fields to
 * the value of a Work Item Result parameter
 */
public class ActionWorkItemInsertFactPopup extends FormStylePopup {

    private SmallLabel                     patternLabel                = new SmallLabel();
    private TextBox                        fieldLabel                  = getFieldLabel();
    private ListBox                        workItemResultParameters    = new ListBox();
    private Map<String, WorkItemParameter> workItemResultParametersMap = new HashMap<String, WorkItemParameter>();

    private ActionWorkItemInsertFactCol52  editingCol;
    private GuidedDecisionTable52          model;
    private SuggestionCompletionEngine     sce;

    private final boolean                  isReadOnly;

    //Container to contain WorkItem and WorkItem Parameters associations
    private static class WorkItemParameter {

        WorkItemParameter(PortableWorkDefinition workDefinition,
                          PortableParameterDefinition workParameterDefinition) {
            this.workDefinition = workDefinition;
            this.workParameterDefinition = workParameterDefinition;
        }

        PortableWorkDefinition      workDefinition;
        PortableParameterDefinition workParameterDefinition;
    }

    public ActionWorkItemInsertFactPopup(final SuggestionCompletionEngine sce,
                                         final GuidedDecisionTable52 model,
                                         final GenericColumnCommand refreshGrid,
                                         final ActionWorkItemInsertFactCol52 col,
                                         final boolean isNew,
                                         final boolean isReadOnly) {
        this.editingCol = cloneActionInsertColumn( col );
        this.model = model;
        this.sce = sce;
        this.isReadOnly = isReadOnly;

        setTitle( Constants.INSTANCE.ColumnConfigurationWorkItemInsertFact() );
        setModal( false );

        //Fact being inserted
        HorizontalPanel pattern = new HorizontalPanel();
        pattern.add( patternLabel );
        doPatternLabel();

        ImageButton changePattern = new ImageButton( Images.INSTANCE.edit(),
                                                     Images.INSTANCE.editDisabled(),
                                                     Constants.INSTANCE.ChooseAPatternThatThisColumnAddsDataTo(),
                                                     new ClickHandler() {
                                                         public void onClick(ClickEvent w) {
                                                             showChangePattern( w );
                                                         }
                                                     } );
        changePattern.setEnabled( !isReadOnly );
        pattern.add( changePattern );
        addAttribute( Constants.INSTANCE.Pattern(),
                      pattern );

        //Fact field being set
        HorizontalPanel field = new HorizontalPanel();
        fieldLabel.setEnabled( !isReadOnly );
        field.add( fieldLabel );
        ImageButton editField = new ImageButton( Images.INSTANCE.edit(),
                                                 Images.INSTANCE.editDisabled(),
                                                 Constants.INSTANCE.EditTheFieldThatThisColumnOperatesOn(),
                                                 new ClickHandler() {
                                                     public void onClick(ClickEvent w) {
                                                         showFieldChange();
                                                     }
                                                 } );
        editField.setEnabled( !isReadOnly );
        field.add( editField );
        addAttribute( Constants.INSTANCE.Field(),
                      field );
        doFieldLabel();

        //Column header
        final TextBox header = new TextBox();
        header.setText( col.getHeader() );
        header.setEnabled( !isReadOnly );
        if ( !isReadOnly ) {
            header.addChangeHandler( new ChangeHandler() {
                public void onChange(ChangeEvent event) {
                    editingCol.setHeader( header.getText() );
                }
            } );
        }
        addAttribute( Constants.INSTANCE.ColumnHeaderDescription(),
                      header );

        //Logical insertion
        addAttribute( Constants.INSTANCE.LogicallyInsertColon(),
                      doInsertLogical() );

        //Bind field to a WorkItem result parameter
        addAttribute( Constants.INSTANCE.BindActionFieldToWorkItem(),
                      doBindFieldToWorkItem() );
        if ( !isReadOnly ) {
            workItemResultParameters.addChangeHandler( new ChangeHandler() {

                public void onChange(ChangeEvent event) {
                    int index = workItemResultParameters.getSelectedIndex();
                    if ( index >= 0 ) {
                        String key = workItemResultParameters.getValue( index );
                        WorkItemParameter wip = workItemResultParametersMap.get( key );
                        editingCol.setWorkItemName( wip.workDefinition.getName() );
                        editingCol.setWorkItemResultParameterName( wip.workParameterDefinition.getName() );
                        editingCol.setParameterClassName( wip.workParameterDefinition.getClassName() );
                    }
                }

            } );
        }

        //Hide column tick-box
        addAttribute( Constants.INSTANCE.HideThisColumn(),
                      DTCellValueWidgetFactory.getHideColumnIndicator( editingCol ) );

        Button apply = new Button( Constants.INSTANCE.ApplyChanges() );
        apply.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent w) {
                if ( !isValidFactType() ) {
                    Window.alert(Constants.INSTANCE.YouMustEnterAColumnPattern());
                    return;
                }
                if ( !isValidFactField() ) {
                    Window.alert( Constants.INSTANCE.YouMustEnterAColumnField() );
                    return;
                }
                if ( null == editingCol.getHeader()
                        || "".equals( editingCol.getHeader() ) ) {
                    Window.alert( Constants.INSTANCE.YouMustEnterAColumnHeaderValueDescription() );
                    return;
                }
                if ( isNew ) {
                    if ( !unique( editingCol.getHeader() ) ) {
                        Window.alert( Constants.INSTANCE.ThatColumnNameIsAlreadyInUsePleasePickAnother() );
                        return;
                    }

                } else {
                    if ( !col.getHeader().equals( editingCol.getHeader() ) ) {
                        if ( !unique( editingCol.getHeader() ) ) {
                            Window.alert( Constants.INSTANCE.ThatColumnNameIsAlreadyInUsePleasePickAnother() );
                            return;
                        }
                    }
                }

                // Pass new\modified column back for handling
                refreshGrid.execute( editingCol );
                hide();
            }
        } );
        addAttribute( "",
                      apply );

    }

    private ActionWorkItemInsertFactCol52 cloneActionInsertColumn(ActionWorkItemInsertFactCol52 col) {
        ActionWorkItemInsertFactCol52 clone = new ActionWorkItemInsertFactCol52();
        clone.setBoundName( col.getBoundName() );
        clone.setType( col.getType() );
        clone.setFactField( col.getFactField() );
        clone.setFactType( col.getFactType() );
        clone.setHeader( col.getHeader() );
        clone.setValueList( col.getValueList() );
        clone.setDefaultValue( col.getDefaultValue() );
        clone.setHideColumn( col.isHideColumn() );
        clone.setInsertLogical( col.isInsertLogical() );
        clone.setWorkItemName( col.getWorkItemName() );
        clone.setWorkItemResultParameterName( col.getWorkItemResultParameterName() );
        clone.setParameterClassName( col.getParameterClassName() );
        return clone;
    }

    private void doFieldLabel() {
        if ( nil( this.editingCol.getFactField() ) ) {
            fieldLabel.setText( Constants.INSTANCE.pleaseChooseFactType() );
        } else {
            fieldLabel.setText( editingCol.getFactField() );
        }
    }

    private void doPatternLabel() {
        if ( this.editingCol.getFactType() != null ) {
            this.patternLabel.setText( this.editingCol.getFactType() + " [" + editingCol.getBoundName() + "]" );
        }
    }

    private TextBox getFieldLabel() {
        final TextBox box = new TextBox();
        box.addChangeHandler( new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                editingCol.setFactField( box.getText() );
            }
        } );
        return box;
    }

    private ListBox loadPatterns() {
        Set<String> vars = new HashSet<String>();
        ListBox patterns = new ListBox();

        for ( Object o : model.getActionCols() ) {
            ActionCol52 col = (ActionCol52) o;
            if ( col instanceof ActionInsertFactCol52 ) {
                ActionInsertFactCol52 c = (ActionInsertFactCol52) col;
                if ( !vars.contains( c.getBoundName() ) ) {
                    patterns.addItem( c.getFactType() + " [" + c.getBoundName() + "]",
                                      c.getFactType() + " " + c.getBoundName() );
                    vars.add( c.getBoundName() );
                }
            }
        }
        return patterns;
    }

    private boolean nil(String s) {
        return s == null || s.equals( "" );
    }

    private void showFieldChange() {
        final FormStylePopup pop = new FormStylePopup();
        pop.setModal( false );
        String[] fields = this.sce.getFieldCompletions(
                                                        FieldAccessorsAndMutators.MUTATOR,
                                                        this.editingCol.getFactType() );
        final ListBox box = new ListBox();
        for ( int i = 0; i < fields.length; i++ ) {
            box.addItem( fields[i] );
        }
        pop.addAttribute( Constants.INSTANCE.Field(),
                          box );
        Button b = new Button( Constants.INSTANCE.OK() );
        pop.addAttribute( "",
                          b );
        b.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent w) {
                editingCol.setFactField( box.getItemText( box.getSelectedIndex() ) );
                editingCol.setType( sce.getFieldType( editingCol.getFactType(),
                                                      editingCol.getFactField() ) );
                doBindFieldToWorkItem();
                doFieldLabel();
                pop.hide();
            }
        } );
        pop.show();

    }

    private boolean unique(String header) {
        for ( ActionCol52 o : model.getActionCols() ) {
            if ( o.getHeader().equals( header ) ) return false;
        }
        return true;
    }

    protected void showChangePattern(ClickEvent w) {

        final ListBox pats = this.loadPatterns();
        if ( pats.getItemCount() == 0 ) {
            showNewPatternDialog();
            return;
        }
        final FormStylePopup pop = new FormStylePopup();
        Button ok = new Button( "OK" );
        HorizontalPanel hp = new HorizontalPanel();
        hp.add( pats );
        hp.add( ok );

        pop.addAttribute( Constants.INSTANCE.ChooseExistingPatternToAddColumnTo(),
                          hp );
        pop.addAttribute( "",
                          new HTML( Constants.INSTANCE.ORwithEmphasis() ) );

        Button createPattern = new Button( Constants.INSTANCE.CreateNewFactPattern() );
        createPattern.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent w) {
                pop.hide();
                showNewPatternDialog();
            }
        } );
        pop.addAttribute( "",
                          createPattern );

        ok.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent w) {
                String[] val = pats.getValue( pats.getSelectedIndex() ).split( "\\s" );
                editingCol.setFactType( val[0] );
                editingCol.setBoundName( val[1] );
                editingCol.setFactField( null );
                doBindFieldToWorkItem();
                doPatternLabel();
                doFieldLabel();
                pop.hide();
            }
        } );

        pop.show();
    }

    protected void showNewPatternDialog() {
        final FormStylePopup pop = new FormStylePopup();
        pop.setTitle( Constants.INSTANCE.NewFactSelectTheType() );
        final ListBox types = new ListBox();
        for ( int i = 0; i < sce.getFactTypes().length; i++ ) {
            types.addItem( sce.getFactTypes()[i] );
        }
        pop.addAttribute( Constants.INSTANCE.FactType(),
                          types );
        final TextBox binding = new BindingTextBox();
        pop.addAttribute( Constants.INSTANCE.Binding(),
                          binding );

        Button ok = new Button( Constants.INSTANCE.OK() );
        ok.addClickHandler( new ClickHandler() {
            public void onClick(ClickEvent w) {

                //Validate column configuration
                String ft = types.getItemText( types.getSelectedIndex() );
                String fn = binding.getText();
                if ( fn.equals( "" ) ) {
                    Window.alert( Constants.INSTANCE.PleaseEnterANameForFact() );
                    return;
                } else if ( fn.equals( ft ) ) {
                    Window.alert( Constants.INSTANCE.PleaseEnterANameThatIsNotTheSameAsTheFactType() );
                    return;
                } else if ( !isBindingUnique( fn ) ) {
                    Window.alert( Constants.INSTANCE.PleaseEnterANameThatIsNotAlreadyUsedByAnotherPattern() );
                    return;
                }

                //Configure column
                editingCol.setBoundName( binding.getText() );
                editingCol.setFactType( types.getItemText( types.getSelectedIndex() ) );
                editingCol.setFactField( null );
                doBindFieldToWorkItem();
                doPatternLabel();
                doFieldLabel();
                pop.hide();
            }
        } );
        pop.addAttribute( "",
                          ok );

        pop.show();
    }

    private boolean isBindingUnique(String binding) {
        for ( Pattern52 p : model.getPatterns() ) {
            if ( p.getBoundName().equals( binding ) ) return false;
            for ( ConditionCol52 c : p.getChildColumns() ) {
                if ( c.isBound() ) {
                    if ( c.getBinding().equals( binding ) ) return false;
                }
            }
        }
        return true;
    }

    private boolean isValidFactType() {
        return !(editingCol.getFactType() == null || "".equals( editingCol.getFactType() ));
    }

    private boolean isValidFactField() {
        return !(editingCol.getFactField() == null || "".equals( editingCol.getFactField() ));
    }

    private Widget doInsertLogical() {
        HorizontalPanel hp = new HorizontalPanel();

        final CheckBox cb = new CheckBox();
        cb.setValue( editingCol.isInsertLogical() );
        cb.setText( "" );
        cb.setEnabled( !isReadOnly );
        if ( !isReadOnly ) {
            cb.addClickHandler( new ClickHandler() {
                public void onClick(ClickEvent arg0) {
                    if ( sce.isGlobalVariable( editingCol.getBoundName() ) ) {
                        cb.setEnabled( false );
                        editingCol.setInsertLogical( false );
                    } else {
                        editingCol.setInsertLogical( cb.getValue() );
                    }
                }
            } );
        }
        hp.add( cb );
        hp.add( new InfoPopup( Constants.INSTANCE.LogicallyInsertANewFact(),
                               Constants.INSTANCE.LogicallyAssertAFactTheFactWillBeRetractedWhenTheSupportingEvidenceIsRemoved() ) );
        return hp;
    }

    //Populate list of WorkItem Result Parameters for the Fact\Fields data-type
    private Widget doBindFieldToWorkItem() {
        workItemResultParameters.clear();
        workItemResultParametersMap.clear();

        //Get list of Work Items executed by Actions
        List<PortableWorkDefinition> actionWorkItems = new ArrayList<PortableWorkDefinition>();
        for ( ActionCol52 ac : model.getActionCols() ) {
            if ( ac instanceof ActionWorkItemCol52 ) {
                PortableWorkDefinition pwd = ((ActionWorkItemCol52) ac).getWorkItemDefinition();
                actionWorkItems.add( pwd );
            }
        }

        //Populate list of available result parameters
        if ( actionWorkItems.size() == 0 ) {
            workItemResultParameters.setEnabled( false );
            workItemResultParameters.addItem( Constants.INSTANCE.NoWorkItemsAvailable() );
            editingCol.setWorkItemName( null );
            editingCol.setWorkItemResultParameterName( null );
            editingCol.setParameterClassName( null );
        } else {
            int selectedItemIndex = -1;
            String selectedItemKey = editingCol.getWorkItemName() + "." + editingCol.getWorkItemResultParameterName();
            workItemResultParameters.setEnabled( true && !isReadOnly );
            for ( PortableWorkDefinition pwd : actionWorkItems ) {
                for ( PortableParameterDefinition ppd : pwd.getResults() ) {
                    if ( acceptParameterType( ppd ) ) {
                        String key = pwd.getName() + "." + ppd.getName();
                        String parameterDisplayName = pwd.getDisplayName() + "." + ppd.getName();

                        //Pre-select item if applicable
                        if ( key.equals( selectedItemKey ) ) {
                            selectedItemIndex = workItemResultParameters.getItemCount();
                        }
                        workItemResultParametersMap.put( key,
                                                         new WorkItemParameter( pwd,
                                                                                ppd ) );
                        workItemResultParameters.addItem( parameterDisplayName,
                                                          key );
                    }
                }
            }

            //Disable selection if no suitable parameters were found
            if ( workItemResultParameters.getItemCount() == 0 ) {
                workItemResultParameters.setEnabled( false );
                workItemResultParameters.addItem( Constants.INSTANCE.NoWorkItemsAvailable() );
                editingCol.setWorkItemName( null );
                editingCol.setWorkItemResultParameterName( null );
                editingCol.setParameterClassName( null );
            } else {

                //Select first item if none were pre-selected
                if ( selectedItemIndex == -1 ) {
                    selectedItemIndex = 0;
                    selectedItemKey = workItemResultParameters.getValue( selectedItemIndex );
                    WorkItemParameter wip = workItemResultParametersMap.get( selectedItemKey );
                    editingCol.setWorkItemName( wip.workDefinition.getName() );
                    editingCol.setWorkItemResultParameterName( wip.workParameterDefinition.getName() );
                    editingCol.setParameterClassName( wip.workParameterDefinition.getClassName() );
                }
                workItemResultParameters.setSelectedIndex( selectedItemIndex );
            }
        }

        return workItemResultParameters;
    }

    private boolean acceptParameterType(PortableParameterDefinition ppd) {
        if ( nil( editingCol.getFactField() ) ) {
            return false;
        }
        if ( ppd.getClassName() == null ) {
            return false;
        }
        String fieldClassName = sce.getFieldClassName( editingCol.getFactType(),
                                                       editingCol.getFactField() );
        return fieldClassName.equals( ppd.getClassName() );
    }

}
