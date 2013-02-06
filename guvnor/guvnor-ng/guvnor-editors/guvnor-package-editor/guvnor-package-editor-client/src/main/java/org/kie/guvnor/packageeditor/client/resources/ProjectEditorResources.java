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

package org.kie.guvnor.packageeditor.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import org.kie.guvnor.packageeditor.client.resources.css.ProjectEditorCss;

public interface ProjectEditorResources
        extends
        ClientBundle {

    public ProjectEditorResources INSTANCE = GWT.create(ProjectEditorResources.class);


    @Source("css/ProjectEditor.css")
    public ProjectEditorCss mainCss();

    @Source("images/error.gif")
    ImageResource Error();

    @Source("images/warning.gif")
    ImageResource Warning();

    @Source("images/information.gif")
    ImageResource Information();
}
