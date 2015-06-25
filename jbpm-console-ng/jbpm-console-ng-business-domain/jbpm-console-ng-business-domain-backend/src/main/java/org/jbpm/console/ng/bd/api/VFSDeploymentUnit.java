/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.console.ng.bd.api;

import org.jbpm.services.api.model.DeploymentUnit;
import org.kie.internal.runtime.conf.RuntimeStrategy;

public class VFSDeploymentUnit implements DeploymentUnit {

    private String identifier;
    private String repositoryScheme = "default";
    private String repositoryAlias;
    private String repositoryFolder;
    
    private RuntimeStrategy strategy = RuntimeStrategy.SINGLETON;
    
    public VFSDeploymentUnit(String identifier, String repositoryAlias, String repositoryFolder) {
        this.identifier = identifier;
        this.repositoryAlias = repositoryAlias;
        this.repositoryFolder = repositoryFolder;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    public String getRepositoryAlias() {
        return repositoryAlias;
    }

    public void setRepositoryAlias(String repositoryAlias) {
        this.repositoryAlias = repositoryAlias;
    }

    public String getRepositoryFolder() {
        return repositoryFolder;
    }

    public void setRepositoryFolder(String repositoryFolder) {
        this.repositoryFolder = repositoryFolder;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public RuntimeStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(RuntimeStrategy strategy) {
        this.strategy = strategy;
    }
    
    public String getRepository() {
        if (repositoryAlias!= null && !repositoryAlias.equals("")) {
            String repo = this.repositoryScheme + "://" + this.repositoryAlias;
            if (!this.repositoryFolder.startsWith("/")) {
                repo +="/";
            }
            return repo;
        }
        return "";
    }

    public String getRepositoryScheme() {
        return repositoryScheme;
    }

    public void setRepositoryScheme(String repositoryScheme) {
        this.repositoryScheme = repositoryScheme;
    }

}
