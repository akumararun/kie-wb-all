package org.kie.workbench.common.services.rest;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import javax.inject.Named;

import org.drools.workbench.screens.testscenario.model.Failure;
import org.drools.workbench.screens.testscenario.model.Success;
import org.drools.workbench.screens.testscenario.model.TestResultMessage;
import org.drools.workbench.screens.testscenario.service.ScenarioTestEditorService;
import org.guvnor.common.services.project.builder.model.BuildMessage;
import org.guvnor.common.services.project.builder.model.BuildResults;
import org.guvnor.common.services.project.builder.model.DeployResult;
import org.guvnor.common.services.project.builder.service.BuildService;
import org.guvnor.common.services.project.model.POM;
import org.guvnor.common.services.project.model.Project;
import org.guvnor.common.services.project.service.ProjectService;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.file.FileSystem;
import org.kie.workbench.common.services.shared.rest.BuildConfig;
import org.kie.workbench.common.services.shared.rest.Group;
import org.kie.workbench.common.services.shared.rest.JobRequest;
import org.kie.workbench.common.services.shared.rest.JobResult;
import org.kie.workbench.common.services.shared.rest.Repository;
import org.uberfire.backend.group.GroupService;
import org.uberfire.backend.repositories.RepositoryService;
import org.uberfire.backend.repositories.impl.git.GitRepository;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;

@ApplicationScoped
public class ProjectResourceDispatcher {

    @Inject
    RepositoryService repositoryService;

    @Inject
    protected ProjectService projectService;

    @Inject
    private Paths paths;

    @Inject
    @Named("ioStrategy")
    private IOService ioSystemService;

    @Inject
    protected BuildService buildService;

    @Inject
    GroupService groupService;
    
    @Inject
    private Event<JobResult> jobResultEvent;
    
    @Inject
    protected ScenarioTestEditorService scenarioTestEditorService;

    public void cloneRepository(String jobId, Repository repository) {
        System.out.println("-----KieSessionAsyncJobRequestObserver:createOrCloneRepository--- , repository name:" + repository.getName());

        JobResult result = new JobResult();
        result.setJodId(jobId);

        if (repository.getRequestType() == null || "".equals(repository.getRequestType())
                || !("new".equals(repository.getRequestType()) || ("clone".equals(repository.getRequestType())))) {
            result.setStatus(JobRequest.Status.BAD_REQUEST);
            result.setResult("Repository request type can only be new or clone.");
            jobResultEvent.fire(result);
            return;
        }

        final String scheme = "git";

        if ("new".equals(repository.getRequestType())) {
            if (repository.getName() == null || "".equals(repository.getName())) {
                result.setStatus(JobRequest.Status.BAD_REQUEST);
                result.setResult("Repository name must be provided");
                jobResultEvent.fire(result);
                return;
            }

            // username and password are optional
            final Map<String, Object> env = new HashMap<String, Object>(3);
            env.put("username", repository.getUserName());
            env.put("crypt:password", repository.getPassword());
            env.put("init", true);

            repositoryService.createRepository(scheme, repository.getName(), env);

        } else if ("clone".equals(repository.getRequestType())) {
            if (repository.getName() == null || "".equals(repository.getName()) || repository.getGitURL() == null
                    || "".equals(repository.getGitURL())) {
                result.setStatus(JobRequest.Status.BAD_REQUEST);
                result.setResult("Repository name and GitURL must be provided");
            }

            final Map<String, Object> env = new HashMap<String, Object>(3);
            env.put("username", repository.getUserName());
            env.put("crypt:password", repository.getPassword());
            env.put("origin", repository.getGitURL());

            repositoryService.createRepository(scheme, repository.getName(), env);
        }

        result.setStatus(JobRequest.Status.SUCCESS);
        jobResultEvent.fire(result);
    }

    public void createProject(String jobId, String repositoryName, String projectName) {
        System.out.println("-----KieSessionAsyncJobRequestObserver:createProject--- , repositoryName:" + repositoryName + ", project name:" + projectName);
        JobResult result = new JobResult();
        result.setJodId(jobId);
        
        org.kie.commons.java.nio.file.Path repositoryPath = getRepositoryRootPath( repositoryName );

        if ( repositoryPath == null ) {
            result.setStatus(JobRequest.Status.RESOURCE_NOT_EXIST);
            result.setResult("Repository [" + repositoryName + "] does not exist");  
            jobResultEvent.fire(result);
            return;
        } else {
            POM pom = new POM();
            
            try {
                Project project = projectService.newProject( makeRepository( paths.convert( repositoryPath,
                                                                                            false)),
                                                             projectName,
                                                             pom,
                                                             "/" );
            } catch (org.kie.commons.java.nio.file.FileAlreadyExistsException e) {
                result.setStatus(JobRequest.Status.DUPLICATE_RESOURCE);
                result.setResult("Project [" + projectName + "] already exists");  
                jobResultEvent.fire(result);
                return;
            }

            //TODO: handle errors, exceptions.

            result.setStatus(JobRequest.Status.SUCCESS);
            jobResultEvent.fire(result);
        }
    }

