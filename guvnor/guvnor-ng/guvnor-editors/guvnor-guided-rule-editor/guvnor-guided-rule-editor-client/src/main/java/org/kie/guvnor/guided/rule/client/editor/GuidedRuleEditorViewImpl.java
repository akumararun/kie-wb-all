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

package org.kie.guvnor.guided.rule.client.editor;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.drools.guvnor.models.commons.shared.rule.RuleModel;
import org.kie.guvnor.commons.ui.client.resources.i18n.CommonConstants;
import org.kie.guvnor.datamodel.oracle.DataModelOracle;
import org.uberfire.backend.vfs.Path;
import org.uberfire.client.common.BusyPopup;

public class GuidedRuleEditorViewImpl
        extends Composite
        implements GuidedRuleEditorView {

    private final EventBus localBus = new SimpleEventBus();
    private final VerticalPanel panel = new VerticalPanel();
    private RuleModeller modeller = null;

    public GuidedRuleEditorViewImpl() {
        panel.setWidth( "100%" );
        initWidget( panel );
    }

    @Override
    public void setContent( final Path path,
                            final RuleModel model,
                            final DataModelOracle dataModel,
                            final boolean isReadOnly,
                            final boolean isDSLEnabled ) {
        modeller = new RuleModeller( path,
                                     model,
                                     dataModel,
                                     new RuleModellerWidgetFactory(),
                                     localBus,
                                     isReadOnly,
                                     isDSLEnabled );
        panel.add( this.modeller );
    }

    @Override
    public RuleModel getContent() {
        return modeller.getModel();
    }

    @Override
    public boolean isDirty() {
        //The Modeller widget isn't set until after the content has been loaded from an asynchronous call to
        //the server. It is therefore possible that the User attempts to close the tab before Modeller is set
        return ( modeller == null ) ? false : modeller.getRuleModeller().isDirty();
    }

    @Override
    public void setNotDirty() {
        modeller.resetDirty();
    }

    @Override
    public boolean confirmClose() {
        return Window.confirm( CommonConstants.INSTANCE.DiscardUnsavedData() );
    }

    @Override
    public void refresh() {
        modeller.refreshWidget();
    }

    @Override
    public void alertReadOnly() {
        Window.alert( CommonConstants.INSTANCE.CantSaveReadOnly() );
    }

    @Override
    public void showBusyIndicator( final String message ) {
        BusyPopup.showMessage( message );
    }

    @Override
    public void hideBusyIndicator() {
        BusyPopup.close();
    }

}
