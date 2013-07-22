
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

package org.kie.workbench.common.services.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.guvnor.common.services.project.builder.service.BuildService;
import org.guvnor.common.services.project.service.ProjectService;
import org.jboss.resteasy.annotations.GZIP;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.file.FileSystem;
import org.kie.workbench.common.services.shared.rest.AddRepositoryToGroupRequest;
import org.kie.workbench.common.services.shared.rest.BuildConfig;
import org.kie.workbench.common.services.shared.rest.CompileProjectRequest;
import org.kie.workbench.common.services.shared.rest.CreateGroupRequest;
import org.kie.workbench.common.services.shared.rest.CreateOrCloneRepositoryRequest;
import org.kie.workbench.common.services.shared.rest.CreateProjectRequest;
import org.kie.workbench.common.services.shared.rest.DeployProjectRequest;
import org.kie.workbench.common.services.shared.rest.Entity;
import org.kie.workbench.common.services.shared.rest.Group;
import org.kie.workbench.common.services.shared.rest.InstallProjectRequest;
import org.kie.workbench.common.services.shared.rest.JobRequest;
import org.kie.workbench.common.services.shared.rest.JobResult;
import org.kie.workbench.common.services.shared.rest.RemoveRepositoryFromGroupRequest;
import org.kie.workbench.common.services.shared.rest.RemoveRepositoryRequest;
import org.kie.workbench.common.services.shared.rest.Repository;
import org.kie.workbench.common.services.shared.rest.TestProjectRequest;
import org.uberfire.backend.group.GroupService;
import org.uberfire.backend.repositories.RepositoryService;
import org.uberfire.backend.server.util.Paths;

@Path("/")
@Named
@GZIP
@ApplicationScoped
public class ProjectResource {
    @Context
    protected UriInfo uriInfo;

    @Inject
    protected ProjectService projectService;

    @Inject
    protected BuildService buildService;

//    @Inject
//    protected ScenarioTestEditorService scenarioTestEditorService;

    @Inject
    private Paths paths;

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    GroupService groupService;

    @Inject
    RepositoryService repositoryService;

	private static class Cache extends LinkedHashMap<String, JobResult> {
		private int maxSize = 1000;

		public Cache(int maxSize) {
			this.maxSize = maxSize;
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, JobResult> stringFutureEntry) {
			return size() > maxSize;
		}

		public void setMaxSize(int maxSize) {
			this.maxSize = maxSize;
		}
	}
    private Cache cache;
	private Map<String, JobResult> jobs;
    private AtomicLong counter = new AtomicLong(0);
   
    private int maxCacheSize = 10000;
    
    @Inject
    private Event<CreateOrCloneRepositoryRequest> createOrCloneJobRequestEvent;     
    @Inject
    private Event<RemoveRepositoryRequest> removeRepositoryRequestEvent;     
    @Inject
    private Event<CreateProjectRequest> createProjectRequestEvent; 
    @Inject
    private Event<CompileProjectRequest> compileProjectRequestEvent; 
    @Inject
    private Event<InstallProjectRequest> installProjectRequestEvent; 
    @Inject
    private Event<TestProjectRequest> testProjectRequestEvent; 
    @Inject
    private Event<DeployProjectRequest> deployProjectRequestEvent; 
    @Inject
    private Event<CreateGroupRequest> createGroupRequestEvent; 
    @Inject
    private Event<AddRepositoryToGroupRequest> addRepositoryToGroupRequest; 
    @Inject
    private Event<RemoveRepositoryFromGroupRequest> removeRepositoryFromGroupRequest; 

    @PostConstruct
    public void start() {
    	cache = new Cache(maxCacheSize);
    	jobs = Collections.synchronizedMap(cache);
    }
    
