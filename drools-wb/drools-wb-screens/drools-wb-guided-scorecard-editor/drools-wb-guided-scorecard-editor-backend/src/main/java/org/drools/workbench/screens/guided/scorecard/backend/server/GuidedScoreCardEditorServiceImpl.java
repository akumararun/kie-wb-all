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

package org.drools.workbench.screens.guided.scorecard.backend.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.StringUtils;
import org.drools.workbench.models.guided.scorecard.backend.GuidedScoreCardXMLPersistence;
import org.drools.workbench.models.guided.scorecard.shared.Attribute;
import org.drools.workbench.models.guided.scorecard.shared.Characteristic;
import org.drools.workbench.models.guided.scorecard.shared.ScoreCardModel;
import org.drools.workbench.screens.guided.scorecard.model.ScoreCardModelContent;
import org.drools.workbench.screens.guided.scorecard.service.GuidedScoreCardEditorService;
import org.guvnor.common.services.backend.exceptions.ExceptionUtilities;
import org.guvnor.common.services.project.builder.events.InvalidateDMOProjectCacheEvent;
import org.guvnor.common.services.project.model.Package;
import org.guvnor.common.services.project.service.ProjectService;
import org.guvnor.common.services.shared.builder.BuildMessage;
import org.guvnor.common.services.shared.file.CopyService;
import org.guvnor.common.services.shared.file.DeleteService;
import org.guvnor.common.services.shared.file.RenameService;
import org.guvnor.common.services.shared.metadata.MetadataService;
import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.jboss.errai.bus.server.annotations.Service;
import org.kie.commons.io.IOService;
import org.kie.commons.java.nio.base.options.CommentedOption;
import org.kie.workbench.common.services.backend.source.SourceServices;
import org.kie.workbench.common.services.datamodel.oracle.PackageDataModelOracle;
import org.kie.workbench.common.services.datamodel.service.DataModelService;
import org.uberfire.backend.server.util.Paths;
import org.uberfire.backend.vfs.Path;
import org.uberfire.security.Identity;
import org.uberfire.workbench.events.ResourceAddedEvent;
import org.uberfire.workbench.events.ResourceOpenedEvent;
import org.uberfire.workbench.events.ResourceUpdatedEvent;

@Service
@ApplicationScoped
public class GuidedScoreCardEditorServiceImpl implements GuidedScoreCardEditorService {

    @Inject
    @Named("ioStrategy")
    private IOService ioService;

    @Inject
    private MetadataService metadataService;

    @Inject
    private CopyService copyService;

    @Inject
    private DeleteService deleteService;

    @Inject
    private RenameService renameService;

    @Inject
    private Event<InvalidateDMOProjectCacheEvent> invalidateDMOProjectCache;

    @Inject
    private Event<ResourceOpenedEvent> resourceOpenedEvent;

    @Inject
    private Event<ResourceAddedEvent> resourceAddedEvent;

    @Inject
    private Event<ResourceUpdatedEvent> resourceUpdatedEvent;

    @Inject
    private Paths paths;

    @Inject
    private Identity identity;

    @Inject
    private DataModelService dataModelService;

    @Inject
    private SourceServices sourceServices;

    @Inject
    private ProjectService projectService;

