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

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.kie.guvnor.project.model.POM;
import org.kie.guvnor.project.service.POMService;
import org.kie.guvnor.services.metadata.model.Metadata;
import org.uberfire.backend.vfs.Path;

public class MockPomServiceCaller
        implements Caller<POMService> {

    private POM gavModel;
    private final POMService service;
    private RemoteCallback callback;
    private POM pomModel;

    public MockPomServiceCaller() {
        service = new POMService() {


            @Override
            public POM loadPOM(Path path) {
                callback.callback(gavModel);
                return gavModel;
            }

            @Override
            public Path savePOM(String commitMessage, Path pathToPOM, POM pomModel, Metadata metadata) {
                MockPomServiceCaller.this.pomModel=pomModel;
                callback.callback(pathToPOM);
                return pathToPOM;
            }

            @Override
            public Path savePOM(Path pathToPOM, POM pomModel) {
                MockPomServiceCaller.this.pomModel = pomModel;
                callback.callback(pathToPOM);
                return pathToPOM;
            }

        };
    }

    @Override
    public POMService call(RemoteCallback<?> remoteCallback) {
        callback = remoteCallback;
        return service;
    }

    @Override
    public POMService call(RemoteCallback<?> remoteCallback,
                           ErrorCallback errorCallback) {
        callback = remoteCallback;
        return service;
    }

    public void setGav(POM gavModel) {
        this.gavModel = gavModel;
    }

    public POM getSavedPOM() {
        return pomModel;
    }
}
