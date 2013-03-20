<%--

    Copyright (C) 2012 JBoss Inc

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@ page import="org.apache.commons.lang.StringUtils"%>
<%@ page import="org.jbpm.formModeler.core.processing.fieldHandlers.NumericFieldHandler"%>
<%@ page import="java.util.Map" %>
<%@ taglib uri="mvc_taglib.tld" prefix="mvc" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/i18n-1.0" prefix="i18n" %>
<%try{%>
<table border="0" cellpadding="0" cellspacing="0" >
    <tr valign="top">
<mvc:formatter name="org.jbpm.formModeler.core.processing.fieldHandlers.SimpleFieldHandlerFormatter">
    <mvc:fragment name="output">
        <mvc:fragmentValue name="name" id="name">
        <mvc:fragmentValue name="title" id="title">
            <mvc:fragmentValue name="styleclass" id="styleclass">
                <mvc:fragmentValue name="size" id="size">
                    <mvc:fragmentValue name="maxlength" id="maxlength">
                        <mvc:fragmentValue name="tabindex" id="tabindex">
                            <mvc:fragmentValue name="value" id="value">
                                <mvc:fragmentValue name="accesskey" id="accesskey">
                                    <mvc:fragmentValue name="alt" id="alt">
                                        <mvc:fragmentValue name="cssStyle" id="cssStyle">
                                            <mvc:fragmentValue name="disabled" id="disabled">
                                                <mvc:fragmentValue name="height" id="height">
                                                    <mvc:fragmentValue name="readonly" id="readonly">
                                                        <mvc:fragmentValue name="lastParameterMap" id="lastParameterMap">
        <td>
                                                        <%
                                                            boolean useLastParams = lastParameterMap != null && !((Map)lastParameterMap).isEmpty();
                                                            String v1 = "", v2 = "";
                                                            if (useLastParams) {
                                                                Object from = ((Map)lastParameterMap).get(name+NumericFieldHandler.NUMERIC_FROM_SUFFIX);
                                                                if (from != null && ((String[])from)[0] != null) {
                                                                    v1 = ((String[])from)[0];
                                                                }
                                                                Object to = ((Map)lastParameterMap).get(name+ NumericFieldHandler.NUMERIC_TO_SUFFIX);
                                                                if (to != null && ((String[])to)[0] != null) {
                                                                    v2 = ((String[])to)[0];
                                                                }
                                                            } else {
                                                                if (value instanceof Object[]) {
                                                                    if (((Object[])value)[0]!=null) v1 = String.valueOf(((Object[])value)[0]);
                                                                    if (((Object[])value)[1]!=null) v2 = String.valueOf(((Object[])value)[1]);
                                                                } else if (value instanceof Object && value!=null) {
                                                                    v1 = v2 = String.valueOf(value);
                                                                }
                                                            }
                                                        %>
                                                        <input  name="<%=name%><%=NumericFieldHandler.NUMERIC_FROM_SUFFIX%>"  id="<mvc:fragmentValue name="uid"/><%=NumericFieldHandler.NUMERIC_FROM_SUFFIX%>"
                                                            onchange="processFormInputChange(this)"
                                                            <%=title!=null?("title=\""+title+"\""):""%>
                                                            <%=styleclass!=null && ((String)styleclass).trim().length()>0 ? " class=\""+styleclass+"\"":"class=\"skn-input\""%>
                                                            <%=size!=null ? " size=\""+size+"\"":""%>
                                                            <%=maxlength!=null ? " maxlength=\""+maxlength+"\"":""%>
                                                            <%=tabindex!=null ? " tabindex=\""+tabindex+"\"":""%>
                                                            <%=accesskey!=null ? " accesskey=\""+accesskey+"\"":""%>
                                                            <%=alt!=null ? " alt=\""+alt+"\"":""%>
                                                            <%=cssStyle!=null ? " style=\""+cssStyle+"\"":""%>
                                                            <%=height!=null ? " height=\""+height+"\"":""%>
                                                            <%=readonly!=null && ((Boolean)readonly).booleanValue()? " readonly ":""%>
                                                            <%=disabled!=null && ((Boolean)disabled).booleanValue()? " disabled ":""%>
                                                                value="<%= StringEscapeUtils.escapeHtml(v1) %>">
        </td>
        <td>
                                                        &nbsp;-&nbsp;
        </td>
        <td>
                                                        <input  name="<%=name%><%=NumericFieldHandler.NUMERIC_TO_SUFFIX%>"  id="<mvc:fragmentValue name="uid"/><%=NumericFieldHandler.NUMERIC_TO_SUFFIX%>"
                                                            onchange="processFormInputChange(this)"
                                                            <%=title!=null?("title=\""+title+"\""):""%>
                                                            <%=styleclass!=null && ((String)styleclass).trim().length()>0 ? " class=\""+styleclass+"\"":"class=\"skn-input\""%>
                                                            <%=size!=null ? " size=\""+size+"\"":""%>
                                                            <%=maxlength!=null ? " maxlength=\""+maxlength+"\"":""%>
                                                            <%=tabindex!=null ? " tabindex=\""+tabindex+"\"":""%>
                                                            <%=accesskey!=null ? " accesskey=\""+accesskey+"\"":""%>
                                                            <%=alt!=null ? " alt=\""+alt+"\"":""%>
                                                            <%=cssStyle!=null ? " style=\""+cssStyle+"\"":""%>
                                                            <%=height!=null ? " height=\""+height+"\"":""%>
                                                            <%=readonly!=null && ((Boolean)readonly).booleanValue()? " readonly ":""%>
                                                            <%=disabled!=null && ((Boolean)disabled).booleanValue()? " disabled ":""%>
                                                                value="<%= StringEscapeUtils.escapeHtml(v2) %>">
        </td>
                                                        </mvc:fragmentValue>
                                                    </mvc:fragmentValue>
                                                </mvc:fragmentValue>
                                            </mvc:fragmentValue>
                                        </mvc:fragmentValue>
                                    </mvc:fragmentValue>
                                </mvc:fragmentValue>
                            </mvc:fragmentValue>
                        </mvc:fragmentValue>
                    </mvc:fragmentValue>
                </mvc:fragmentValue>
            </mvc:fragmentValue>
        </mvc:fragmentValue>
        </mvc:fragmentValue>
    </mvc:fragment>
</mvc:formatter>
    </tr>
</table>
<%}catch(Throwable t){System.out.println("Error showing Text input "+t);t.printStackTrace();}%>
