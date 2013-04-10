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

package org.kie.guvnor.builder;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;

import org.kie.commons.io.IOService;
import org.kie.commons.io.impl.IOServiceDotFileImpl;
import org.kie.guvnor.project.service.KModuleService;
import org.uberfire.backend.repositories.Repository;
import org.uberfire.backend.server.repositories.DefaultSystemRepository;

import static org.mockito.Mockito.*;

@Singleton
public class TestAppSetup {

    private final IOService ioService = new IOServiceDotFileImpl();
    private final DefaultSystemRepository systemRepository = new DefaultSystemRepository();

    @PostConstruct
    public void onStartup() {
    }

    @Produces
    @Named("ioStrategy")
    public IOService makeIOService() {
        return ioService;
    }

    @Produces
    @Named("system")
    public Repository systemRepository() {
        return systemRepository;
    }

    @Produces
    public KModuleService makeKModuleService() {
        return mock( KModuleService.class );
    }

}