    private org.uberfire.backend.repositories.Repository makeRepository(final Path repositoryRoot) {
        return new GitRepository(){

            @Override
            public Path getRoot() {
                return repositoryRoot;
            }
        };
    }
    
    public void compileProject(String jobId, String repositoryName, String projectName, BuildConfig mavenConfig ) {
        System.out.println( "-----KieSessionAsyncJobRequestObserver:compileProject--- , repositoryName:" + repositoryName + ", project name:" + projectName );
        JobResult result = new JobResult();
        result.setJodId(jobId);
        
        org.kie.commons.java.nio.file.Path repositoryPath = getRepositoryRootPath( repositoryName );

        if ( repositoryPath == null ) {
            result.setStatus(JobRequest.Status.RESOURCE_NOT_EXIST);
            result.setResult("Repository [" + repositoryName + "] does not exist");  
            jobResultEvent.fire(result);
         } else {
            Project project = projectService.resolveProject( paths.convert( repositoryPath.resolve( projectName ), false ) );

            if ( project == null ) {
                result.setStatus(JobRequest.Status.RESOURCE_NOT_EXIST);
                result.setResult("Project [" + projectName + "] does not exist" );  
                jobResultEvent.fire(result);
                return;
             }

            BuildResults buildResults = buildService.build( project );

            result.setDetailedResult(buildResultsToDetailedStringMessages(buildResults));
            result.setStatus(JobRequest.Status.SUCCESS);
            jobResultEvent.fire(result);
        }
    }
    
    private List<String> buildResultsToDetailedStringMessages(BuildResults buildResults) {
    	List<String> result = new ArrayList<String>(buildResults.getMessages().size());
    	for(BuildMessage message : buildResults.getMessages() ) {
    		String detailedStringMessage = "artifactID:" + message.getArtifactID() +
    				", level:" + message.getLevel() +
    				", path:" + message.getPath() +
    		        "text:" + message.getText();
    		result.add(detailedStringMessage);
    	}
    	
    	return result;
    }
    
    public void installProject(String jobId, String repositoryName, String projectName, BuildConfig mavenConfig ) {
        System.out.println( "-----installProject--- , repositoryName:" + repositoryName + ", project name:" + projectName );
        JobResult result = new JobResult();
        result.setJodId(jobId);
        
        org.kie.commons.java.nio.file.Path repositoryPath = getRepositoryRootPath( repositoryName );

        if ( repositoryPath == null ) {
            result.setStatus(JobRequest.Status.RESOURCE_NOT_EXIST);
            result.setResult("Repository [" + repositoryName + "] does not exist");  
            jobResultEvent.fire(result);
            return;
        } else {
            Project project = projectService.resolveProject( paths.convert( repositoryPath.resolve( projectName ), false ) );

            if ( project == null ) {
                result.setStatus(JobRequest.Status.RESOURCE_NOT_EXIST);
                result.setResult("Project [" + projectName + "] does not exist" );  
                jobResultEvent.fire(result);
                return;
            }

            DeployResult buildResults = buildService.buildAndDeploy( project );

            result.setDetailedResult(deployResultToDetailedStringMessages(buildResults));
            result.setStatus(JobRequest.Status.SUCCESS);
            jobResultEvent.fire(result);
        }
    }
    
    private List<String> deployResultToDetailedStringMessages(DeployResult deployResult) {
    	List<String> result = new ArrayList<String>(1);
    	String detailedStringMessage = "artifactID:" + deployResult.getArtifactId() +
				", groupId:" + deployResult.getGroupId() +
				", version:" + deployResult.getVersion();
        result.add(detailedStringMessage);
    	return result;
    }
    
