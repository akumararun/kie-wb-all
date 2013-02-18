package org.kie.guvnor.commons.ui.client.handlers;

import java.util.List;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.ui.IsWidget;
import org.kie.commons.data.Pair;
import org.uberfire.backend.vfs.Path;

/**
 * Definition of Handler to support creation of new resources
 */
public interface NewResourceHandler {

    /**
     * A description of the new resource type
     * @return
     */
    public String getDescription();

    /**
     * An icon representing the new resource type
     * @return
     */
    public IsWidget getIcon();

    /**
     * An entry-point for the creation of the new resource
     * @param context the path context where new resource should be created
     * @param baseFileName the base name of the new resource
     */
    public void create( final Path context,
                        final String baseFileName );

    /**
     * Return a List of Widgets that the NewResourceHandler can use to gather additional parameters for the
     * new resource. The List is of Pairs, where each Pair consists of a String caption and IsWidget editor.
     * @return null if no extension is provided
     */
    public List<Pair<String, ? extends IsWidget>> getExtensions();

    /**
     * Provide NewResourceHandlers with the ability to validate additional parameters before the creation of the new resource
     * @return true if validation is successful
     */
    public boolean validate();

    /**
     * Indicates if the NewResourceHandler can create a resource to this path
     * @return
     */
    void acceptPath( final Path path,
                     final Callback<Boolean, Void> callback );
}
