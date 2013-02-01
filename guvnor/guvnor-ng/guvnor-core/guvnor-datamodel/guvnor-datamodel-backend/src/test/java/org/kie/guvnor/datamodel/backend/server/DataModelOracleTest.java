package org.kie.guvnor.datamodel.backend.server;

import java.io.IOException;

import org.junit.Test;
import org.kie.guvnor.datamodel.backend.server.builder.packages.PackageDataModelOracleBuilder;
import org.kie.guvnor.datamodel.oracle.ProjectDefinition;
import org.kie.guvnor.datamodel.backend.server.builder.projects.ProjectDefinitionBuilder;
import org.kie.guvnor.datamodel.backend.server.testclasses.TestDataTypes;
import org.kie.guvnor.datamodel.backend.server.testclasses.TestDelegatedClass;
import org.kie.guvnor.datamodel.backend.server.testclasses.TestSubClass;
import org.kie.guvnor.datamodel.backend.server.testclasses.TestSuperClass;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.kie.guvnor.datamodel.oracle.DataType;

import static org.junit.Assert.*;

/**
 * Tests for the ProjectDefinition
 */
public class DataModelOracleTest {

    @Test
    public void testDataTypes() throws IOException {
        final ProjectDefinition pd = ProjectDefinitionBuilder.newProjectDefinitionBuilder()
                .addClass( TestDataTypes.class )
                .build();

        final DataModelOracle dmo = PackageDataModelOracleBuilder.newDataModelBuilder( "org.kie.guvnor.datamodel.backend.server.testclasses" ).setProjectDefinition( pd ).build();

        assertEquals( 1,
                      dmo.getFactTypes().length );
        assertEquals( TestDataTypes.class.getSimpleName(),
                      dmo.getFactTypes()[ 0 ] );

        assertEquals( 20,
                      dmo.getFieldCompletions( TestDataTypes.class.getSimpleName() ).length );

        assertEquals( DataType.TYPE_THIS,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "this" ) );
        assertEquals( DataType.TYPE_STRING,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldString" ) );
        assertEquals( DataType.TYPE_BOOLEAN,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldBooleanObject" ) );
        assertEquals( DataType.TYPE_DATE,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldDate" ) );
        assertEquals( DataType.TYPE_NUMERIC_BIGDECIMAL,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldNumeric" ) );
        assertEquals( DataType.TYPE_NUMERIC_BIGDECIMAL,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldBigDecimal" ) );
        assertEquals( DataType.TYPE_NUMERIC_BIGINTEGER,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldBigInteger" ) );
        assertEquals( DataType.TYPE_NUMERIC_BYTE,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldByteObject" ) );
        assertEquals( DataType.TYPE_NUMERIC_DOUBLE,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldDoubleObject" ) );
        assertEquals( DataType.TYPE_NUMERIC_FLOAT,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldFloatObject" ) );
        assertEquals( DataType.TYPE_NUMERIC_INTEGER,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldIntegerObject" ) );
        assertEquals( DataType.TYPE_NUMERIC_LONG,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldLongObject" ) );
        assertEquals( DataType.TYPE_NUMERIC_SHORT,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldShortObject" ) );
        assertEquals( DataType.TYPE_BOOLEAN,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldBooleanPrimitive" ) );
        assertEquals( DataType.TYPE_NUMERIC_BYTE,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldBytePrimitive" ) );
        assertEquals( DataType.TYPE_NUMERIC_DOUBLE,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldDoublePrimitive" ) );
        assertEquals( DataType.TYPE_NUMERIC_FLOAT,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldFloatPrimitive" ) );
        assertEquals( DataType.TYPE_NUMERIC_INTEGER,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldIntegerPrimitive" ) );
        assertEquals( DataType.TYPE_NUMERIC_LONG,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldLongPrimitive" ) );
        assertEquals( DataType.TYPE_NUMERIC_SHORT,
                      dmo.getFieldType( TestDataTypes.class.getSimpleName(),
                                        "fieldShortPrimitive" ) );
    }

    @Test
    public void testSuperClass() throws IOException {
        final ProjectDefinition pd = ProjectDefinitionBuilder.newProjectDefinitionBuilder()
                .addClass( TestSuperClass.class )
                .build();

        final DataModelOracle dmo = PackageDataModelOracleBuilder.newDataModelBuilder( "org.kie.guvnor.datamodel.backend.server.testclasses" ).setProjectDefinition( pd ).build();

        assertEquals( 1,
                      dmo.getFactTypes().length );
        assertEquals( TestSuperClass.class.getSimpleName(),
                      dmo.getFactTypes()[ 0 ] );

        assertEquals( 2,
                      dmo.getFieldCompletions( TestSuperClass.class.getSimpleName() ).length );

        assertEquals( DataType.TYPE_THIS,
                      dmo.getFieldType( TestSuperClass.class.getSimpleName(),
                                        "this" ) );
        assertEquals( DataType.TYPE_STRING,
                      dmo.getFieldType( TestSuperClass.class.getSimpleName(),
                                        "field1" ) );
    }

    @Test
    public void testSubClass() throws IOException {
        final ProjectDefinition pd = ProjectDefinitionBuilder.newProjectDefinitionBuilder()
                .addClass( TestSubClass.class )
                .build();

        final DataModelOracle dmo = PackageDataModelOracleBuilder.newDataModelBuilder( "org.kie.guvnor.datamodel.backend.server.testclasses" ).setProjectDefinition( pd ).build();

        assertEquals( 1,
                      dmo.getFactTypes().length );
        assertEquals( TestSubClass.class.getSimpleName(),
                      dmo.getFactTypes()[ 0 ] );

        assertEquals( 3,
                      dmo.getFieldCompletions( TestSubClass.class.getSimpleName() ).length );

        assertEquals( DataType.TYPE_THIS,
                      dmo.getFieldType( TestSubClass.class.getSimpleName(),
                                        "this" ) );
        assertEquals( DataType.TYPE_STRING,
                      dmo.getFieldType( TestSubClass.class.getSimpleName(),
                                        "field1" ) );
        assertEquals( DataType.TYPE_STRING,
                      dmo.getFieldType( TestSubClass.class.getSimpleName(),
                                        "field2" ) );
    }

    @Test
    public void testDelegatedClass() throws IOException {
        final ProjectDefinition pd = ProjectDefinitionBuilder.newProjectDefinitionBuilder()
                .addClass( TestDelegatedClass.class )
                .build();

        final DataModelOracle dmo = PackageDataModelOracleBuilder.newDataModelBuilder( "org.kie.guvnor.datamodel.backend.server.testclasses" ).setProjectDefinition( pd ).build();

        assertEquals( 1,
                      dmo.getFactTypes().length );
        assertEquals( TestDelegatedClass.class.getSimpleName(),
                      dmo.getFactTypes()[ 0 ] );

        assertEquals( 2,
                      dmo.getFieldCompletions( TestDelegatedClass.class.getSimpleName() ).length );

        assertEquals( DataType.TYPE_THIS,
                      dmo.getFieldType( TestDelegatedClass.class.getSimpleName(),
                                        "this" ) );
        assertEquals( DataType.TYPE_STRING,
                      dmo.getFieldType( TestDelegatedClass.class.getSimpleName(),
                                        "field1" ) );
    }

}
