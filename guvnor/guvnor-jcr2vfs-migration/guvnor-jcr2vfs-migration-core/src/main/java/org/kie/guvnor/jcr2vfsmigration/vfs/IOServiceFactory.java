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

package org.kie.guvnor.jcr2vfsmigration.vfs;

import java.net.URI;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;

import org.kie.commons.io.FileSystemType;
import org.kie.commons.io.IOService;
import org.kie.commons.io.impl.IOServiceDotFileImpl;
import org.kie.commons.java.nio.file.FileSystem;
import org.uberfire.backend.repositories.Repository;
import org.uberfire.backend.server.repositories.DefaultSystemRepository;

import static org.kie.guvnor.jcr2vfsmigration.vfs.IOServiceFactory.Migration.*;

@Singleton
public class IOServiceFactory {

    private final IOService ioService = new IOServiceDotFileImpl();
    private FileSystem fs;
    
    public static String  DEFAULT_MIGRATION_FILE_SYSTEM = "guvnor-jcr2vfs-migration";

    public static enum Migration implements FileSystemType {

        MIGRATION_INSTANCE;

        public String toString() {
            return "MIGRATION";
        }
    }

    @PostConstruct
    public void onStartup() {
        URI uri = URI.create( "git://" + DEFAULT_MIGRATION_FILE_SYSTEM);
        this.fs = ioService.newFileSystem( uri, new HashMap<String, Object>(), MIGRATION_INSTANCE );
    }

    @Produces
    @Named("ioStrategy")
    public IOService ioService() {
        return ioService;
    }

    @Produces
    @Named("migrationFS")
    public FileSystem migrationFS() {
        return fs;
    }
    
    @Produces
    @Named("system")
    public Repository systemRepository() {
        DefaultSystemRepository systemRepository = new DefaultSystemRepository();
        return systemRepository;
    }

}
