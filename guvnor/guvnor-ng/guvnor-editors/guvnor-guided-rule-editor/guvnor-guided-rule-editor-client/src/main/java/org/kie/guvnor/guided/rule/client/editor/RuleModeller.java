/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.guvnor.guided.rule.client.editor;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import org.drools.guvnor.models.commons.shared.rule.IAction;
import org.drools.guvnor.models.commons.shared.rule.IPattern;
import org.drools.guvnor.models.commons.shared.rule.RuleMetadata;
import org.drools.guvnor.models.commons.shared.rule.RuleModel;
import org.kie.guvnor.commons.security.UserCapabilities;
import org.kie.guvnor.commons.ui.client.widget.ErrorPopup;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.guided.rule.client.editor.events.TemplateVariablesChangedEvent;
import org.kie.guvnor.guided.rule.client.resources.i18n.Constants;
import org.kie.guvnor.guided.rule.client.resources.images.GuidedRuleEditorImages508;
import org.kie.guvnor.guided.rule.client.widget.RuleModellerWidget;
import org.kie.guvnor.workingset.client.WorkingSetManager;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.common.ClickableLabel;
import org.uberfire.client.common.DirtyableComposite;
import org.uberfire.client.common.DirtyableFlexTable;
import org.uberfire.client.common.DirtyableHorizontalPane;
import org.uberfire.client.common.DirtyableVerticalPane;
import org.uberfire.client.common.SmallLabel;

/**
 * This is the parent widget that contains the model based rule builder.
 */
