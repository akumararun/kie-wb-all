package org.kie.guvnor.datamodel.backend.server.testclasses;

/**
 * Test class to check data-types are extracted correctly by DataModelBuilder for subclasses and delegated classes
 */
public class TestSuperClass {

    private String field1;

    public String getField1() {
        return field1;
    }

    public void setField1( final String field1 ) {
        this.field1 = field1;
    }

}
