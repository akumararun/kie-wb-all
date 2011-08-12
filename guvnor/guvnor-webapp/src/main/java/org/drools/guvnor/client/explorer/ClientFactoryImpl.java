/*
 * Copyright 2011 JBoss Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.drools.guvnor.client.explorer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import org.drools.guvnor.client.common.AssetEditorFactory;
import org.drools.guvnor.client.explorer.navigation.NavigationViewFactory;
import org.drools.guvnor.client.explorer.navigation.NavigationViewFactoryImpl;
import org.drools.guvnor.client.explorer.perspectives.PerspectivesPanelView;
import org.drools.guvnor.client.explorer.perspectives.PerspectivesPanelViewImpl;
import org.drools.guvnor.client.rpc.AssetServiceAsync;
import org.drools.guvnor.client.rpc.CategoryServiceAsync;
import org.drools.guvnor.client.rpc.ConfigurationService;
import org.drools.guvnor.client.rpc.ConfigurationServiceAsync;
import org.drools.guvnor.client.rpc.PackageServiceAsync;
import org.drools.guvnor.client.rpc.RepositoryServiceAsync;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.widgets.assetviewer.AssetViewerActivityView;
import org.drools.guvnor.client.widgets.assetviewer.AssetViewerActivityViewImpl;

public class ClientFactoryImpl
        implements
        ClientFactory {

    private final PlaceController placeController;
    private PerspectivesPanelView perspectivesPanelView;
    private NavigationViewFactoryImpl navigationViewFactory;
    private AssetEditorFactory assetEditorFactory;
    private PlaceHistoryHandler placeHistoryHandler;
    private GuvnorPlaceHistoryMapper guvnorPlaceHistoryMapper;
    private final EventBus eventBus;

    public ClientFactoryImpl(EventBus eventBus) {
        this.eventBus = eventBus;
        this.placeController = new PlaceController(eventBus);
    }

    public PlaceController getPlaceController() {
        return placeController;
    }

    public PerspectivesPanelView getPerspectivesPanelView() {
        if (perspectivesPanelView == null) {
            perspectivesPanelView = new PerspectivesPanelViewImpl(this, eventBus);
        }
        return perspectivesPanelView;
    }

    public NavigationViewFactory getNavigationViewFactory() {
        if (navigationViewFactory == null) {
            navigationViewFactory = new NavigationViewFactoryImpl(this);
        }
        return navigationViewFactory;
    }

    public ConfigurationServiceAsync getConfigurationService() {
        return GWT.create(ConfigurationService.class);
    }

    public MultiActivityManager getActivityManager() {
        return new MultiActivityManager(this, eventBus);
    }

    public GuvnorActivityMapper getActivityMapper() {
        return new GuvnorActivityMapper(this, eventBus);
    }

    public PlaceHistoryHandler getPlaceHistoryHandler() {
        if (placeHistoryHandler == null) {
            placeHistoryHandler = new PlaceHistoryHandler(getPlaceHistoryMapper());
        }
        return placeHistoryHandler;
    }

    public GuvnorPlaceHistoryMapper getPlaceHistoryMapper() {
        if (guvnorPlaceHistoryMapper == null) {
            guvnorPlaceHistoryMapper = GWT.create(GuvnorPlaceHistoryMapper.class);
        }
        return guvnorPlaceHistoryMapper;
    }

    public ModuleEditorActivityView getModuleEditorActivityView() {
        return new ModuleEditorActivityViewImpl();
    }

    public AssetViewerActivityView getAssetViewerActivityView() {
        return new AssetViewerActivityViewImpl();
    }

    public PackageServiceAsync getPackageService() {
        return RepositoryServiceFactory.getPackageService();
    }

    public AssetEditorFactory getAssetEditorFactory() {
        if (assetEditorFactory == null) {
            assetEditorFactory = GWT.create(AssetEditorFactory.class);
        }
        return assetEditorFactory;
    }

    public RepositoryServiceAsync getRepositoryService() {
        return RepositoryServiceFactory.getService();
    }

    public CategoryServiceAsync getCategoryService() {
        return RepositoryServiceFactory.getCategoryService();
    }

    public AssetServiceAsync getAssetService() {
        return RepositoryServiceFactory.getAssetService();
    }
}
