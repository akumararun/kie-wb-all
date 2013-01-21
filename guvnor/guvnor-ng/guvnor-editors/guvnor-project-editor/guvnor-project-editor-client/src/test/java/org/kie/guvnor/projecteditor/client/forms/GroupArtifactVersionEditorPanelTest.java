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

import com.google.gwt.user.client.Command;
import org.junit.Before;
import org.junit.Test;
import org.kie.guvnor.project.model.GroupArtifactVersionModel;
import org.mockito.ArgumentCaptor;
import org.uberfire.backend.vfs.Path;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GroupArtifactVersionEditorPanelTest {

    private GroupArtifactVersionEditorPanelView view;
    private GroupArtifactVersionEditorPanel panel;
    private MockProjectEditorServiceCaller projectEditorServiceCaller;
    private MockProjectServiceCaller projectServiceCaller;

    @Before
    public void setUp() throws Exception {
        view = mock(GroupArtifactVersionEditorPanelView.class);
        projectEditorServiceCaller = new MockProjectEditorServiceCaller();
        projectServiceCaller = new MockProjectServiceCaller();
        panel = new GroupArtifactVersionEditorPanel(projectEditorServiceCaller, projectServiceCaller, view);
    }

    @Test
    public void testLoad() throws Exception {
        GroupArtifactVersionModel gavModel = createTestModel("group", "artifact", "1.1.1");
        projectServiceCaller.setGav(gavModel);
        Path path = mock(Path.class);
        panel.init(path);

        verify(view).setGAV(gavModel);
        verify(view).setTitleText("artifact");
        verify(view).setDependencies(gavModel.getDependencies());
    }

    @Test
    public void testSave() throws Exception {
        GroupArtifactVersionModel gavModel = createTestModel("my.group", "my.artifact", "1.0-SNAPSHOT");
        projectServiceCaller.setGav(gavModel);
        Path path = mock(Path.class);
        panel.init(path);

        verify(view).setGAV(gavModel);
        verify(view).setTitleText("my.artifact");

        gavModel.setGroupId("group2");
        gavModel.setArtifactId("artifact2");

        ArgumentCaptor<ArtifactIdChangeHandler> captor = ArgumentCaptor.forClass(ArtifactIdChangeHandler.class);
        verify(view).addArtifactIdChangeHandler(captor.capture());
        gavModel.setVersion("2.2.2");
        captor.getValue().onChange("2.2.2");

        verify(view).setTitleText("2.2.2");

        panel.save(new Command() {
            @Override
            public void execute() {
                //TODO -Rikkola-
            }
        });

        GroupArtifactVersionModel savedGav = projectEditorServiceCaller.getSavedGav();
        assertEquals("group2", savedGav.getGroupId());
        assertEquals("artifact2", savedGav.getArtifactId());
        assertEquals("2.2.2", savedGav.getVersion());

        verify(view).showSaveSuccessful("pom.xml");
    }

    private GroupArtifactVersionModel createTestModel(String group, String artifact, String version) {
        GroupArtifactVersionModel gavModel = new GroupArtifactVersionModel();
        gavModel.setGroupId(group);
        gavModel.setArtifactId(artifact);
        gavModel.setVersion(version);
        return gavModel;
    }
}
