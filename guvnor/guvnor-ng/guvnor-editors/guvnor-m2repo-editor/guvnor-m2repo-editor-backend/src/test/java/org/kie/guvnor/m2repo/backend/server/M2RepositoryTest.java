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

package org.kie.guvnor.m2repo.backend.server;

import org.drools.kproject.ReleaseIdImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.builder.ReleaseId;
import org.kie.guvnor.commons.data.tables.PageResponse;
import org.kie.guvnor.m2repo.model.JarListPageRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class M2RepositoryTest {
    private static final Logger log = LoggerFactory.getLogger( M2RepositoryTest.class );

    
    @After
    public void tearDown() throws Exception {
        log.info( "Creating a new Repository Instance.." );

        File dir = new File( "repository" );
        log.info( "DELETING test repo: " + dir.getAbsolutePath() );
        deleteDir( dir );
        log.info( "TEST repo was deleted." );
    }

    public static boolean deleteDir(File dir) {

        if ( dir.isDirectory() ) {
            String[] children = dir.list();
            for ( int i = 0; i < children.length; i++ ) {
                if ( !deleteDir( new File( dir,
                                           children[i] ) ) ) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
    
    @Test
    public void testAddFile() throws Exception {
        M2Repository repo = new M2Repository();
        repo.init();

        ReleaseId gav = new ReleaseIdImpl("org.kie.guvnor", "guvnor-m2repo-editor-backend", "6.0.0-SNAPSHOT");
        
        InputStream is = this.getClass().getResourceAsStream("guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar");
        repo.addFile(is, gav);
        
        Collection<File> files = repo.listFiles();

        boolean found = false;
        for(File file : files) {
            String fileName = file.getName();
            if("guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar".equals(fileName)) {
                found = true;
                String path = file.getPath();
                assertEquals("repository\\releases\\org\\kie\\guvnor\\guvnor-m2repo-editor-backend\\6.0.0-SNAPSHOT\\guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar", path);
                break;
            } 
        }
        
        assertTrue("Did not find expected file after calling M2Repository.addFile()", found);
    }

    @Test
    public void testListFiles() throws Exception {
        M2Repository repo = new M2Repository();
        repo.init();

        ReleaseId gav = new ReleaseIdImpl("org.kie.guvnor", "guvnor-m2repo-editor-backend", "6.0.0-SNAPSHOT");        
        InputStream is = this.getClass().getResourceAsStream("guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar");
        repo.addFile(is, gav);
        
        gav = new ReleaseIdImpl("org.jboss.arquillian.core", "arquillian-core-api", "1.0.2.Final");        
        is = this.getClass().getResourceAsStream("guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar");
        repo.addFile(is, gav);
        
        Collection<File> files = repo.listFiles();

        boolean found1 = false;
        boolean found2 = false;
        for(File file : files) {
            String fileName = file.getName();
            if("guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar".equals(fileName)) {
                found1 = true;
                String path = file.getPath();
                assertEquals("repository\\releases\\org\\kie\\guvnor\\guvnor-m2repo-editor-backend\\6.0.0-SNAPSHOT\\guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar", path);
            } 
            if("arquillian-core-api-1.0.2.Final.jar".equals(fileName)) {
                found2 = true;
                String path = file.getPath();
                assertEquals("repository\\releases\\org\\jboss\\arquillian\\core\\arquillian-core-api\\1.0.2.Final\\arquillian-core-api-1.0.2.Final.jar", path);
            } 
        }
        
        assertTrue("Did not find expected file after calling M2Repository.addFile()", found1);
        assertTrue("Did not find expected file after calling M2Repository.addFile()", found2);
    }
    
    @Test
    public void testListFilesWithFilter() throws Exception {
        M2Repository repo = new M2Repository();
        repo.init();

        ReleaseId gav = new ReleaseIdImpl("org.kie.guvnor", "guvnor-m2repo-editor-backend", "6.0.0-SNAPSHOT");        
        InputStream is = this.getClass().getResourceAsStream("guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar");
        repo.addFile(is, gav);
        
        gav = new ReleaseIdImpl("org.jboss.arquillian.core", "arquillian-core-api", "1.0.2.Final");        
        is = this.getClass().getResourceAsStream("guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar");
        repo.addFile(is, gav);
        
        //filter with version number
        Collection<File> files = repo.listFiles("1.0.2");
        boolean found1 = false;
        for(File file : files) {
            String fileName = file.getName();
            if("arquillian-core-api-1.0.2.Final.jar".equals(fileName)) {
                found1 = true;
            } 
        }        
        assertTrue("Did not find expected file after calling M2Repository.addFile()", found1);
        
/*        //filter with group id
        files = repo.listFiles("org.kie.guvnor");
        found1 = false;
        for(File file : files) {
            String fileName = file.getName();
            if("guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar".equals(fileName)) {
                found1 = true;
            } 
        }        
        assertTrue("Did not find expected file after calling M2Repository.addFile()", found1);*/
        
        //fileter with artifact id
        files = repo.listFiles("arquillian-core-api");
        found1 = false;
        for(File file : files) {
            String fileName = file.getName();
            if("arquillian-core-api-1.0.2.Final.jar".equals(fileName)) {
                found1 = true;
            } 
        }        
        assertTrue("Did not find expected file after calling M2Repository.addFile()", found1);

    }
    
    @Test
    public void testDeleteFile() throws Exception {
        M2Repository repo = new M2Repository();
        repo.init();

        ReleaseId gav = new ReleaseIdImpl("org.kie.guvnor", "guvnor-m2repo-editor-backend", "6.0.0-SNAPSHOT");        
        InputStream is = this.getClass().getResourceAsStream("guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar");
        repo.addFile(is, gav);
        
        gav = new ReleaseIdImpl("org.jboss.arquillian.core", "arquillian-core-api", "1.0.2.Final");        
        is = this.getClass().getResourceAsStream("guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar");
        repo.addFile(is, gav);
        
        Collection<File> files = repo.listFiles();

        boolean found1 = false;
        boolean found2 = false;
        for(File file : files) {
            String fileName = file.getName();
            if("guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar".equals(fileName)) {
                found1 = true;
                String path = file.getPath();
                assertEquals("repository\\releases\\org\\kie\\guvnor\\guvnor-m2repo-editor-backend\\6.0.0-SNAPSHOT\\guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar", path);
            } 
            if("arquillian-core-api-1.0.2.Final.jar".equals(fileName)) {
                found2 = true;
                String path = file.getPath();
                assertEquals("repository\\releases\\org\\jboss\\arquillian\\core\\arquillian-core-api\\1.0.2.Final\\arquillian-core-api-1.0.2.Final.jar", path);
            } 
        }
        
        assertTrue("Did not find expected file after calling M2Repository.addFile()", found1);
        assertTrue("Did not find expected file after calling M2Repository.addFile()", found2);
        
        boolean result = repo.deleteFile("repository\\releases\\org\\kie\\guvnor\\guvnor-m2repo-editor-backend\\6.0.0-SNAPSHOT\\guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar");
        result = repo.deleteFile("repository\\releases\\org\\jboss\\arquillian\\core\\arquillian-core-api\\1.0.2.Final\\arquillian-core-api-1.0.2.Final.jar");
        
        found1 = false;
        found2 = false;
        files = repo.listFiles();
        for(File file : files) {
            String fileName = file.getName();
            if("guvnor-m2repo-editor-backend-6.0.0-SNAPSHOT.jar".equals(fileName)) {
                found1 = true;
            } 
            if("arquillian-core-api-1.0.2.Final.jar".equals(fileName)) {
                found2 = true;
            } 
        }
        
        assertFalse("Found unexpected file after calling M2Repository.deleteFile()", found1);
        assertFalse("Found unexpected file after calling M2Repository.deleteFile()", found2);        
    }
}
