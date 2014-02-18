/*
 * Copyright 2014 JBoss Inc
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
package org.kie.integration.eap.maven;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.kie.integration.eap.maven.distribution.EAPLayerDistributionManager;
import org.kie.integration.eap.maven.distribution.EAPXMLLayerDistribution;
import org.kie.integration.eap.maven.exception.EAPModuleDefinitionException;
import org.kie.integration.eap.maven.exception.EAPModuleResourceDuplicationException;
import org.kie.integration.eap.maven.exception.EAPModulesDefinitionException;
import org.kie.integration.eap.maven.model.dependency.EAPModuleDependency;
import org.kie.integration.eap.maven.model.dependency.EAPStaticModuleDependency;
import org.kie.integration.eap.maven.model.graph.EAPModuleGraphNodeDependency;
import org.kie.integration.eap.maven.model.graph.distribution.EAPModuleNodeGraphDependency;
import org.kie.integration.eap.maven.model.layer.EAPLayer;
import org.kie.integration.eap.maven.model.layer.EAPLayerImpl;
import org.kie.integration.eap.maven.model.module.EAPDynamicModule;
import org.kie.integration.eap.maven.model.module.EAPModule;
import org.kie.integration.eap.maven.model.resource.EAPModuleResource;
import org.kie.integration.eap.maven.scanner.EAPModulesScanner;
import org.kie.integration.eap.maven.scanner.EAPStaticModulesScanner;
import org.kie.integration.eap.maven.template.EAPTemplateBuilder;
import org.kie.integration.eap.maven.template.EAPVelocityTemplateBuilder;
import org.kie.integration.eap.maven.util.*;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This plugin mojo generates a dynamic module (webapp) definition and the assembly files to assemble it.
 *
 * @goal build-dynamic
 * @requiresProject true
 */
public class EAPDynamicModulesBuilderMojo extends AbstractMojo {

    private static final String JBOSS_DEP_STRUCTURE_NAME = "jboss-deployment-structure";
    private static final String EXTENSION_XML = ".xml";
    private static final String JBOSS_DEP_STRUCTURE_ZIP_ENTRY_NAME = "WEB-INF/jboss-deployment-structure.xml";
    private static final String ASSEMBLY_DESCRIPTOR_NAME = "-assembly.xml";
    private static final String EXCLUSIONS_PATH = "WEB-INF/lib/";

    /** The path where modules will be deployed in EAP. Corresponds to modules/system/layers. **/
    private static final String ASSEMBLY_OUTPUT_PATH = new StringBuilder("modules").append(File.separator).
            append("system").append(File.separator).append("layers").toString();

    private static Pattern PATTERN_WAR_LIBRARIES = Pattern.compile("WEB-INF/lib/(.*).jar");
    /**
     * The Maven project.
     *
     * @parameter property="project"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    protected RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    protected RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution of plugins and their dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    protected List<RemoteRepository> remoteRepos;

    /**
     * The name of the dynamic module distribution to generate.
     *
     * @parameter default-value=""
     */
    protected String distributionName;

    /**
     * The output path for the genrated module descriptor and assembly files.
     * The resulting global assembly.xml file will be created in this path.
     *
     * @parameter default-value=""
     */
    protected String outputPath;

    /**
     * The output formats for assembly descriptor. Use comma-separated values.
     *
     * @parameter default-value="dir,war"
     */
    protected String assemblyFormats;

    // Services.
    private EAPTemplateBuilder templateBuilder;
    protected EAPLayerDistributionManager distributionManager;
    private EAPModulesScanner staticModulesScanner;

    // Class members.
    private Collection<Artifact> dynamicModuleArtifacts = null;
    private Collection<Artifact> staticModuleArtifacts = null;
    private Collection<EAPDynamicModule> dynamicModules;
    private EAPLayer staticLayer;
    private EAPArtifactsHolder artifactsHolder;
    private String distroOutputPath = null;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        // Check configuration parameters.
        checkConfiguration();

        initServices();

        distroOutputPath = new StringBuilder(outputPath).append(File.separator).append("dynamic-modules").
                append(File.separator).append(distributionName).toString();

