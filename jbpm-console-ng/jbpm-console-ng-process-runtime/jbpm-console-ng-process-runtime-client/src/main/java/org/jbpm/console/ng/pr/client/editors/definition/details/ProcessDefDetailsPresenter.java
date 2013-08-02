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

package org.jbpm.console.ng.pr.client.editors.definition.details;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.ListBox;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;

import java.util.ArrayList;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.jbpm.console.ng.bd.service.DataServiceEntryPoint;
import org.jbpm.console.ng.ht.model.TaskDefSummary;
import org.jbpm.console.ng.pr.client.i18n.Constants;
import org.jbpm.console.ng.pr.model.DummyProcessPath;
import org.jbpm.console.ng.pr.model.ProcessSummary;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.VFSService;
import org.uberfire.lifecycle.OnOpen;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberView;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.MenuItem;
import org.uberfire.workbench.model.menu.Menus;

@Dependent
@WorkbenchScreen(identifier = "Process Definition Details")
public class ProcessDefDetailsPresenter {

    private PlaceRequest place;

    public interface ProcessDefDetailsView extends UberView<ProcessDefDetailsPresenter> {

        void displayNotification( String text );

        TextBox getNroOfHumanTasksText();

        TextBox getProcessNameText();
        
        TextBox getProcessIdText();

        ListBox getHumanTasksListBox();

        ListBox getUsersGroupsListBox();

        ListBox getProcessDataListBox();

        ListBox getSubprocessListBox();

        TextBox getDeploymentIdText();

        void setProcessAssetPath( Path processAssetPath );

        void setEncodedProcessSource( String encodedProcessSource );
        
        Path getProcessAssetPath();
        
        String getEncodedProcessSource();
    }
    
    private Menus menus;
    
    @Inject
    private PlaceManager placeManager;

    @Inject
    private ProcessDefDetailsView view;

    @Inject
    private Caller<DataServiceEntryPoint> dataServices;

    @Inject
    private Caller<VFSService> fileServices;

    private Constants constants = GWT.create( Constants.class );

    public ProcessDefDetailsPresenter() {
        makeMenuBar();
    }
    
    

    @OnStartup
    public void onStartup( final PlaceRequest place ) {
        this.place = place;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return constants.Definition_Details();
    }

    @WorkbenchPartView
    public UberView<ProcessDefDetailsPresenter> getView() {
        return view;
    }

    public void refreshProcessDef( final String processId ) {
        dataServices.call( new RemoteCallback<List<TaskDefSummary>>() {
            @Override
            public void callback( List<TaskDefSummary> tasks ) {
                view.getNroOfHumanTasksText().setText( String.valueOf( tasks.size() ) );
                view.getHumanTasksListBox().clear();
                for ( TaskDefSummary t : tasks ) {
                    view.getHumanTasksListBox().addItem( t.getName(), String.valueOf( t.getId() ) );
                }
            }
        } ).getAllTasksDef( processId );

        dataServices.call( new RemoteCallback<Map<String, String>>() {
            @Override
            public void callback( Map<String, String> entities ) {
                view.getUsersGroupsListBox().clear();
                for ( String key : entities.keySet() ) {
                    view.getUsersGroupsListBox().addItem( entities.get( key ) + "- " + key, key );
                }
            }
        } ).getAssociatedEntities( processId );

        dataServices.call( new RemoteCallback<Map<String, String>>() {
            @Override
            public void callback( Map<String, String> inputs ) {
                view.getProcessDataListBox().clear();
                for ( String key : inputs.keySet() ) {
                    view.getProcessDataListBox().addItem( key + "- " + inputs.get( key ), key );
                }
            }
        } ).getRequiredInputData( processId );

        dataServices.call( new RemoteCallback<Collection<String>>() {
            @Override
            public void callback( Collection<String> subprocesses ) {
                view.getSubprocessListBox().clear();
                for ( String key : subprocesses ) {
                    view.getSubprocessListBox().addItem( key, key );
                }
            }
        } ).getReusableSubProcesses( processId );

        dataServices.call( new RemoteCallback<ProcessSummary>() {
            @Override
            public void callback( ProcessSummary process ) {
                view.setEncodedProcessSource( process.getEncodedProcessSource() );
                view.getProcessNameText().setText(process.getName());
                if ( process.getOriginalPath() != null ) {
                    fileServices.call( new RemoteCallback<Path>() {
                        @Override
                        public void callback( Path processPath ) {
                            view.setProcessAssetPath( processPath );
                        }
                    } ).get( process.getOriginalPath() );
                } else {
                    view.setProcessAssetPath( new DummyProcessPath( process.getId() ) );
                }
            }
        } ).getProcessById( processId );
    }

    @OnOpen
    public void onOpen() {
        String processId = place.getParameter( "processId", "" );
        view.getProcessIdText().setText( processId );
        String deploymentId = place.getParameter( "deploymentId", "none" );
        view.getDeploymentIdText().setText( deploymentId );

        refreshProcessDef( processId );
    }
    
    @WorkbenchMenu
    public Menus getMenus() {
        return menus;
    }
    
    private void makeMenuBar() {
        menus = MenuFactory
                .newTopLevelMenu( constants.New_Instance()).respondsWith(new Command() {
                        @Override
                        public void execute() {
                            PlaceRequest placeRequestImpl = new DefaultPlaceRequest( "Form Display" );
                            placeRequestImpl.addParameter( "processId", view.getProcessIdText().getText() );
                            placeRequestImpl.addParameter( "domainId", view.getDeploymentIdText().getText() );
                            placeManager.goTo( placeRequestImpl );
                        }
                     }).endMenu()
                .newTopLevelMenu( constants.Options())
                .withItems(getOptions())
                .endMenu()
                .newTopLevelMenu( constants.Refresh() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        refreshProcessDef( view.getProcessNameText().getText() );
                        view.displayNotification( constants.Process_Definition_Details_Refreshed() );
                    }
                } )
                .endMenu().build();

    }
    private List<MenuItem> getOptions(){
        List<MenuItem> menuItems = new ArrayList<MenuItem>(2);
        
        menuItems.add( MenuFactory.newSimpleItem( constants.View_Process_Model()).respondsWith( new Command() {
            @Override
            public void execute() {
                PlaceRequest placeRequestImpl = new DefaultPlaceRequest( "Designer" );

                if ( view.getEncodedProcessSource() != null ) {
                    placeRequestImpl.addParameter( "readOnly", "true" );
                    placeRequestImpl.addParameter( "encodedProcessSource", view.getEncodedProcessSource() );
                }
                placeManager.goTo( view.getProcessAssetPath(), placeRequestImpl );
            }
        } ).endMenu().build().getItems().get( 0 ) );
        
        menuItems.add( MenuFactory.newSimpleItem( constants.View_Process_Instances()).respondsWith( new Command() {
            @Override
            public void execute() {
                PlaceRequest placeRequestImpl = new DefaultPlaceRequest( "Process Instances" );
                placeRequestImpl.addParameter( "processName", view.getProcessNameText().getText() );
                placeManager.goTo( placeRequestImpl );
            }
        } ).endMenu().build().getItems().get( 0 ) );
        
        
        return menuItems;
    
    }

}
