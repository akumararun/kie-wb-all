package org.kie.remote.client.documentation;

import java.net.URL;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.remote.client.api.RemoteRestRuntimeEngineBuilder;
import org.kie.remote.client.api.RemoteRestRuntimeEngineFactory;
import org.kie.remote.client.api.RemoteRuntimeEngineFactory;

//TODO: changed, add to documentation
public class DocumentationBuilderExamples {

    private static final String KRIS_USER = "kris";
    private static final String KRIS_PASSWORD = "kris123@";
    private static final String MARY_USER = "mary";
    private static final String MARY_PASSWORD = "mary123@";
    private static final String JOHN_USER = "john";
    private static final String JOHN_PASSWORD = "john123@";

    public void multipleDifferentRuntimeExamples(String deploymentId, URL deploymentUrl, boolean useFormBasedAuth) throws Exception {
        RemoteRestRuntimeEngineBuilder runtimeEngineBuilder = RemoteRuntimeEngineFactory.newRestBuilder()
                .addDeploymentId(deploymentId)
                .addUrl(deploymentUrl);

        RuntimeEngine krisRemoteEngine = runtimeEngineBuilder
                .addUserName(KRIS_USER)
                .addPassword(KRIS_PASSWORD)
                .build();
        RuntimeEngine maryRemoteEngine = runtimeEngineBuilder
                .addUserName(MARY_USER)
                .addPassword(MARY_PASSWORD)
                .build();
        RuntimeEngine johnRemoteEngine = runtimeEngineBuilder
                .addUserName(JOHN_USER)
                .addPassword(JOHN_PASSWORD)
                .build();
    }
   
    // TODO
    public void jmsBuilderExamples() { 
        
    }
}