        try {
            // Obtain the dynamic module definitions dependencies present in project.
            dynamicModuleArtifacts = scanPomDependencies();
            if (dynamicModuleArtifacts == null || dynamicModuleArtifacts.isEmpty()) throw new EAPModulesDefinitionException("No dynamic modules found in project dependency artifacts.");
            getLog().info("Found " + dynamicModuleArtifacts.size() + " POM dependency artifacts.");

            dynamicModules = new ArrayList<EAPDynamicModule>();
            staticModuleArtifacts = new LinkedHashSet<Artifact>();

            // Create the model.
            for (Artifact dynamicModuleArtifact : dynamicModuleArtifacts) {
                EAPDynamicModule dynamicModule = scanDynamicModule(dynamicModuleArtifact);
                dynamicModules.add(dynamicModule);
            }

            // Scan the static layer.
            if (staticModuleArtifacts.isEmpty()) throw new EAPModulesDefinitionException("No static modules found in project dependency artifacts.");
            staticLayer = staticModulesScanner.scan("staticLayer", staticModuleArtifacts, null, artifactsHolder);


            // Fill artifacts holder with static layer modules.
            EAPLayer layer = new EAPLayerImpl(distributionName);
            Collection<EAPModule> staticModules = staticLayer.getModules();
            for (EAPModule staticModule : staticModules) {
                fixDynamicModuleDependency(staticModule);
                artifactsHolder.add(staticModule.getArtifact(), staticModule);
                layer.addModule(staticModule);
            }

        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Cannot resolve a WAR dependency. ", e);
        } catch (EAPModulesDefinitionException e) {
            throw new MojoExecutionException("Cannot resolve module definitions. ", e);
        } catch (EAPModuleDefinitionException e) {
            throw new MojoExecutionException("cannot resolve a module definition. ", e);
        } catch (EAPModuleResourceDuplicationException e) {
            throw new MojoExecutionException("Resource is duplicated. ", e);
        }