    public void onUpateJobStatus( final @Observes JobResult jobResult ) {
    	JobResult job = jobs.get(jobResult.getJodId());

        if (job == null) {
            //the job has gone probably because its done and has been removed.
            System.out.println( "-----onUpateJobStatus--- , can not find jobId:" + jobResult.getJodId() + ", the job has gone probably because its done and has been removed.");
        	return;
        }

        jobResult.setLastModified(System.currentTimeMillis());
        jobs.put(jobResult.getJodId(), jobResult);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/jobs/{jobId}")
    public JobResult getJobStatus( @PathParam("jobId") String jobId ) {
        System.out.println( "-----getJobStatus--- , jobId:" + jobId );
        
        JobResult job = jobs.get(jobId);

        if (job == null) {
            //the job has gone probably because its done and has been removed.
            System.out.println( "-----getJobStatus--- , can not find jobId:" + jobId + ", the job has gone probably because its done and has been removed.");
        	job = new JobResult();
        	job.setStatus(JobRequest.Status.GONE);
        	return job;
        }

        return job;
    }
    
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/jobs/{jobId}")
    public JobResult removeJob( @PathParam("jobId") String jobId ) {
        System.out.println( "-----removeJob--- , jobId:" + jobId );
        
        JobResult job = jobs.get(jobId);

        if (job == null) {
            //the job has gone probably because its done and has been removed.
            System.out.println( "-----removeJob--- , can not find jobId:" + jobId + ", the job has gone probably because its done and has been removed.");
        	job = new JobResult();
        	job.setStatus(JobRequest.Status.GONE);
        	return job;
        }

        jobs.remove(jobId);
        job.setStatus(JobRequest.Status.GONE);
        return job;
    }
    
    //TODO: Stop or cancel a job
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("repositories")
    public JobRequest createOrCloneRepository( Repository repository ) {
        System.out.println( "-----createOrCloneRepository--- , repository name:" + repository.getName() );

        String id = "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        CreateOrCloneRepositoryRequest jobRequest = new CreateOrCloneRepositoryRequest();
        jobRequest.setStatus(JobRequest.Status.ACCEPTED);
        jobRequest.setJodId(id);
        jobRequest.setRepository(repository);
        
        JobResult jobResult = new JobResult();
        jobResult.setJodId(id);
        jobResult.setStatus(JobRequest.Status.ACCEPTED);
        jobs.put(id, jobResult);
        
        createOrCloneJobRequestEvent.fire(jobRequest);
        
        return jobRequest;   
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("repositories")
    public Collection<org.uberfire.backend.repositories.Repository> getRepositories() {
        System.out.println( "-----getRepositories--- " );

        return repositoryService.getRepositories(); 
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("repositories/{repositoryName}")
    public JobRequest removeRepository(
            @PathParam("repositoryName") String repositoryName ) {
        System.out.println( "-----removeRepository--- , repositoryName:" + repositoryName );

        String id = "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        
        RemoveRepositoryRequest jobRequest = new RemoveRepositoryRequest();
        jobRequest.setStatus(JobRequest.Status.ACCEPTED);
        jobRequest.setJodId(id);
        jobRequest.setRepositoryName(repositoryName);
        
        JobResult jobResult = new JobResult();
        jobResult.setJodId(id);
        jobResult.setStatus(JobRequest.Status.ACCEPTED);
        jobs.put(id, jobResult);
        
        removeRepositoryRequestEvent.fire(jobRequest);

        return jobRequest;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("repositories/{repositoryName}/projects")
    public JobRequest createProject(
            @PathParam("repositoryName") String repositoryName,
            Entity project ) {
        System.out.println( "-----createProject--- , repositoryName:" + repositoryName + ", project name:" + project.getName() );

        String id = "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        CreateProjectRequest jobRequest = new CreateProjectRequest();
        jobRequest.setStatus(JobRequest.Status.ACCEPTED);
        jobRequest.setJodId(id);
        jobRequest.setRepositoryName(repositoryName);
        jobRequest.setProjectName(project.getName());
        jobRequest.setDescription(project.getDescription());
        
        JobResult jobResult = new JobResult();
        jobResult.setJodId(id);
        jobResult.setStatus(JobRequest.Status.ACCEPTED);
        jobs.put(id, jobResult);
        
        createProjectRequestEvent.fire(jobRequest);
        
        return jobRequest;
    }
    
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("repositories/{repositoryName}/projects/{projectName}")
    public JobRequest deleteProject(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("projectName") String projectName ) {
        System.out.println( "-----deleteProject--- , repositoryName:" + repositoryName + ", project name:" + projectName );

        throw new WebApplicationException(Response.status(Response.Status.NOT_ACCEPTABLE)
                .entity("UNIMPLEMENTED").build());
        
/*        String id = "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        CreateProjectRequest jobRequest = new CreateProjectRequest();
        jobRequest.setStatus(JobRequest.Status.ACCEPTED);
        jobRequest.setJodId(id);
        jobRequest.setRepositoryName(repositoryName);
        jobRequest.setProjectName(projectName);
        
        JobResult jobResult = new JobResult();
        jobResult.setJodId(id);
        jobResult.setStatus(JobRequest.Status.ACCEPTED);
        jobs.put(id, jobResult);
        
        //TODO: Delete project. ProjectService does not have a removeProject method yet.
        //createProjectRequestEvent.fire(jobRequest);
        
        return jobRequest;*/
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("repositories/{repositoryName}/projects/{projectName}/maven/compile")
    public JobRequest compileProject(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("projectName") String projectName,
            BuildConfig mavenConfig ) {
        System.out.println( "-----compileProject--- , repositoryName:" + repositoryName + ", project name:" + projectName );

        String id = "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        CompileProjectRequest jobRequest = new CompileProjectRequest();
        jobRequest.setStatus(JobRequest.Status.ACCEPTED);
        jobRequest.setJodId(id);
        jobRequest.setRepositoryName(repositoryName);
        jobRequest.setProjectName(projectName);
        jobRequest.setBuildConfig(mavenConfig);

        JobResult jobResult = new JobResult();
        jobResult.setJodId(id);
        jobResult.setStatus(JobRequest.Status.ACCEPTED);
        jobs.put(id, jobResult);
        
        compileProjectRequestEvent.fire(jobRequest);
        
        return jobRequest;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("repositories/{repositoryName}/projects/{projectName}/maven/install")
    public JobRequest installProject(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("projectName") String projectName,
            BuildConfig mavenConfig ) {
        System.out.println( "-----installProject--- , repositoryName:" + repositoryName + ", project name:" + projectName );

        String id = "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        InstallProjectRequest jobRequest = new InstallProjectRequest();
        jobRequest.setStatus(JobRequest.Status.ACCEPTED);
        jobRequest.setJodId(id);
        jobRequest.setRepositoryName(repositoryName);
        jobRequest.setProjectName(projectName);
        jobRequest.setBuildConfig(mavenConfig);
        
        JobResult jobResult = new JobResult();
        jobResult.setJodId(id);
        jobResult.setStatus(JobRequest.Status.ACCEPTED);
        jobs.put(id, jobResult);
        
        installProjectRequestEvent.fire(jobRequest);
        
        return jobRequest;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("repositories/{repositoryName}/projects/{projectName}/maven/test")
    public JobRequest testProject(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("projectName") String projectName,
            BuildConfig mavenConfig ) {
        System.out.println( "-----testProject--- , repositoryName:" + repositoryName + ", project name:" + projectName );

        String id = "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        TestProjectRequest jobRequest = new TestProjectRequest();
        jobRequest.setStatus(JobRequest.Status.ACCEPTED);
        jobRequest.setJodId(id);
        jobRequest.setRepositoryName(repositoryName);
        jobRequest.setProjectName(projectName);
        jobRequest.setBuildConfig(mavenConfig);
        
        JobResult jobResult = new JobResult();
        jobResult.setJodId(id);
        jobResult.setStatus(JobRequest.Status.ACCEPTED);
        jobs.put(id, jobResult);
        
        testProjectRequestEvent.fire(jobRequest);
        
        return jobRequest;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("repositories/{repositoryName}/projects/{projectName}/maven/deploy")
    public JobRequest deployProject(
            @PathParam("repositoryName") String repositoryName,
            @PathParam("projectName") String projectName,
            BuildConfig mavenConfig ) {        
        System.out.println( "-----deployProject--- , repositoryName:" + repositoryName + ", project name:" + projectName );

        String id = "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        DeployProjectRequest jobRequest = new DeployProjectRequest();
        jobRequest.setStatus(JobRequest.Status.ACCEPTED);
        jobRequest.setJodId(id);
        jobRequest.setRepositoryName(repositoryName);
        jobRequest.setProjectName(projectName);
        jobRequest.setBuildConfig(mavenConfig);
        
        JobResult jobResult = new JobResult();
        jobResult.setJodId(id);
        jobResult.setStatus(JobRequest.Status.ACCEPTED);
        jobs.put(id, jobResult);
        
        deployProjectRequestEvent.fire(jobRequest);
        
        return jobRequest;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups")
    public JobRequest createGroup( Group group ) {
        System.out.println( "-----createGroup--- , Group name:" + group.getName() + ", Group owner:" + group.getOwner() );

        String id = "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        CreateGroupRequest jobRequest = new CreateGroupRequest();
        jobRequest.setStatus(JobRequest.Status.ACCEPTED);
        jobRequest.setJodId(id);
        jobRequest.setGroupName(group.getName());
        jobRequest.setOwnder(group.getOwner());
        jobRequest.setRepositories(group.getRepositories());
        
        JobResult jobResult = new JobResult();
        jobResult.setJodId(id);
        jobResult.setStatus(JobRequest.Status.ACCEPTED);
        jobs.put(id, jobResult);
        
        createGroupRequestEvent.fire(jobRequest);
        
        return jobRequest;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups/{groupName}/repositories/{repositoryName}")
    public JobRequest addRepositoryToGroup( @PathParam("groupName") String groupName, @PathParam("repositoryName") String repositoryName) {
        System.out.println( "-----addRepositoryToGroup--- , Group name:" + groupName + ", Repository name:" + repositoryName );

        String id = "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        AddRepositoryToGroupRequest jobRequest = new AddRepositoryToGroupRequest();
        jobRequest.setStatus(JobRequest.Status.ACCEPTED);
        jobRequest.setJodId(id);
        jobRequest.setGroupName(groupName);
        jobRequest.setRepositoryName(repositoryName);
        
        JobResult jobResult = new JobResult();
        jobResult.setJodId(id);
        jobResult.setStatus(JobRequest.Status.ACCEPTED);
        jobs.put(id, jobResult);
        
        addRepositoryToGroupRequest.fire(jobRequest);
        
        return jobRequest;
    }
    
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups/{groupName}/repositories/{repositoryName}")
    public JobRequest removeRepositoryFromGroup( @PathParam("groupName") String groupName,  @PathParam("repositoryName") String repositoryName) {
        System.out.println( "-----removeRepositoryFromGroup--- , Group name:" + groupName + ", Repository name:" + repositoryName );

        String id = "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        RemoveRepositoryFromGroupRequest jobRequest = new RemoveRepositoryFromGroupRequest();
        jobRequest.setStatus(JobRequest.Status.ACCEPTED);
        jobRequest.setJodId(id);
        jobRequest.setGroupName(groupName);
        jobRequest.setRepositoryName(repositoryName);
        
        JobResult jobResult = new JobResult();
        jobResult.setJodId(id);
        jobResult.setStatus(JobRequest.Status.ACCEPTED);
        jobs.put(id, jobResult);
        
        removeRepositoryFromGroupRequest.fire(jobRequest);
        
        return jobRequest;
    }
    
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups/{groupName}")
    public JobRequest deleteGroup( @PathParam("groupName") String groupName ) {
        System.out.println( "-----deleteGroup--- , Group name:" + groupName );
           
        throw new WebApplicationException(Response.status(Response.Status.NOT_ACCEPTABLE)
                .entity("UNIMPLEMENTED").build());
        
        //TODO:GroupService does not have removeGroup method yet
        //groupService.removeGroup(groupName);
        //createGroupRequestEvent.fire(jobRequest);             
/*        String id = "" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
        CreateGroupRequest jobRequest = new CreateGroupRequest();
        jobRequest.setStatus(JobRequest.Status.ACCEPTED);
        jobRequest.setJodId(id);
        jobRequest.setGroupName(groupName);
        
        JobResult jobResult = new JobResult();
        jobResult.setJodId(id);
        jobResult.setStatus(JobRequest.Status.ACCEPTED);
        jobs.put(id, jobResult);
        

        
        return jobRequest;*/
    }

    public org.kie.commons.java.nio.file.Path getRepositoryRootPath( String repositoryName ) {
        org.kie.commons.java.nio.file.Path repositoryRootPath = null;

        final Iterator<FileSystem> fsIterator = ioService.getFileSystems().iterator();

        if ( fsIterator.hasNext() ) {
            final FileSystem fileSystem = fsIterator.next();
            System.out.println( "-----FileSystem id--- :" + ( (org.kie.commons.java.nio.base.FileSystemId) fileSystem ).id() );

            if ( repositoryName.equalsIgnoreCase( ( (org.kie.commons.java.nio.base.FileSystemId) fileSystem ).id() ) ) {
                final Iterator<org.kie.commons.java.nio.file.Path> rootIterator = fileSystem.getRootDirectories().iterator();
                if ( rootIterator.hasNext() ) {
                    repositoryRootPath = rootIterator.next();
                    System.out.println( "-----rootPath--- :" + repositoryRootPath );

                    org.kie.commons.java.nio.file.DirectoryStream<org.kie.commons.java.nio.file.Path> paths = ioService
                            .newDirectoryStream( repositoryRootPath );
                    for ( final org.kie.commons.java.nio.file.Path child : paths ) {
                        System.out.println( "-----child--- :" + child );
                    }

                    return repositoryRootPath;
                }
            }
        }

        return repositoryRootPath;
    }
}





