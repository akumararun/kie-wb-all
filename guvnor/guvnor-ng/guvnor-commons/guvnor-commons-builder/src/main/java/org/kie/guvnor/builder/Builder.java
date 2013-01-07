/*
 * Copyright 2012 JBoss Inc
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

package org.kie.guvnor.builder;

import org.kie.KieServices;
import org.kie.builder.KieBuilder;
import org.kie.builder.KieFileSystem;
import org.kie.builder.KieModule;
import org.kie.builder.Message;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.file.DirectoryStream;
import org.kie.commons.java.nio.file.Files;
import org.kie.commons.java.nio.file.NoSuchFileException;
import org.kie.commons.java.nio.file.Path;
import org.kie.guvnor.commons.service.builder.model.Results;
import org.kie.guvnor.commons.service.source.SourceServices;
import org.uberfire.backend.server.util.Paths;

public class Builder {

    private final KieBuilder kieBuilder;
    private final String projectName;
    private final KieFileSystem kieFileSystem;
    private final IOService ioService;
    private final Path moduleDirectory;
    private final Paths paths;
    private final String artifactId;
    private final static String RESOURCE_PATH = "src/main/resources";
    private final static String KMODULE_PATH = "src/main/resources/META-INF/kmodule.xml";
    private final SourceServices sourceServices;

    public Builder(Path moduleDirectory,
                   String artifactId,
                   IOService ioService,
                   Paths paths,
                   SourceServices sourceServices) {
        this.moduleDirectory = moduleDirectory;
        this.artifactId = artifactId;
        this.ioService = ioService;
        this.paths = paths;
        this.sourceServices = sourceServices;

        KieServices kieServices = KieServices.Factory.get();
        kieFileSystem = kieServices.newKieFileSystem();

        DirectoryStream<org.kie.commons.java.nio.file.Path> directoryStream = Files.newDirectoryStream(moduleDirectory);

        projectName = getProjectName(moduleDirectory);
        visitPaths(directoryStream);

        kieBuilder = kieServices.newKieBuilder(kieFileSystem);
    }

    public void build() {
        kieBuilder.buildAll();
    }

    public KieModule getKieModule() {
        return kieBuilder.getKieModule();
    }


    //TODO This should really look for a SourceService for *all* file types. If none found don't add the file.
    private void visitPaths(final DirectoryStream<org.kie.commons.java.nio.file.Path> directoryStream) {
        for (org.kie.commons.java.nio.file.Path path : directoryStream) {
            if (Files.isDirectory(path)) {
                visitPaths(Files.newDirectoryStream(path));
            } else {
                final String fileName = path.getFileName().toString();
                final String uri = path.toUri().toString().toLowerCase();
                if (uri.endsWith(KMODULE_PATH)) {

                    kieFileSystem.write("META-INF/kmodule.xml", ioService.readAllString(path));

                } else if (!fileName.startsWith(".") && uri.endsWith(".drl")) {
                    //TODO Hack to exclude meta-data. If we had a SourceService for all file types this would not be required.

                    kieFileSystem.write(stripPath(projectName, path), ioService.readAllString(path));

                } else if (sourceServices.hasServiceFor(path.toUri().toString())) {

                    kieFileSystem.write(stripPath(projectName, path) + ".drl", sourceServices.getServiceFor(path.toUri().toString()).getSource(path));

                }
            }
        }
    }

    private String stripPath(final String projectName,
                             final org.kie.commons.java.nio.file.Path path) {
        return path.toString().substring(projectName.length() + 2);
    }

    private String getProjectName(final Path path) {
        String substring = path.toUri().toString();
        return substring.substring(substring.indexOf("uf-playground/") + "uf-playground/".length());
    }

    public Results getResults() {
        Results results = new Results();
        results.setArtifactID(artifactId);

        for (final Message message : kieBuilder.getResults().getMessages()) {
            final org.kie.guvnor.commons.service.builder.model.Message m = new org.kie.guvnor.commons.service.builder.model.Message();
            switch (message.getLevel()) {
                case ERROR:
                    m.setLevel(org.kie.guvnor.commons.service.builder.model.Message.Level.ERROR);
                    break;
                case WARNING:
                    m.setLevel(org.kie.guvnor.commons.service.builder.model.Message.Level.WARNING);
                    break;
                case INFO:
                    m.setLevel(org.kie.guvnor.commons.service.builder.model.Message.Level.INFO);
                    break;
            }

            m.setId(message.getId());
            m.setArtifactID(artifactId);
            m.setLine(message.getLine());
            if (message.getPath() != null && !message.getPath().isEmpty()) {
                try {
                    String pathToFile = RESOURCE_PATH + "/" + message.getPath();
                    System.out.println("Path to error file = " + pathToFile);
                    if (message.getPath().equals("pom.xml")) {
                        m.setPath(paths.convert(moduleDirectory.resolve(message.getPath())));
                    } else {
                        m.setPath(paths.convert(moduleDirectory.resolve(pathToFile)));
                    }
                } catch (NoSuchFileException e) {
                    // Just to be safe.
                }
            }
            m.setColumn(message.getColumn());
            m.setText(message.getText());

            results.getMessages().add(m);
        }

        return results;
    }
}
