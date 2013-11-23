Patches
=======

This directory contains some patches required to run BPMS/BRMS applications using a custom EAP static module layer distribution.

NOTE: These patches currently applies to version 6.1.0.GA. On next releases some of them are unnecessary.

EAP patches
===========

CDI Extensions
--------------
* In EAP 6.1.0.GA the CDI Extensions declared in JARs from the BPMS layer are not loaded.
* This bug is already reported and fixed for EAP 6.1.1.
   See https://bugzilla.redhat.com/show_bug.cgi?id=988093
* The patch consist of copying the resources from jars inside <code>META-INF/services</code> directory of the webapp.

Servlet spec 3.0 - Webfragments
-------------------------------
* Is known that on both EAP 6.1.0. and 6.1.1 webfragment descriptors located inside custom static modules are not loaded.
* The patch consists on creating a new jar on runtime with the web-fragment descriptor to use as a patch. For each web-fragment descriptor a new jar is created and added into WEB-INF/lib of the webapp.
* This method allows to not modify the original deployment descriptor (web.xml) of the webapp.

Seam transactions
-----------------
Seam consists of two artifacts:
* seam-transaction-api-3.X.jar
* seam-transapction-3.X.jar

The jBPM core static module for EAP depends on seam transaction api. So, this jars should be placed in another static module, not in the webapp.
But for a unknown reason yet, when putting seam-transaction-3.X.jar outside the webapp, the transactions are not running.
The reason seems to be that the transaction interceptor defined in <code>beans.xml</code> located inside webapp, is not registered if seam-transaction-3.X.jar (impl classes) is outside webapp lib.
This interceptor is:
 <code>
 <interceptors>
      <class>org.jboss.seam.transaction.TransactionInterceptor</class>
  </interceptors>
 </code>
This behaviour should be analyzed with EAP team.

REST services
-------------
As seam transactions, if the jar containing kie remote REST services <code>kie-common-services-6-X</code> is located outside webapp lib, for example inside a EAP static module, the services are not running.
This behaviour should be analyzed with EAP team.

How to create a patch
=====================

1,- The name of the build file for the patch will be considered as the patch identifier
2.- There exist a patch lifecycle.
3.- Use the template files to create new patches.
TODO