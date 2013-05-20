/*
 * Copyright 2013 JBoss Inc
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

package org.kie.workbench.common.services.rest;


import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.environment.se.StartMain;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public class ProjectResourceTest {

    private BeanManager beanManager;

    @Before
    public void setUp() throws Exception {
        StartMain startMain = new StartMain( new String[ 0 ] );
        beanManager = startMain.go().getBeanManager();
    }

    @Test
    public void testCompileProject() throws Exception {

    }
    
    private <T> T getReference( Class<T> clazz ) {
        Bean bean = (Bean) beanManager.getBeans( clazz ).iterator().next();
        CreationalContext cc = beanManager.createCreationalContext( bean );
        return (T) beanManager.getReference( bean,
                                             clazz,
                                             cc );
    }
}
