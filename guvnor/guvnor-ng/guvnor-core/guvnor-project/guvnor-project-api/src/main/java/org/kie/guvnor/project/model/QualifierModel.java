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

package org.kie.guvnor.project.model;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.util.HashMap;
import java.util.Map;

@Portable
public class QualifierModel {

    private boolean simple = false;
    private String type;
    private String value;
    private Map<String, String> arguments = new HashMap<String, String>();

    public QualifierModel() {
    }

    public QualifierModel(String type) {
        this.type = type;
    }

    public boolean isSimple() {
        return simple;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void addArgument(String key, String value) {
        arguments.put(key, value);
    }
}
