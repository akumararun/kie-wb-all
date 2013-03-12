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
<%@ taglib prefix="panel" uri="bui_taglib.tld" %>
<%@ taglib uri="factory.tld" prefix="factory" %>
<html lang="<factory:property bean="org.jboss.dashboard.LocaleManager" property="currentLang"/>">
<head>
    <panel:envelopeHead/>
    <title><panel:propertyRead localize="true" object="workspace" property="title"/> - <panel:propertyRead localize="true" object="section" property="title"/></title>
    <meta http-equiv="Page-Enter"
          content="progid:DXImageTransform.Microsoft.Zigzag(duration=0)">
        <meta http-equiv="Page-Exit"
          content="progid:DXImageTransform.Microsoft.Zigzag(duration=0)">

</head>
<body>
<panel:envelopeContent/>
<panel:envelopeFooter/>
</body>
</html>