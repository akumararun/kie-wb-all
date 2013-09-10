package org.kie.workbench.common.widgets.client.handlers;

import java.util.List;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.ui.IsWidget;
import org.guvnor.common.services.project.context.ProjectContext;
import org.guvnor.common.services.project.model.Package;
import org.kie.commons.data.Pair;
import org.kie.workbench.common.services.shared.validation.ValidatorWithReasonCallback;

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
     * @param pkg the Package context where new resource should be created
     * @param baseFileName the base name of the new resource
     * @param presenter underlying presenter
     */
    public void create( final Package pkg,
                        final String baseFileName,
                        final NewResourcePresenter presenter );

    /**
     * Return a List of Widgets that the NewResourceHandler can use to gather additional parameters for the
     * new resource. The List is of Pairs, where each Pair consists of a String caption and IsWidget editor.
     * @return null if no extension is provided
     */
    public List<Pair<String, ? extends IsWidget>> getExtensions();

    /**
     * Provide NewResourceHandlers with the ability to validate additional parameters before the creation of the new resource
     * @param fileName The file name for the new item
     * @param callback Callback depending on validation result
     */
    public void validate( final String fileName,
                          final ValidatorWithReasonCallback callback );

    /**
     * Indicates if the NewResourceHandler can create a resource to this path
     * @return
     */
    void acceptContext( final ProjectContext context,
                        final Callback<Boolean, Void> callback );
}