public class RuleModeller extends DirtyableComposite
        implements
        RuleModelEditor {

    private WorkingSetManager workingSetManager = null;

    private DirtyableFlexTable layout;
    private RuleModel model;
    private DataModelOracle dataModel;
    private RuleModellerConfiguration configuration;
    private boolean showingOptions = false;
    private int currentLayoutRow = 0;
    private Path path;
    private ModellerWidgetFactory widgetFactory;
    private EventBus eventBus;
    private boolean isReadOnly = false;

    private List<RuleModellerWidget> lhsWidgets = new ArrayList<RuleModellerWidget>();
    private List<RuleModellerWidget> rhsWidgets = new ArrayList<RuleModellerWidget>();

    private boolean hasModifiedWidgets;

    private final Command onWidgetModifiedCommand = new Command() {

        public void execute() {
            hasModifiedWidgets = true;
        }
    };

    //used by RuleModeller
    public RuleModeller( final Path path,
                         final RuleModel model,
                         final DataModelOracle dataModel,
                         final ModellerWidgetFactory widgetFactory,
                         final EventBus eventBus,
                         final boolean isReadOnly ) {
        this.path = path;
        this.model = model;
        this.dataModel = dataModel;
        this.widgetFactory = widgetFactory;
        this.configuration = RuleModellerConfiguration.getDefault();
        this.eventBus = eventBus;
        this.isReadOnly = isReadOnly;

        doLayout();
    }

    //used by GuidedDecisionTableWidget
    public RuleModeller( final Path path,
                         final RuleModel model,
                         final DataModelOracle dataModel,
                         final ModellerWidgetFactory widgetFactory,
                         final RuleModellerConfiguration configuration,
                         final EventBus eventBus,
                         final boolean isReadOnly ) {
        this.path = path;
        this.model = model;
        this.dataModel = dataModel;
        this.widgetFactory = widgetFactory;
        this.configuration = configuration;
        this.eventBus = eventBus;
        this.isReadOnly = isReadOnly;

        doLayout();
    }

    protected void doLayout() {
        layout = new DirtyableFlexTable();
        initWidget();
        layout.setStyleName( "model-builder-Background" );
        initWidget( layout );
        setWidth( "100%" );
        setHeight( "100%" );
    }

    /**
     * This updates the widget to reflect the state of the model.
     */
    public void initWidget() {
        layout.removeAllRows();
        currentLayoutRow = 0;

        Image addPattern = GuidedRuleEditorImages508.INSTANCE.NewItem();
        addPattern.setTitle( Constants.INSTANCE.AddAConditionToThisRule() );
        addPattern.addClickHandler( new ClickHandler() {

            public void onClick( ClickEvent event ) {
                showConditionSelector( null );
            }
        } );

        layout.getColumnFormatter().setWidth( 0,
                                              "20px" );
        layout.getColumnFormatter().setWidth( 1,
                                              "20px" );
        layout.getColumnFormatter().setWidth( 2,
                                              "48px" );
        layout.getColumnFormatter().setWidth( 3,
                                              "*" );//90%
        layout.getColumnFormatter().setWidth( 4,
                                              "64px" );

        if ( this.showLHS() ) {
            layout.setWidget( currentLayoutRow,
                              0,
                              new SmallLabel( "<b>" + Constants.INSTANCE.WHEN() + "</b>" ) );
            layout.getFlexCellFormatter().setColSpan( currentLayoutRow,
                                                      0,
                                                      4 );

            if ( !lockLHS() ) {
                layout.setWidget( currentLayoutRow,
                                  1,
                                  addPattern );
            }
            currentLayoutRow++;

            renderLhs( this.model );
        }

        if ( this.showRHS() ) {
            layout.setWidget( currentLayoutRow,
                              0,
                              new SmallLabel( "<b>" + Constants.INSTANCE.THEN() + "</b>" ) );
            layout.getFlexCellFormatter().setColSpan( currentLayoutRow,
                                                      0,
                                                      4 );

            Image addAction = GuidedRuleEditorImages508.INSTANCE.NewItem();
            addAction.setTitle( Constants.INSTANCE.AddAnActionToThisRule() );
            addAction.addClickHandler( new ClickHandler() {

                public void onClick( ClickEvent event ) {
                    showActionSelector( (Widget) event.getSource(),
                                        null );
                }
            } );
            if ( !lockRHS() ) {
                layout.setWidget( currentLayoutRow,
                                  1,
                                  addAction );
            }
            currentLayoutRow++;

            renderRhs( this.model );
        }

        if ( showAttributes() ) {

            final int optionsRowIndex = currentLayoutRow;
            if ( !this.showingOptions ) {
                ClickableLabel showMoreOptions = new ClickableLabel( "(show options...)",
                                                                     new ClickHandler() {

                                                                         public void onClick( ClickEvent event ) {
                                                                             showingOptions = true;
                                                                             renderOptions( optionsRowIndex );
                                                                         }
                                                                     } );
                layout.setWidget( optionsRowIndex,
                                  2,
                                  showMoreOptions );
            } else {
                renderOptions( optionsRowIndex );
            }

        }

        currentLayoutRow++;
        layout.setWidget( currentLayoutRow + 1,
                          3,
                          spacerWidget() );
        layout.getCellFormatter().setHeight( currentLayoutRow + 1,
                                             3,
                                             "100%" );
    }

    private void renderOptions( final int optionsRowIndex ) {
        layout.setWidget( optionsRowIndex,
                          2,
                          new SmallLabel( Constants.INSTANCE.optionsRuleModeller() ) );
        if ( !isReadOnly ) {
            layout.setWidget( optionsRowIndex,
                              4,
                              getAddAttribute() );
        }
        layout.setWidget( optionsRowIndex + 1,
                          3,
                          new RuleAttributeWidget( this,
                                                   this.model,
                                                   isReadOnly ) );
    }

    private boolean isLock( String attr ) {

        if ( isReadOnly() ) {
            return true;
        }

        if ( this.model.metadataList.length == 0 ) {
            return false;
        }

        for ( RuleMetadata at : this.model.metadataList ) {
            if ( at.getAttributeName().equals( attr ) ) {
                return true;
            }
        }
        return false;
    }

    public boolean showRHS() {
        return !this.configuration.isHideRHS();
    }

    /**
     * return true if we should not allow unfrozen editing of the RHS
     */
    public boolean lockRHS() {
        return isLock( RuleAttributeWidget.LOCK_RHS );
    }

    public boolean showLHS() {
        return !this.configuration.isHideLHS();
    }

    /**
     * return true if we should not allow unfrozen editing of the LHS
     */
    public boolean lockLHS() {
        return isLock( RuleAttributeWidget.LOCK_LHS );
    }

    private boolean showAttributes() {
        if ( !UserCapabilities.canSeeModulesTree() ) {
            return false;
        }

        return !this.configuration.isHideAttributes();
    }

    public void refreshWidget() {
        initWidget();
        makeDirty();
    }

    private Widget getAddAttribute() {
        Image add = GuidedRuleEditorImages508.INSTANCE.NewItem();
        add.setTitle( Constants.INSTANCE.AddAnOptionToTheRuleToModifyItsBehaviorWhenEvaluatedOrExecuted() );

        add.addClickHandler( new ClickHandler() {

            public void onClick( ClickEvent event ) {
                showAttributeSelector();
            }
        } );
        return add;
    }

    protected void showAttributeSelector() {
        AttributeSelectorPopup pop = new AttributeSelectorPopup( model,
                                                                 lockLHS(),
                                                                 lockRHS(),
                                                                 new Command() {

                                                                     public void execute() {
                                                                         refreshWidget();
                                                                     }
                                                                 } );

        pop.show();
    }

    /**
     * Do all the widgets for the RHS.
     */
    private void renderRhs( final RuleModel model ) {

        for ( int i = 0; i < model.rhs.length; i++ ) {
            DirtyableVerticalPane widget = new DirtyableVerticalPane();
            widget.setWidth( "100%" );
            IAction action = model.rhs[ i ];

            //if lockRHS() set the widget RO, otherwise let them decide.
            Boolean readOnly = this.lockRHS() ? true : null;

            RuleModellerWidget w = getWidgetFactory().getWidget( this,
                                                                 eventBus,
                                                                 action,
                                                                 readOnly );
            w.addOnModifiedCommand( this.onWidgetModifiedCommand );

            widget.add( wrapRHSWidget( model,
                                       i,
                                       w ) );
            widget.add( spacerWidget() );

            layout.setWidget( currentLayoutRow,
                              0,
                              new DirtyableHorizontalPane() );
            layout.setWidget( currentLayoutRow,
                              1,
                              new DirtyableHorizontalPane() );

            layout.setWidget( currentLayoutRow,
                              2,
                              this.wrapLineNumber( i + 1,
                                                   false ) );
            layout.getFlexCellFormatter().setHorizontalAlignment( currentLayoutRow,
                                                                  2,
                                                                  HasHorizontalAlignment.ALIGN_CENTER );
            layout.getFlexCellFormatter().setVerticalAlignment( currentLayoutRow,
                                                                2,
                                                                HasVerticalAlignment.ALIGN_MIDDLE );

            layout.setWidget( currentLayoutRow,
                              3,
                              widget );
            layout.getFlexCellFormatter().setHorizontalAlignment( currentLayoutRow,
                                                                  3,
                                                                  HasHorizontalAlignment.ALIGN_LEFT );
            layout.getFlexCellFormatter().setVerticalAlignment( currentLayoutRow,
                                                                3,
                                                                HasVerticalAlignment.ALIGN_TOP );
            layout.getFlexCellFormatter().setWidth( currentLayoutRow,
                                                    3,
                                                    "100%" );

            if ( !w.isFactTypeKnown() ) {
                final Image image = GuidedRuleEditorImages508.INSTANCE.Error();
                image.setTitle( Constants.INSTANCE.InvalidPatternSectionDisabled() );
                this.addLineIcon( currentLayoutRow,
                                  0,
                                  image );
            }

            final int index = i;
            if ( !( this.lockRHS() || w.isReadOnly() ) ) {
                this.addActionsButtonsToLayout( Constants.INSTANCE.AddAnActionBelow(),
                                                new ClickHandler() {

                                                    public void onClick( ClickEvent event ) {
                                                        showActionSelector( (Widget) event.getSource(),
                                                                            index + 1 );
                                                    }
                                                },
                                                new ClickHandler() {

                                                    public void onClick( ClickEvent event ) {
                                                        model.moveRhsItemDown( index );
                                                        refreshWidget();
                                                    }
                                                },
                                                new ClickHandler() {

                                                    public void onClick( ClickEvent event ) {
                                                        model.moveRhsItemUp( index );
                                                        refreshWidget();
                                                    }
                                                }
                                              );
            }

            this.rhsWidgets.add( w );
            currentLayoutRow++;

        }

    }

    /**
     * Pops up the fact selector.
     */
    protected void showConditionSelector( Integer position ) {
        RuleModellerConditionSelectorPopup popup = new RuleModellerConditionSelectorPopup( model,
                                                                                           this,
                                                                                           position,
                                                                                           getSuggestionCompletions() );
        popup.show();
    }

    protected void showActionSelector( Widget w,
                                       Integer position ) {
        RuleModellerActionSelectorPopup popup = new RuleModellerActionSelectorPopup( model,
                                                                                     this,
                                                                                     position,
                                                                                     getSuggestionCompletions() );
        popup.show();
    }

    /**
     * Builds all the condition widgets.
     */
    private void renderLhs( final RuleModel model ) {

        for ( int i = 0; i < model.lhs.length; i++ ) {
            DirtyableVerticalPane vert = new DirtyableVerticalPane();
            vert.setWidth( "100%" );

            //if lockLHS() set the widget RO, otherwise let them decide.
            Boolean readOnly = this.lockLHS() ? true : null;

            IPattern pattern = model.lhs[ i ];

            RuleModellerWidget w = getWidgetFactory().getWidget( this,
                                                                 eventBus,
                                                                 pattern,
                                                                 readOnly );
            w.addOnModifiedCommand( this.onWidgetModifiedCommand );

            vert.add( wrapLHSWidget( model,
                                     i,
                                     w ) );
            vert.add( spacerWidget() );

            layout.setWidget( currentLayoutRow,
                              0,
                              new DirtyableHorizontalPane() );
            layout.setWidget( currentLayoutRow,
                              1,
                              new DirtyableHorizontalPane() );

            layout.setWidget( currentLayoutRow,
                              2,
                              this.wrapLineNumber( i + 1,
                                                   true ) );
            layout.getFlexCellFormatter().setHorizontalAlignment( currentLayoutRow,
                                                                  2,
                                                                  HasHorizontalAlignment.ALIGN_CENTER );
            layout.getFlexCellFormatter().setVerticalAlignment( currentLayoutRow,
                                                                2,
                                                                HasVerticalAlignment.ALIGN_MIDDLE );

            layout.setWidget( currentLayoutRow,
                              3,
                              vert );
            layout.getFlexCellFormatter().setHorizontalAlignment( currentLayoutRow,
                                                                  3,
                                                                  HasHorizontalAlignment.ALIGN_LEFT );
            layout.getFlexCellFormatter().setVerticalAlignment( currentLayoutRow,
                                                                3,
                                                                HasVerticalAlignment.ALIGN_TOP );
            layout.getFlexCellFormatter().setWidth( currentLayoutRow,
                                                    3,
                                                    "100%" );

            if ( !w.isFactTypeKnown() ) {
                final Image image = GuidedRuleEditorImages508.INSTANCE.Error();
                image.setTitle( Constants.INSTANCE.InvalidPatternSectionDisabled() );
                this.addLineIcon( currentLayoutRow,
                                  0,
                                  image );
            }

            final int index = i;
            if ( !( this.lockLHS() || w.isReadOnly() ) ) {
                this.addActionsButtonsToLayout( Constants.INSTANCE.AddAConditionBelow(),
                                                new ClickHandler() {

                                                    public void onClick( ClickEvent event ) {
                                                        showConditionSelector( index + 1 );
                                                    }
                                                },
                                                new ClickHandler() {

                                                    public void onClick( ClickEvent event ) {
                                                        model.moveLhsItemDown( index );
                                                        refreshWidget();
                                                    }
                                                },
                                                new ClickHandler() {

                                                    public void onClick( ClickEvent event ) {
                                                        model.moveLhsItemUp( index );
                                                        refreshWidget();
                                                    }
                                                }
                                              );
            }

            this.lhsWidgets.add( w );
            currentLayoutRow++;
        }

    }

    private HTML spacerWidget() {
        HTML h = new HTML( "&nbsp;" ); //NON-NLS
        h.setHeight( "2px" ); //NON-NLS
        return h;
    }

    private Widget wrapLineNumber( int number,
                                   boolean isLHSLine ) {
        String id = "rhsLine";
        if ( isLHSLine ) {
            id = "lhsLine";
        }
        id += number;
        DirtyableHorizontalPane horiz = new DirtyableHorizontalPane();
        horiz.add( new HTML( "<div class='form-field' id='" + id + "'>" + number + ".</div>" ) );
        return horiz;
    }

    /**
     * This adds the widget to the UI, also adding the remove icon.
     */
    private Widget wrapLHSWidget( final RuleModel model,
                                  int i,
                                  RuleModellerWidget w ) {
        final DirtyableFlexTable wrapper = new DirtyableFlexTable();
        final Image remove = GuidedRuleEditorImages508.INSTANCE.DeleteItemSmall();
        remove.setTitle( Constants.INSTANCE.RemoveThisENTIREConditionAndAllTheFieldConstraintsThatBelongToIt() );
        final int idx = i;
        remove.addClickHandler( new ClickHandler() {

            public void onClick( ClickEvent event ) {
                if ( Window.confirm( Constants.INSTANCE.RemoveThisEntireConditionQ() ) ) {
                    if ( model.removeLhsItem( idx ) ) {
                        refreshWidget();

                        //Signal possible change in Template variables
                        TemplateVariablesChangedEvent tvce = new TemplateVariablesChangedEvent( model );
                        eventBus.fireEventFromSource( tvce,
                                                      model );
                    } else {
                        ErrorPopup.showMessage( Constants.INSTANCE.CanTRemoveThatItemAsItIsUsedInTheActionPartOfTheRule() );
                    }
                }
            }
        } );

        wrapper.getColumnFormatter().setWidth( 0,
                                               "100%" );
        w.setWidth( "100%" );
        wrapper.setWidget( 0,
                           0,
                           w );
        if ( !( this.lockLHS() || w.isReadOnly() ) || !w.isFactTypeKnown() ) {
            wrapper.setWidget( 0,
                               1,
                               remove );
            wrapper.getColumnFormatter().setWidth( 1,
                                                   "20px" );
        }

        return wrapper;
    }

    /**
     * This adds the widget to the UI, also adding the remove icon.
     */
    private Widget wrapRHSWidget( final RuleModel model,
                                  int i,
                                  RuleModellerWidget w ) {
        final DirtyableFlexTable wrapper = new DirtyableFlexTable();
        final Image remove = GuidedRuleEditorImages508.INSTANCE.DeleteItemSmall();
        remove.setTitle( Constants.INSTANCE.RemoveThisAction() );
        final int idx = i;
        remove.addClickHandler( new ClickHandler() {

            public void onClick( ClickEvent event ) {
                if ( Window.confirm( Constants.INSTANCE.RemoveThisItem() ) ) {
                    model.removeRhsItem( idx );
                    refreshWidget();

                    //Signal possible change in Template variables
                    TemplateVariablesChangedEvent tvce = new TemplateVariablesChangedEvent( model );
                    eventBus.fireEventFromSource( tvce,
                                                  model );
                }
            }
        } );

        //        if ( !(w instanceof ActionRetractFactWidget) ) {
        //            w.setWidth( "100%" );
        //            horiz.setWidth( "100%" );
        //        }

        wrapper.getColumnFormatter().setWidth( 0,
                                               "100%" );
        w.setWidth( "100%" );
        wrapper.setWidget( 0,
                           0,
                           w );

        if ( !( this.lockRHS() || w.isReadOnly() ) || !w.isFactTypeKnown() ) {
            wrapper.setWidget( 0,
                               1,
                               remove );
            wrapper.getColumnFormatter().setWidth( 1,
                                                   "20px" );
        }

        return wrapper;
    }

    private void addLineIcon( int row,
                              int col,
                              Image icon ) {
        Widget widget = layout.getWidget( row,
                                          col );
        if ( widget instanceof DirtyableHorizontalPane ) {
            DirtyableHorizontalPane horiz = (DirtyableHorizontalPane) widget;
            horiz.add( icon );
        }
    }

    private void clearLineIcons( int row,
                                 int col ) {
        if ( layout.getCellCount( row ) <= col ) {
            return;
        }
        Widget widget = layout.getWidget( row,
                                          col );
        if ( widget instanceof DirtyableHorizontalPane ) {
            DirtyableHorizontalPane horiz = (DirtyableHorizontalPane) widget;
            horiz.clear();
        }
    }

    private void clearLinesIcons( int col ) {
        for ( int i = 0; i < layout.getRowCount(); i++ ) {
            this.clearLineIcons( i,
                                 col );
        }
    }

    private void addActionsButtonsToLayout( String title,
                                            ClickHandler addBelowListener,
                                            ClickHandler moveDownListener,
                                            ClickHandler moveUpListener ) {

        final DirtyableHorizontalPane hp = new DirtyableHorizontalPane();

        Image addPattern = GuidedRuleEditorImages508.INSTANCE.NewItemBelow();
        addPattern.setTitle( title );
        addPattern.addClickHandler( addBelowListener );

        Image moveDown = GuidedRuleEditorImages508.INSTANCE.MoveDown();
        moveDown.setTitle( Constants.INSTANCE.MoveDown() );
        moveDown.addClickHandler( moveDownListener );

        Image moveUp = GuidedRuleEditorImages508.INSTANCE.MoveUp();
        moveUp.setTitle( Constants.INSTANCE.MoveUp() );
        moveUp.addClickHandler( moveUpListener );

        hp.add( addPattern );
        hp.add( moveDown );
        hp.add( moveUp );

        layout.setWidget( currentLayoutRow,
                          4,
                          hp );
        layout.getFlexCellFormatter().setHorizontalAlignment( currentLayoutRow,
                                                              4,
                                                              HasHorizontalAlignment.ALIGN_CENTER );
        layout.getFlexCellFormatter().setVerticalAlignment( currentLayoutRow,
                                                            4,
                                                            HasVerticalAlignment.ALIGN_MIDDLE );
    }

    public RuleModel getModel() {
        return model;
    }

    /**
     * Returns true is a var name has already been used either by the rule, or
     * as a global.
     */
    public boolean isVariableNameUsed( String name ) {
        return model.isVariableNameUsed( name ) || getSuggestionCompletions().isGlobalVariable( name );
    }

    @Override
    public boolean isDirty() {
        return ( layout.hasDirty() || dirtyflag );
    }

    public DataModelOracle getSuggestionCompletions() {
        return dataModel;
    }

    public ModellerWidgetFactory getWidgetFactory() {
        return widgetFactory;
    }

    public RuleModeller getRuleModeller() {
        return this;
    }

    public boolean isTemplate() {
        return widgetFactory.isTemplate();
    }

    public Path getPath() {
        return path;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

}