        // Genrate the jboss-deployment-structure and assembly files for each dynamic module.
        for (EAPDynamicModule dynamicModule : dynamicModules) {

            // IF the current WAR contains a jboss deployment structure descriptor, read its dependencies.
            Collection<EAPModuleNodeGraphDependency> staticModuleDependencies = new LinkedList<EAPModuleNodeGraphDependency>();
            Collection<String> staticModuleResourceNames = new LinkedHashSet<String>();

            Collection<EAPModuleDependency> _dependencies = dynamicModule.getDependencies();
            if (_dependencies != null && !_dependencies.isEmpty()) {
                for (EAPModuleDependency dependency : _dependencies) {
                    EAPModuleNodeGraphDependency dep = new EAPModuleNodeGraphDependency(dependency.getName(), dependency.getSlot(), false);
                    staticModuleDependencies.add(dep);

                    EAPModule module = artifactsHolder.getModule(((EAPStaticModuleDependency)dependency).getArtifacts().iterator().next());
                    Collection<EAPModuleResource> resources = module.getResources();
                    if (resources != null && !resources.isEmpty()) {
                        for (EAPModuleResource resource : resources) {
                            Artifact artifact = (Artifact) resource.getResource();
                            staticModuleResourceNames.add(EXCLUSIONS_PATH + resource.getFileName());
                        }
                    }
                }
            }

            // Obtain the war file and generate jboss-deployment-structure description.
            ZipFile war = null;
            EAPWarResources warResources = null;
            String jbossDeploymentStructure = null;
            String warArtifactCoordinates = EAPArtifactUtils.getArtifactCoordinates(dynamicModule.getWarFile());
            try {
                war = getWarFile(dynamicModule);
                warResources = scanWarResources(war);
                jbossDeploymentStructure = generateJbossDeploymentStructure(staticModuleDependencies, war, warResources.getJbossDeploymentStructure());
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot obtain WAR dependency file or cannot access its content.", e);
            } catch (ArtifactResolutionException e) {
                throw new MojoExecutionException("Cannot resolve WAR dependency.", e);
            }

            // Generate the assembly file.
            try {
                // Generate the war assembly exclusions list.
                Collection<String> exclusions = generateWarExclusions(staticModuleResourceNames, warResources.getWarLibs());
                exclusions.add(JBOSS_DEP_STRUCTURE_ZIP_ENTRY_NAME);

                //Generate the assembly descriptor content.
                String assembly = generateAssemblyDescriptor(dynamicModule.getName(), warArtifactCoordinates,jbossDeploymentStructure, exclusions);

                // Write the generated assembly descriptor.
                File out = EAPFileUtils.writeFile(new File(distroOutputPath),dynamicModule.getArtifact().getArtifactId() + ASSEMBLY_DESCRIPTOR_NAME, assembly);

                getLog().info("Assembly file generated into " + out.getAbsolutePath());
                getLog().info("Dynamic distribution generated sucessfully.");
            } catch (IOException e) {
                throw new MojoExecutionException("Exception generating the assembly file. ", e);
            }
        }

    }

    private Collection<String> generateWarExclusions(Collection<String> staticModuleResourceNames, Collection<String> warLibs) {
        if (warLibs == null || warLibs.isEmpty()) return null;
        if (staticModuleResourceNames == null || staticModuleResourceNames.isEmpty()) return Collections.emptyList();

        Map<String, String> staticResourceCoords = new HashMap<String, String>(staticModuleResourceNames.size());
        Map<String, String> warResourceArtId_fileName = new HashMap<String, String>(warLibs.size());

        for (String resourceName : staticModuleResourceNames) {
            // Lenght of "WEb-INF/lib/" is 12.
            String[] coords = EAPArtifactUtils.parseFileName(resourceName.substring(12, resourceName.length()));
            staticResourceCoords.put(coords[0], coords[1]);
        }

        Map<String, String> warResourceCoords = new HashMap<String, String>(warLibs.size());
        for (String resourceName : warLibs) {
            // Lenght of "WEb-INF/lib/" is 12.
            String[] coords = EAPArtifactUtils.parseFileName(resourceName.substring(12, resourceName.length()));
            warResourceCoords.put(coords[0], coords[1]);
            warResourceArtId_fileName.put(coords[0], resourceName);
        }

        Collection<String> exclusions = new LinkedList<String>();
        for (Map.Entry<String, String> warResourceCoordsEntry : warResourceCoords.entrySet()) {
            String warResourceArtId = warResourceCoordsEntry.getKey();
            String warResourceVersion = warResourceCoordsEntry.getValue();

            String staticResourceVersion = staticResourceCoords.get(warResourceArtId);
            if (staticResourceVersion != null) {

                if (warResourceVersion != null && !warResourceVersion.equals(staticResourceVersion)) {
                    getLog().warn("Excluded " + warResourceArtId + ":" + warResourceVersion + " from war but the version defined in static module is " + staticResourceVersion);
                }
                exclusions.add(warResourceArtId_fileName.get(warResourceArtId));
            }
        }
        return exclusions;
    }

    private void fixDynamicModuleDependency(EAPModule module) {
        for (EAPDynamicModule dynModule : dynamicModules) {
            Collection<EAPModuleDependency>  dependencies = dynModule.getDependencies();
            if (dependencies != null && !dependencies.isEmpty()) {
                for (EAPModuleDependency dependency : dependencies) {
                    EAPStaticModuleDependency staticModuleDependency = (EAPStaticModuleDependency) dependency;
                    if (!staticModuleDependency.getArtifacts().isEmpty()) {
                        Artifact moduleArtifact = staticModuleDependency.getArtifacts().iterator().next();
                        if (EAPArtifactUtils.equals(module.getArtifact(), moduleArtifact)) {
                            staticModuleDependency.setName(module.getName());
                            staticModuleDependency.setSlot(module.getSlot());
                        }
                    }
                }
            }
        }
    }

    private EAPDynamicModule scanDynamicModule(Artifact artifact) throws EAPModuleDefinitionException{
        if (artifact == null) return  null;

        String moduleArtifactCoordinates = EAPArtifactUtils.getArtifactCoordinates(artifact);

        try {
            Model moduleModel = EAPArtifactUtils.generateModel(artifact);
            String moduleName = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_NAME));
            String moduleType = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_TYPE));
            // String moduleDependenciesRaw = EAPArtifactUtils.getPropertyValue(moduleModel, (String) moduleModel.getProperties().get(EAPConstants.MODULE_DEPENDENCIES));

            // Obtain module properties.
            if (moduleName == null || moduleName.trim().length() == 0)
                throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module name is not set.");
            if (moduleType == null || moduleType.trim().length() == 0)
                throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module type is not set.");
            if (!moduleType.equalsIgnoreCase(EAPConstants.MODULE_TYPE_DYNAMIC))
                throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module is not dynamic.");

            EAPDynamicModule result = new EAPDynamicModule(moduleName);
            result.setArtifact(artifact);

            // Add the static module dependencies.
            // TODO: addStaticDependencies();

            // Obtain module resources.
            List<Dependency> moduleDependencies = moduleModel.getDependencies();
            if (moduleDependencies != null && !moduleDependencies.isEmpty()) {
                for (org.apache.maven.model.Dependency moduleDependency : moduleDependencies) {

                    String artifactId = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getArtifactId());;
                    String groupId = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getGroupId());
                    String version = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getVersion());
                    String type = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getType());
                    String classifier = EAPArtifactUtils.getPropertyValue(moduleModel, moduleDependency.getClassifier());

                    Artifact artifact1 = EAPArtifactUtils.createArtifact(groupId, artifactId, version, type, classifier);
                    if (moduleDependency.getType().equals(EAPConstants.WAR)) result.setWarFile(artifact1);
                    else if (moduleDependency.getType().equals(EAPConstants.POM)) {
                        EAPStaticModuleDependency dep = new EAPStaticModuleDependency(moduleDependency.getArtifactId());
                        dep.setArtifacts(Arrays.asList(new Artifact[] {artifact1}));
                        result.addDependency(dep);
                        staticModuleArtifacts.add(artifactsHolder.resolveArtifact(artifact1));
                    }
                }
            }

            if (result.getWarFile() == null) throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The module has not the required WAR dependency.");

            return result;

        } catch (XmlPullParserException e) {
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The artifact's pom cannot be pared.", e);
        } catch (IOException e) {
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The artifact's pom cannot be read.", e);
        } catch (ArtifactResolutionException e) {
            throw new EAPModuleDefinitionException(moduleArtifactCoordinates, "The artifact cannot be resolved.", e);
        }
    }

    protected void checkConfiguration() throws MojoFailureException {
        if (distributionName == null || distributionName.trim().length() == 0) throw new MojoFailureException("Distribution name configuration parameter cannot be null or empty.");
        if (outputPath == null || outputPath.trim().length() == 0) throw new MojoFailureException("Output path configuration parameter cannot be null or empty.");
    }

    // TODO: Replace using plexus container.
    protected void initServices() {
        templateBuilder = new EAPVelocityTemplateBuilder();
        distributionManager = new EAPXMLLayerDistribution();
        staticModulesScanner = new EAPStaticModulesScanner();
        staticModulesScanner.setScanResources(true);
        staticModulesScanner.setScanStaticDependencies(false);
        ((EAPStaticModulesScanner)staticModulesScanner).setArtifactTreeResolved(false);
        artifactsHolder = new EAPArtifactsHolder(repoSystem, repoSession, remoteRepos);
    }

    private Collection<Artifact> scanPomDependencies() throws ArtifactResolutionException {
        Collection<Artifact> result = null;
        Set<org.apache.maven.artifact.Artifact> artifacts = project.getDependencyArtifacts();

        if (artifacts != null) {
            result = new LinkedList<Artifact>();
            for (org.apache.maven.artifact.Artifact artifact : artifacts) {
                if (EAPConstants.POM.equals(artifact.getType())) {
                    Artifact resolved = EAPArtifactUtils.resolveArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType(), artifact.getClassifier(), repoSystem, repoSession, remoteRepos);
                    result.add(resolved);
                }
            }
        }

        return result;
    }

    protected ZipFile getWarFile(EAPDynamicModule module) throws IOException, ArtifactResolutionException {
        Artifact warArtifact = artifactsHolder.resolveArtifact(module.getWarFile());
        return new ZipFile(warArtifact.getFile(), ZipFile.OPEN_READ);
    }

    protected EAPWarResources scanWarResources(ZipFile war) throws MojoExecutionException, IOException {
        Document currentJbossDepStructureDoc = null;
        Collection<String> warDependencies = new LinkedList<String>();

        String warName = EAPFileUtils.extractFileName(war.getName());

        try {
            for (Enumeration e = war.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();

                if (entry.getName().equals(JBOSS_DEP_STRUCTURE_ZIP_ENTRY_NAME)) {
                    InputStream in = war.getInputStream(entry);
                    EAPXMLUtils xmlUtils = new EAPXMLUtils(in);
                    currentJbossDepStructureDoc = xmlUtils.getDocument();
                }
                if (isWarLibrary(entry.getName())) {
                    warDependencies.add(entry.getName());
                }


            }
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot read the jboss-deployment-structure descriptor from WAR dependency: " + warName, e);
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot read the jboss-deployment-structure descriptor from WAR dependency: " + warName, e);
        }
        return new EAPWarResources(currentJbossDepStructureDoc, warDependencies);
    }

    protected boolean isWarLibrary(String fileName) {
        return PATTERN_WAR_LIBRARIES.matcher(fileName).matches();
    }

    protected String generateJbossDeploymentStructure(Collection<EAPModuleNodeGraphDependency> staticModuleDependencies, ZipFile war, Document currentJbossDepStructureDoc) throws MojoExecutionException, IOException {
        String warName = EAPFileUtils.extractFileName(war.getName());

        Collection<? extends EAPModuleGraphNodeDependency> dependencies = new LinkedList<EAPModuleGraphNodeDependency>(staticModuleDependencies);

        if (currentJbossDepStructureDoc != null) {
            getLog().info("Jboss deployment descritpor file found in WAR " + warName + ". Reading its dependencies.");

            // Obtain the current dependencies present in current deployment structure descriptor.
            Collection<EAPModuleNodeGraphDependency> actualDependencies = readCurrentJBossDepStructurDependencies(currentJbossDepStructureDoc);

            // Merge current jboss-deployment-structure (if present) with the ones from static module definition.
            dependencies = mergeDependencies(staticModuleDependencies, actualDependencies, war);
        }

        return templateBuilder.buildJbossDeploymentStructure(dependencies);
    }

    private String generateAssemblyDescriptor(String layerId, String inputWarCoordinates, String jbossDeploymentStructureContent, Collection<String> exclusions) throws MojoExecutionException, IOException {
        // Write the jboss-deployment-structure content into a temp path.
        String jbossDepStuctureName = new StringBuilder(layerId).append("-").append(JBOSS_DEP_STRUCTURE_NAME).append(EXTENSION_XML).toString();

        File out = EAPFileUtils.writeFile(new File(distroOutputPath), jbossDepStuctureName, jbossDeploymentStructureContent);
        // Build the content.
        return templateBuilder.buildDynamicModuleAssembly(layerId, assemblyFormats.split(","), inputWarCoordinates ,exclusions, out.getAbsolutePath());
    }

    private Collection<EAPModuleGraphNodeDependency> mergeDependencies(Collection<EAPModuleNodeGraphDependency> staticModuleDependencies, Collection<EAPModuleNodeGraphDependency> actualDependencies, ZipFile war) {
        Collection<EAPModuleGraphNodeDependency> dependencies = new LinkedList<EAPModuleGraphNodeDependency>();

        // Add the ones from static modules.
        dependencies.addAll(staticModuleDependencies);

        // Check the current jboss-dep-structure.xml file from WAR dependency.
        if (actualDependencies != null && !actualDependencies.isEmpty()) {
            for (EAPModuleNodeGraphDependency dependency : actualDependencies) {
                String depName = dependency.getName();
                String depSlot = dependency.getSlot() != null ? dependency.getSlot() : "main";
                String depCoords = new StringBuilder(depName).append(":").append(depSlot).toString();
                if (!staticModuleDependencies.contains(dependency)) {
                    getLog().warn("Dependency " + depCoords + " is present in actual jboss-deployment-structure file from WAR '" + EAPFileUtils.extractFileName(war.getName()) + "'. It will be added in the new generated jboss-deployment-structure descriptor, if not present.");
                    dependencies.add(dependency);
                } else {
                    getLog().warn("Dependency " + depCoords + " will be overriden by the one from static module definition.");
                }
            }
        }

        return dependencies;
    }

    protected Collection<EAPModuleNodeGraphDependency> readCurrentJBossDepStructurDependencies(Document document) {
        Collection<EAPModuleNodeGraphDependency> result = null;
        if (document != null) {
            NodeList moduleNodes = document.getElementsByTagName("module");
            if (moduleNodes != null && moduleNodes.getLength() > 0) {
                result = new LinkedList<EAPModuleNodeGraphDependency>();
                for (int i = 0; i < moduleNodes.getLength(); i++) {
                    Node node = moduleNodes.item(i);
                    NamedNodeMap attrs = node.getAttributes();

                    String nameNodeValue = null;
                    String slotNodeValue = null;
                    String exportNodeValue = null;

                    Node nameNode = attrs.getNamedItem("name");
                    if (nameNode != null) nameNodeValue = nameNode.getNodeValue();

                    Node slotNode = attrs.getNamedItem("slot");
                    if (slotNode != null) slotNodeValue = nameNode.getNodeValue();

                    Node exportNode = attrs.getNamedItem("export");
                    if (exportNode != null) exportNodeValue = nameNode.getNodeValue();

                    EAPModuleNodeGraphDependency dep = new EAPModuleNodeGraphDependency(nameNodeValue, slotNodeValue, exportNodeValue != null ? Boolean.valueOf(exportNodeValue) : false);
                    result.add(dep);
                }
            }
        }
        return result;
    }

    private static class EAPWarResources {
        private Document jbossDeploymentStructure;
        private Collection<String> warLibs = new LinkedList<String>();

        private EAPWarResources(Document jbossDeploymentStructure, Collection<String> warLibs) {
            this.jbossDeploymentStructure = jbossDeploymentStructure;
            this.warLibs = warLibs;
        }

        private Document getJbossDeploymentStructure() {
            return jbossDeploymentStructure;
        }

        private Collection<String> getWarLibs() {
            return warLibs;
        }
    }

}