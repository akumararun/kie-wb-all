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

package org.kie.guvnor.projecteditor.client.forms;

import com.google.gwt.user.client.ui.IsWidget;
import org.kie.guvnor.m2repo.model.GAV;
import org.kie.guvnor.project.model.Dependency;

import java.util.List;

public interface POMEditorPanelView
        extends IsWidget {


    IsWidget getTitleWidget();

    void setTitleText(String titleText);

    void showSaveSuccessful(String fileName);

    void setDependencies(List<Dependency> dependencies);

    void setGAV(GAV gav);

    void addArtifactIdChangeHandler(ArtifactIdChangeHandler changeHandler);
}
