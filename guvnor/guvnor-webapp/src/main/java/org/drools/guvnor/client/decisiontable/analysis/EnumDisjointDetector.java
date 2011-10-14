/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.client.decisiontable.analysis;

import java.util.ArrayList;
import java.util.List;

public class EnumDisjointDetector extends DisjointDetector<EnumDisjointDetector> {

    private final List<String> allowedValueList = new ArrayList<String>();

    public EnumDisjointDetector(String[] allValueList, String value, String operator) {
        if (operator.equals("==")) {
            allowedValueList.add(value);
        } else if (operator.equals("!=")) {
            for (String allValue : allValueList) {
                if (!value.equals(allValue)) {
                    allowedValueList.add(value);
                }
            }
        } else {
            throw new IllegalArgumentException("The operator (" + operator + ") is not supported.");
        }
    }

    public void merge(EnumDisjointDetector other) {
        allowedValueList.retainAll(other.allowedValueList);
        if (allowedValueList.isEmpty()) {
            impossibleMatch = true;
        }
    }

}
