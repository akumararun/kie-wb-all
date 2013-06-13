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

package org.kie.workbench.common.screens.projecteditor.client.forms;

import com.google.gwt.user.client.ui.IsWidget;
import org.kie.workbench.common.services.project.service.model.Dependency;
import org.kie.workbench.common.services.project.service.model.KModuleModel;
import org.kie.workbench.common.services.project.service.model.POM;
import org.kie.workbench.common.services.project.service.model.ProjectImports;
import org.kie.workbench.common.services.shared.metadata.model.Metadata;
import org.kie.workbench.common.widgets.client.widget.HasBusyIndicator;
import org.kie.workbench.common.widgets.configresource.client.widget.unbound.ImportsWidgetPresenter;
import org.kie.workbench.common.widgets.metadata.client.widget.MetadataWidget;

import java.util.List;

public interface ProjectScreenView
        extends HasBusyIndicator,
        IsWidget {

    interface Presenter {

        void onGAVPanelSelected();

        void onGAVMetadataPanelSelected();

        void onKBasePanelSelected();

        void onKBaseMetadataPanelSelected();

        void onImportsPanelSelected();

        void onImportsMetadataPanelSelected();

        void onDependenciesSelected();

    }
    void setPresenter(Presenter projectScreenPresenter);

    void setPOM(POM pom);

    void setDependencies(List<Dependency> dependencies);

    void setPomMetadata(Metadata pomMetaData);

    void setKModule(KModuleModel kModule);

    void setKModuleMetadata(Metadata kModuleMetaData);

    void setImports(ProjectImports projectImports);

    void setImportsMetadata(Metadata projectImportsMetadata);

    void showImportsPanel();

    void showImportsMetadataPanel();

    void showDependenciesPanel();

    void showGAVMetadataPanel();

    void showGAVPanel();

    void showKBasePanel();

    void showKBaseMetadataPanel();

}
