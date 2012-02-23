/*
 * Copyright 2011 JBoss Inc
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

package org.drools.ide.common.server.testscenarios.populators;

import java.util.Map;

public class SimpleFieldPopulator extends FieldPopulator {

    private final Object value;

    public SimpleFieldPopulator(Object factObject, String fieldName, Object value) {
        super(factObject, fieldName);
        this.value = value;
    }

    @Override
    public void populate(Map<String, Object> populatedData) {
        populateField(value, populatedData);
    }
}
