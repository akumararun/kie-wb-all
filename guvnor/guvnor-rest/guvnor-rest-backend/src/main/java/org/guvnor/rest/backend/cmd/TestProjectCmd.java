/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.guvnor.rest.backend.cmd;

import org.guvnor.rest.backend.JobRequestHelper;
import org.guvnor.rest.client.JobRequest;
import org.guvnor.rest.client.JobResult;
import org.guvnor.rest.client.JobStatus;
import org.guvnor.rest.client.TestProjectRequest;
import org.kie.api.executor.CommandContext;

public class TestProjectCmd extends AbstractJobCommand {

    @Override
    public JobResult internalExecute(CommandContext ctx, JobRequest request) throws Exception {
        JobRequestHelper helper = getHelper(ctx);
        TestProjectRequest jobRequest = (TestProjectRequest) request;

        JobResult result = null;
        try { 
            result = helper.testProject( jobRequest.getJobId(), jobRequest.getRepositoryName(), jobRequest.getProjectName(), jobRequest.getBuildConfig() );
        } finally { 
            JobStatus status = result != null ? result.getStatus() : JobStatus.SERVER_ERROR;
            logger.debug( "-----testProject--- , repositoryName: {}, project name: {} [{}]",
                    jobRequest.getRepositoryName(), jobRequest.getProjectName(), status);
        }
        return result;
    }
}
