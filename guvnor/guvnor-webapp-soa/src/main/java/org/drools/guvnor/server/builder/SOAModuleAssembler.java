/*
 * Copyright 2011 JBoss Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.drools.guvnor.server.builder;

import org.drools.guvnor.server.util.LoggingHelper;
import org.drools.repository.ModuleItem;

/**
 * This assembles SOA services into deployment bundles, and deals
 * with errors etc. Each content type is responsible for contributing to the
 * deployment bundle.
 */
public class SOAModuleAssembler extends AssemblerBase {
    private static final LoggingHelper log = LoggingHelper.getLogger(SOAModuleAssembler.class);

    private ModuleAssemblerConfiguration configuration;  

    public void init(ModuleItem moduleItem, ModuleAssemblerConfiguration moduleAssemblerConfiguration) {
        this.moduleItem = moduleItem;
        this.configuration = moduleAssemblerConfiguration;
    }

    public void compile() {
        //TO_BE_IMPLEMENTED
    }

    /**
     * This will return true if there is an error in the module configuration
     * @return
     */
    public boolean isModuleConfigurationInError() {
        return errorLogger.hasErrors() && this.errorLogger.getErrors().get(0).isModuleItem();
    }

    public byte[] getCompiledBinary() {
        //NOT_IMPLEMENTED
        return null;
    }
    public String getCompiledSource() {
        //NOT_IMPLEMENTED
        return null;
    }   
}