    @Override
    public Path create( final Path context,
                        final String fileName,
                        final ScoreCardModel content,
                        final String comment ) {
        try {
            final Package pkg = projectService.resolvePackage( context );
            final String packageName = ( pkg == null ? null : pkg.getPackageName() );
            content.setPackageName( packageName );

            final org.kie.commons.java.nio.file.Path nioPath = paths.convert( context ).resolve( fileName );
            final Path newPath = paths.convert( nioPath,
                                                false );

            ioService.createFile( nioPath );
            ioService.write( nioPath,
                             GuidedScoreCardXMLPersistence.getInstance().marshal( content ),
                             makeCommentedOption( comment ) );

            //Signal creation to interested parties
            resourceAddedEvent.fire( new ResourceAddedEvent( newPath ) );

            return newPath;

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public ScoreCardModel load( final Path path ) {
        try {
            final String content = ioService.readAllString( paths.convert( path ) );

            //Signal opening to interested parties
            resourceOpenedEvent.fire( new ResourceOpenedEvent( path ) );

            return GuidedScoreCardXMLPersistence.getInstance().unmarshall( content );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public ScoreCardModelContent loadContent( final Path path ) {
        try {
            final ScoreCardModel model = load( path );
            final PackageDataModelOracle oracle = dataModelService.getDataModel( path );
            return new ScoreCardModelContent( model,
                                              oracle );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public Path save( final Path resource,
                      final ScoreCardModel model,
                      final Metadata metadata,
                      final String comment ) {
        try {
            final Package pkg = projectService.resolvePackage( resource );
            final String packageName = ( pkg == null ? null : pkg.getPackageName() );
            model.setPackageName( packageName );

            ioService.write( paths.convert( resource ),
                             GuidedScoreCardXMLPersistence.getInstance().marshal( model ),
                             metadataService.setUpAttributes( resource, metadata ),
                             makeCommentedOption( comment ) );

            //Signal update to interested parties
            resourceUpdatedEvent.fire( new ResourceUpdatedEvent( resource ) );

            return resource;

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public void delete( final Path path,
                        final String comment ) {
        try {
            deleteService.delete( path,
                                  comment );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public Path rename( final Path path,
                        final String newName,
                        final String comment ) {
        try {
            return renameService.rename( path,
                                         newName,
                                         comment );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public Path copy( final Path path,
                      final String newName,
                      final String comment ) {
        try {
            return copyService.copy( path,
                                     newName,
                                     comment );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public String toSource( final Path path,
                            final ScoreCardModel model ) {
        try {
            final List<BuildMessage> results = validateScoreCard( model );
            if ( results.isEmpty() ) {
                return toDRL( path,
                              model );
            }
            return toValidationErrors( results );

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    private String toDRL( final Path path,
                          final ScoreCardModel model ) {
        return sourceServices.getServiceFor( paths.convert( path ) ).getSource( paths.convert( path ), model );
    }

    private String toValidationErrors( final List<BuildMessage> results ) {
        final StringBuilder drl = new StringBuilder();
        for ( final BuildMessage msg : results ) {
            drl.append( "//" ).append( msg.getText() ).append( "\n" );
        }
        return drl.toString();
    }

    @Override
    public List<BuildMessage> validate( final ScoreCardModel content ) {
        try {
            final List<BuildMessage> result = validateScoreCard( content );
            return result;

        } catch ( Exception e ) {
            throw ExceptionUtilities.handleException( e );
        }
    }

    @Override
    public boolean isValid( final ScoreCardModel content ) {
        return validate( content ).isEmpty();
    }

    private List<BuildMessage> validateScoreCard( final ScoreCardModel model ) {
        final List<BuildMessage> results = new ArrayList<BuildMessage>();
        if ( StringUtils.isBlank( model.getFactName() ) ) {
            results.add( createBuilderResultLine( "Fact Name is empty." ) );
        }
        if ( StringUtils.isBlank( model.getFieldName() ) ) {
            results.add( createBuilderResultLine( "Resultant Score Field is empty." ) );
        }
        if ( model.getCharacteristics().size() == 0 ) {
            results.add( createBuilderResultLine( "No Characteristics Found." ) );
        }
        int ctr = 1;
        for ( final Characteristic c : model.getCharacteristics() ) {
            String characteristicName = "Characteristic ('#" + ctr + "')";
            if ( StringUtils.isBlank( c.getName() ) ) {
                results.add( createBuilderResultLine( "Characteristic Name '" + characteristicName + "' is empty." ) );
            } else {
                characteristicName = "Characteristic ('" + c.getName() + "')";
            }
            if ( StringUtils.isBlank( c.getFact() ) ) {
                results.add( createBuilderResultLine( "Characteristic Name '" + characteristicName + "'. Fact is empty." ) );
            }
            if ( StringUtils.isBlank( c.getField() ) ) {
                results.add( createBuilderResultLine( "Characteristic Name '" + characteristicName + "'. Characteristic Field is empty." ) );
            } else if ( StringUtils.isBlank( c.getDataType() ) ) {
                results.add( createBuilderResultLine( "Characteristic Name '" + characteristicName + "'. Internal Error (missing datatype)." ) );
            }
            if ( c.getAttributes().size() == 0 ) {
                results.add( createBuilderResultLine( "Characteristic Name '" + characteristicName + "'. No Attributes Found." ) );
            }
            if ( model.isUseReasonCodes() ) {
                if ( StringUtils.isBlank( model.getReasonCodeField() ) ) {
                    results.add( createBuilderResultLine( "Characteristic Name '" + characteristicName + "'. Resultant Reason Codes Field is empty." ) );
                }
                if ( !"none".equalsIgnoreCase( model.getReasonCodesAlgorithm() ) ) {
                    results.add( createBuilderResultLine( "Characteristic Name '" + characteristicName + "'. Baseline Score is not specified." ) );
                }
            }
            int attrCtr = 1;
            for ( final Attribute attribute : c.getAttributes() ) {
                final String attributeName = "Attribute ('#" + attrCtr + "')";
                if ( StringUtils.isBlank( attribute.getOperator() ) ) {
                    results.add( createBuilderResultLine( "Attribute Name '" + attributeName + "'. Attribute Operator is empty." ) );
                }
                if ( StringUtils.isBlank( attribute.getValue() ) ) {
                    results.add( createBuilderResultLine( "Attribute Name '" + attributeName + "'. Attribute Value is empty." ) );
                }
                if ( model.isUseReasonCodes() ) {
                    if ( StringUtils.isBlank( c.getReasonCode() ) ) {
                        if ( StringUtils.isBlank( attribute.getReasonCode() ) ) {
                            results.add( createBuilderResultLine( "Attribute Name '" + attributeName + "'. Reason Code must be set at either attribute or characteristic." ) );
                        }
                    }
                }
                attrCtr++;
            }
            ctr++;
        }
        return results;
    }

    private BuildMessage createBuilderResultLine( final String message ) {
        final BuildMessage msg = new BuildMessage();
        msg.setText( message );
        return msg;
    }

    private CommentedOption makeCommentedOption( final String commitMessage ) {
        final String name = identity.getName();
        final Date when = new Date();
        final CommentedOption co = new CommentedOption( name,
                                                        null,
                                                        commitMessage,
                                                        when );
        return co;
    }

}
