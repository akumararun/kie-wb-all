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

package org.kie.guvnor.explorer.service;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;
import org.kie.guvnor.explorer.model.Item;
import org.uberfire.backend.vfs.Path;

/**
 * Service definition for Explorer editor
 */
@Remote
public interface ExplorerService {

    /**
     * Return a list of items for the specified Path. The Path can be either:-
     * - A Project Root (i.e. folder containing pom.xml and parent of folder containing kmodule.xml)
     * - Within a Project Package (i.e. folder within Project Root and src/main/resources)
     * - Within a Project but outside of a Package (i.e. between pom.xml and src/main/resources)
     * - Other
     * @param path
     * @return
     */
    List<Item> getItemsForPathScope( final Path path );

}
