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

package org.drools.workbench.screens.dtablexls.backend.server.indexing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.drools.workbench.screens.dtablexls.type.DecisionTableXLSResourceTypeDefinition;
import org.junit.Test;
import org.kie.workbench.common.services.refactoring.backend.server.BaseIndexingTest;
import org.kie.workbench.common.services.refactoring.backend.server.TestIndexer;
import org.kie.workbench.common.services.refactoring.backend.server.indexing.RuleAttributeNameAnalyzer;
import org.kie.workbench.common.services.refactoring.backend.server.query.builder.BasicQueryBuilder;
import org.kie.workbench.common.services.refactoring.model.index.terms.RuleAttributeIndexTerm;
import org.kie.workbench.common.services.refactoring.model.index.terms.valueterms.ValueRuleAttributeIndexTerm;
import org.kie.workbench.common.services.refactoring.model.index.terms.valueterms.ValueRuleAttributeValueIndexTerm;
import org.mockito.ArgumentMatcher;
import org.slf4j.LoggerFactory;
import org.uberfire.ext.metadata.backend.lucene.index.LuceneIndex;
import org.uberfire.ext.metadata.engine.Index;
import org.uberfire.java.nio.file.Path;

import static org.apache.lucene.util.Version.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IndexDecisionTableXLSInvalidDrlTest extends BaseIndexingTest<DecisionTableXLSResourceTypeDefinition> {

    @Test
    public void testIndexDecisionTableXLSInvalidDrl() throws IOException, InterruptedException {
        //Setup logging
        final Logger root = (Logger) LoggerFactory.getLogger( Logger.ROOT_LOGGER_NAME );
        final Appender<ILoggingEvent> mockAppender = mock( Appender.class );
        when( mockAppender.getName() ).thenReturn( "MOCK" );
        root.addAppender( mockAppender );

        //Add test files
        loadXLSFile( basePath,
                     "bz1269366.xls" );

        Thread.sleep( 5000 ); //wait for events to be consumed from jgit -> (notify changes -> watcher -> index) -> lucene index

        final Index index = getConfig().getIndexManager().get( org.uberfire.ext.metadata.io.KObjectUtil.toKCluster( basePath.getFileSystem() ) );

        {
            final IndexSearcher searcher = ( (LuceneIndex) index ).nrtSearcher();
            final TopScoreDocCollector collector = TopScoreDocCollector.create( 10,
                                                                                true );
            final Query query = new BasicQueryBuilder()
                    .addTerm( new ValueRuleAttributeIndexTerm( "ruleflow-group" ) )
                    .addTerm( new ValueRuleAttributeValueIndexTerm( "myRuleFlowGroup" ) )
                    .build();

            searcher.search( query,
                             collector );
            final ScoreDoc[] hits = collector.topDocs().scoreDocs;
            assertEquals( 0,
                          hits.length );

            verify( mockAppender ).doAppend( argThat( new ArgumentMatcher<ILoggingEvent>() {

                @Override
                public boolean matches( final Object argument ) {
                    return ( (ILoggingEvent) argument ).getMessage().startsWith( "Unable to parse DRL" );
                }

            } ) );

            ( (LuceneIndex) index ).nrtRelease( searcher );
        }

    }

    @Override
    protected TestIndexer getIndexer() {
        return new TestDecisionTableXLSFileIndexer();
    }

    @Override
    public Map<String, Analyzer> getAnalyzers() {
        return new HashMap<String, Analyzer>() {{
            put( RuleAttributeIndexTerm.TERM,
                 new RuleAttributeNameAnalyzer( LUCENE_40 ) );
        }};
    }

    @Override
    protected DecisionTableXLSResourceTypeDefinition getResourceTypeDefinition() {
        return new DecisionTableXLSResourceTypeDefinition();
    }

    @Override
    protected String getRepositoryName() {
        return this.getClass().getSimpleName();
    }

    private Path loadXLSFile( final Path basePath,
                              final String fileName ) throws IOException {
        final Path path = basePath.resolve( fileName );
        final InputStream is = this.getClass().getResourceAsStream( fileName );
        final OutputStream os = ioService().newOutputStream( path );
        IOUtils.copy( is,
                      os );
        os.flush();
        os.close();
        return path;
    }

}
