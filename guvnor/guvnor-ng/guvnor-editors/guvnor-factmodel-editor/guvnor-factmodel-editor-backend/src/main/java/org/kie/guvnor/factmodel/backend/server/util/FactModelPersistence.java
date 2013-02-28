package org.kie.guvnor.factmodel.backend.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.compiler.DrlParser;
import org.drools.compiler.DroolsParserException;
import org.drools.guvnor.models.commons.backend.imports.ImportsParser;
import org.drools.guvnor.models.commons.shared.imports.Imports;
import org.drools.lang.descr.AnnotationDescr;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.TypeDeclarationDescr;
import org.drools.lang.descr.TypeFieldDescr;
import org.kie.guvnor.factmodel.model.AnnotationMetaModel;
import org.kie.guvnor.factmodel.model.FactMetaModel;
import org.kie.guvnor.factmodel.model.FactModels;
import org.kie.guvnor.factmodel.model.FieldMetaModel;

import static java.util.Collections.*;

/**
 * Utilities for FactModels
 */
public class FactModelPersistence {

    public static String marshal( final FactModels content ) {
        final StringBuilder sb = new StringBuilder();
        sb.append( content.getImports().toString() );
        sb.append( "\n" );
        for ( final FactMetaModel factMetaModel : content.getModels() ) {
            sb.append( toDRL( factMetaModel ) ).append( "\n\n" );
        }
        return sb.toString().trim();
    }

    private static String toDRL( final FactMetaModel mm ) {
        final StringBuilder sb = new StringBuilder();
        sb.append( "declare " ).append( mm.getName() );
        if ( mm.hasSuperType() ) {
            sb.append( " extends " );
            sb.append( mm.getSuperType() );
        }
        for ( int i = 0; i < mm.getAnnotations().size(); i++ ) {
            AnnotationMetaModel a = mm.getAnnotations().get( i );
            sb.append( "\n\t" );
            sb.append( buildAnnotationDRL( a ) );
        }
        for ( int i = 0; i < mm.getFields().size(); i++ ) {
            FieldMetaModel f = mm.getFields().get( i );
            sb.append( "\n\t" );
            sb.append( f.name ).append( ": " ).append( f.type );
        }
        sb.append( "\nend" );
        return sb.toString();
    }

    private static StringBuilder buildAnnotationDRL( AnnotationMetaModel a ) {
        final StringBuilder sb = new StringBuilder();
        sb.append( "@" );
        sb.append( a.name );
        sb.append( "(" );
        for ( final Map.Entry<String, String> e : a.getValues().entrySet() ) {
            if ( e.getKey() != null && e.getKey().length() > 0 ) {
                sb.append( e.getKey() );
                sb.append( " = " );
            }
            if ( e.getValue() != null && e.getValue().length() > 0 ) {
                sb.append( e.getValue() );
            }
            sb.append( ", " );
        }
        sb.delete( sb.length() - 2,
                   sb.length() );
        sb.append( ")" );
        return sb;
    }

    public static FactModels unmarshal( final String content ) {
        try {
            final List<FactMetaModel> models = toModel( content );
            final FactModels factModels = new FactModels();
            factModels.getModels().addAll( models );

            //De-serialize imports
            final Imports imports = ImportsParser.parseImports( content );
            factModels.setImports( imports );
            return factModels;

        } catch ( final DroolsParserException e ) {
            throw new RuntimeException( e );
        }
        //TODO {porcelli} needs define error handling strategy
//            log.error( "Unable to parse the DRL for the model - falling back to text (" + e.getMessage() + ")" );
//            RuleContentText text = new RuleContentText();
//            text.content = item.getContent();
//            asset.setContent( text );
    }

    private static List<FactMetaModel> toModel( String drl )
            throws DroolsParserException {
        if ( drl != null && ( drl.startsWith( "#advanced" ) || drl.startsWith( "//advanced" ) ) ) {
            throw new DroolsParserException( "Using advanced editor" );
        }
        final DrlParser parser = new DrlParser();
        final PackageDescr pkg = parser.parse( drl );
        if ( parser.hasErrors() ) {
            throw new DroolsParserException( "The model drl " + drl + " is not valid" );
        }

        if ( pkg == null ) {
            return emptyList();
        }
        final List<TypeDeclarationDescr> types = pkg.getTypeDeclarations();
        final List<FactMetaModel> list = new ArrayList<FactMetaModel>( types.size() );
        for ( final TypeDeclarationDescr td : types ) {
            final FactMetaModel mm = new FactMetaModel();
            mm.setName( td.getTypeName() );
            mm.setSuperType( td.getSuperTypeName() );

            final Map<String, TypeFieldDescr> fields = td.getFields();
            for ( Map.Entry<String, TypeFieldDescr> en : fields.entrySet() ) {
                final String fieldName = en.getKey();
                final TypeFieldDescr descr = en.getValue();
                final FieldMetaModel fm = new FieldMetaModel( fieldName,
                                                              descr.getPattern().getObjectType() );

                mm.getFields().add( fm );
            }

            final Map<String, AnnotationDescr> annotations = td.getAnnotations();
            for ( final Map.Entry<String, AnnotationDescr> en : annotations.entrySet() ) {
                final String annotationName = en.getKey();
                final AnnotationDescr descr = en.getValue();
                final Map<String, String> values = descr.getValues();
                final AnnotationMetaModel am = new AnnotationMetaModel( annotationName,
                                                                        values );

                mm.getAnnotations().add( am );
            }

            list.add( mm );
        }

        return list;
    }

}
