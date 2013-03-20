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
/**
 *
 */
package org.jbpm.formModeler.core.valueConverters;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 *
 */
public class DoubleValueConverter extends DefaultValueConverter implements ValueConverter {
    private static transient org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(DoubleValueConverter.class.getName());
    public static final String delim = ";";

    private static DecimalFormat sdf = new DecimalFormat("00000000000000000000000000000000000000000000000000.0000000000");
    private static DecimalFormat rdf = new DecimalFormat("##################################################.##########");

    public Class getAcceptedClass() {
        return Double.class;
    }

    /*
    * (non-Javadoc)
    *
    * @see org.jbpm.formModeler.core.valueConverters.ValueConverter#writeValue(java.lang.Object)
    */
    public List writeValue(Object value) {
        if (value == null) return Collections.EMPTY_LIST;
        ArrayList l = new ArrayList();
        DynValue val = new DynValue();
        val.setNumberValue(new BigDecimal((value).toString()));
        l.add(val);
        return l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jbpm.formModeler.core.valueConverters.ValueConverter#readValue(java.util.List)
     */
    public Object readValue(List values) {
        if (values == null || values.isEmpty())
            return null;
        return new Double(((DynValue) values.get(0)).getNumberValue().doubleValue());
    }

    /**
     * Given an Object belonging to an accepted class, convert it to a String
     * value representing it.
     *
     * @param value Value to convert
     * @return a String representation for this object.
     */
    public String getTextValue(Object value) {
        if (value == null) return null;
        StringBuffer sb = new StringBuffer();
        if (value.getClass().isArray()) {
            Object[] val = (Object[])value;
            for (int i = 0; i < val.length; i++) {
                if (i > 0) sb.append(delim);
                sb.append(val[i] != null ? val[i] : "-");
            }
        } else {
            sb.append(value.toString());
        }
        return sb.toString();
    }

    /**
     * Given a text value, which is a representation for an object, convert it
     * to the original object.
     *
     * @param textValue
     * @return an object read from a String value
     */
    public Object readTextValue(String textValue) {
        if (!StringUtils.isBlank(textValue)) {
            try {
                StringTokenizer strtk = new StringTokenizer(textValue, delim);
                if (strtk.countTokens() > 1) {
                    List doubles = new ArrayList(2);
                    while (strtk.hasMoreTokens()) {
                        String token = strtk.nextToken();
                        doubles.add( !("-".equals(token) || StringUtils.isBlank(token)) ? Double.valueOf(token) : null );
                    }
                    return (Double[]) doubles.toArray(new Double[doubles.size()]);
                } else {
                    return Double.valueOf(textValue);
                }
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
        return null;
    }

    /**
     * Convert a value of accepted class into a flat String value
     *
     * @param value
     * @return a String representation for this object to be used in search indexes
     */
    public String flatValue(Object value, String lang) {
        if (value != null) {
            sdf.setDecimalFormatSymbols(new DecimalFormatSymbols(new Locale(lang)));
            return sdf.format((Double) value);
        }

        return "";
    }

    /**
     * Convert a value of accepted class into a flat String value
     *
     * @param value
     * @return a String representation for this object to be used in search indexes
     */
    public String rawFlatValue(Object value, String lang) {
        if (value != null) {
            rdf.setDecimalFormatSymbols(new DecimalFormatSymbols(new Locale(lang)));
            return rdf.format((Double) value);
        }

        return "";
    }
}