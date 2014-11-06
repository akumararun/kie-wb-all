/*
 * Copyright 2014 JBoss Inc
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

package org.guvnor.asset.management.client.editors.repository.wizard.pages;

import org.uberfire.client.mvp.UberView;

public interface RepositoryStructurePageView
        extends
        UberView<RepositoryStructurePageView.Presenter> {

    interface Presenter {

        void stateChanged();
    }

    String getProjectName();

    String getProjectDescription();

    String getGroupId();

    String getArtifactId();

    String getVersion();

    boolean isMultiModule();

    boolean isConfigureRepository();

}