    public void testProject(String jobId, String repositoryName, String projectName, BuildConfig config ) {
        System.out.println( "-----testProject--- , repositoryName:" + repositoryName + ", project name:" + projectName );
        final JobResult result = new JobResult();
        result.setJodId(jobId);

        org.kie.commons.java.nio.file.Path repositoryPath = getRepositoryRootPath( repositoryName );

        if ( repositoryPath == null ) {
            result.setStatus(JobRequest.Status.RESOURCE_NOT_EXIST);
            result.setResult("Repository [" + repositoryName + "] does not exist");
            jobResultEvent.fire(result);
            return;
        } else {
            Project project = projectService.resolveProject( paths.convert( repositoryPath.resolve( projectName ), false ) );

            if ( project == null ) {
                result.setStatus(JobRequest.Status.RESOURCE_NOT_EXIST);
                result.setResult("Project [" + projectName + "] does not exist" );
                jobResultEvent.fire(result);
                return;
            }

            //TODO: Get session from BuildConfig or create a default session for testing if no session is provided.
            scenarioTestEditorService.runAllScenarios( project.getPomXMLPath(), new Event<TestResultMessage>() {
				@Override
				public void fire(TestResultMessage event) {
					result.setDetailedResult(testResultMessageToDetailedStringMessages(event));
					result.setStatus(event.wasSuccessful() ? JobRequest.Status.SUCCESS : JobRequest.Status.FAIL);
			        jobResultEvent.fire(result);					
				}

				//@Override
				public Event<TestResultMessage> select(Annotation... qualifiers) {
					// TODO Auto-generated method stub
					return null;
				}

				//@Override
				public <U extends TestResultMessage> Event<U> select(
						Class<U> subtype, Annotation... qualifiers) {
					// TODO Auto-generated method stub
					return null;
				}

				//@Override
				public <U extends TestResultMessage> Event<U> select(
						TypeLiteral<U> subtype, Annotation... qualifiers) {
					// TODO Auto-generated method stub
					return null;
				}            	
            });
        }
    }   
    
    private List<String> testResultMessageToDetailedStringMessages(TestResultMessage message) {
    	List<String> result = new ArrayList<String>();
    	result.add("wasSuccessuful: " + message.wasSuccessful());
    	result.add("RunCoun: " + message.getRunCount());    	
    	result.add("FailureCount: " + message.getFailureCount());
    	for (Failure failure : message.getFailures()) {
        	result.add("Failure: " + failure.getMessage());    		
    	}
    	return result;
    }
    
    public void deployProject(String jobId, String repositoryName, String projectName, BuildConfig config ) {        
        System.out.println( "-----deployProject--- , repositoryName:" + repositoryName + ", project name:" + projectName );
        JobResult result = new JobResult();
        result.setJodId(jobId);
        
        org.kie.commons.java.nio.file.Path repositoryPath = getRepositoryRootPath( repositoryName );

        if ( repositoryPath == null ) {
            result.setStatus(JobRequest.Status.RESOURCE_NOT_EXIST);
            result.setResult("Repository [" + repositoryName + "] does not exist");  
            jobResultEvent.fire(result);
            return;
        } else {
            Project project = projectService.resolveProject( paths.convert( repositoryPath.resolve( projectName ), false ) );

            if ( project == null ) {
                result.setStatus(JobRequest.Status.RESOURCE_NOT_EXIST);
                result.setResult("Project [" + projectName + "] does not exist" );  
                jobResultEvent.fire(result);
                return;
            }

            DeployResult buildResults = buildService.buildAndDeploy( project );

            result.setDetailedResult(deployResultToDetailedStringMessages(buildResults));
            result.setStatus(JobRequest.Status.SUCCESS);
            jobResultEvent.fire(result);
        }
    }
    
    public void createGroup(String jobId,  Group group ) {
        System.out.println( "-----createGroup--- , Group name:" + group.getName() + ", Group owner:" + group.getOwner() );
        JobResult result = new JobResult();
        result.setJodId(jobId);
        
        if ( group.getName() == null || group.getOwner() == null ) {
            result.setStatus(JobRequest.Status.RESOURCE_NOT_EXIST);
            result.setResult("Group name and owner must be provided");  
            jobResultEvent.fire(result);
            return;
        }

        groupService.createGroup( group.getName(), group.getOwner() );
        
        result.setStatus(JobRequest.Status.SUCCESS);
        jobResultEvent.fire(result);
    }
    
    public org.kie.commons.java.nio.file.Path getRepositoryRootPath(String repositoryName) {
        org.kie.commons.java.nio.file.Path repositoryRootPath = null;

        final Iterator<FileSystem> fsIterator = ioSystemService.getFileSystems().iterator();
        
        if ( fsIterator.hasNext() ) {
            final FileSystem fileSystem = fsIterator.next();
            System.out.println("-----FileSystem id--- :" + ((org.kie.commons.java.nio.base.FileSystemId) fileSystem).id());
            
            if (repositoryName.equalsIgnoreCase(((org.kie.commons.java.nio.base.FileSystemId) fileSystem).id())) {
                 final Iterator<org.kie.commons.java.nio.file.Path> rootIterator = fileSystem.getRootDirectories().iterator();
                 if (rootIterator.hasNext()) {
                     repositoryRootPath = rootIterator.next();
                     System.out.println("-----rootPath--- :" + repositoryRootPath);

                     org.kie.commons.java.nio.file.DirectoryStream<org.kie.commons.java.nio.file.Path> paths = ioSystemService
                             .newDirectoryStream(repositoryRootPath);
                     for (final org.kie.commons.java.nio.file.Path child : paths) {
                         System.out.println("-----child--- :" + child);
                     }
                     
                     return repositoryRootPath;
                 }
             }
        }

        return repositoryRootPath;
    }
}
