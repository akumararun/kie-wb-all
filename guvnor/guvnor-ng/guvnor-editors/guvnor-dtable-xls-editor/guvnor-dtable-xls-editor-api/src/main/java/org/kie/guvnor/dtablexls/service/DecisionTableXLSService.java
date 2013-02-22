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

package org.kie.guvnor.dtablexls.service;

import java.io.InputStream;
import java.io.OutputStream;

import org.jboss.errai.bus.server.annotations.Remote;
import org.kie.guvnor.commons.service.file.FileService;
import org.kie.guvnor.commons.service.validation.ValidationService;
import org.kie.guvnor.commons.service.verification.SimpleVerificationService;
import org.uberfire.backend.vfs.Path;

@Remote
public interface DecisionTableXLSService
        extends FileService<String>,
                ValidationService<String>,
                SimpleVerificationService<String> {

    InputStream load( final Path path );

    OutputStream save( Path path );
}
