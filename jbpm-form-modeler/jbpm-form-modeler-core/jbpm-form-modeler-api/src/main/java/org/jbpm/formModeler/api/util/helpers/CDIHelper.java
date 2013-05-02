/**
 * Copyright (C) 2012 JBoss Inc
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
package org.jbpm.formModeler.api.util.helpers;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class CDIHelper {

    /** The bean manager. */
    public static BeanManager beanManager;

    public static BeanManager getBeanManager() {
        try{

            if (beanManager == null) {
                InitialContext initialContext = new InitialContext();
                beanManager = (BeanManager) initialContext.lookup("java:comp/env/BeanManager");
            }
            return beanManager;
        } catch (NamingException e) {
            System.out.println("Couldn't get BeanManager through JNDI");
            return null;
        }

    }

    public static Object getBeanByName(String name) {
        BeanManager bm = getBeanManager();
        Bean bean = bm.getBeans(name).iterator().next();
        CreationalContext ctx = bm.createCreationalContext(bean);
        Object o = bm.getReference(bean, bean.getBeanClass(), ctx);
        return o;
    }

    public static Object getBeanByType(Class type) {
        BeanManager bm = getBeanManager();
        Bean bean = bm.getBeans(type).iterator().next();
        CreationalContext ctx = bm.createCreationalContext(bean);
        Object o = bm.getReference(bean, bean.getBeanClass(), ctx);
        return o;
    }    
}
