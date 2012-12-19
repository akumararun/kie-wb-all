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
package org.kie.guvnor.m2repo.client.editor;

import org.uberfire.client.common.FormStyleLayout;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class M2RepoEditorView
        extends Composite
        implements M2RepoEditorPresenter.View {

    private VerticalPanel       layout;
    private FormPanel           form;
    
    public M2RepoEditorView() {
        layout = new VerticalPanel();
        doSearch();
        layout.setWidth( "100%" );
        initWidget( layout );
        setWidth( "100%" );
    }
    
    private void doSearch() {
        VerticalPanel container = new VerticalPanel();
        VerticalPanel criteria = new VerticalPanel();

        FormStyleLayout ts = new FormStyleLayout();
        
        ts.addAttribute( "Upload new Jar:", doUploadForm() );       
        
        final TextBox tx = new TextBox();
        //tx.setWidth("100px");
        ts.addAttribute( "Search for:",
                         tx );
/*
        final CheckBox archiveBox = new CheckBox();
        archiveBox.setValue( false );
        ts.addAttribute( constants.IncludeArchivedAssetsInResults(),
                         archiveBox );
*/
        Button go = new Button();
        go.setText( "Search" );
        ts.addAttribute( "",
                         go );

        
        ts.setWidth( "100%" );

        final SimplePanel resultsP = new SimplePanel();
        final ClickHandler cl = new ClickHandler() {

            public void onClick(ClickEvent arg0) {
/*                if ( tx.getText().equals( "" ) ) {
                    Window.alert( constants.PleaseEnterSomeSearchText() );
                    return;
                }*/
                resultsP.clear();
                JarListPagedTable table = new JarListPagedTable();
                resultsP.add( table );
            }

        };

        go.addClickHandler( cl );
        tx.addKeyPressHandler( new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                if ( event.getCharCode() == KeyCodes.KEY_ENTER ) {
                    cl.onClick( null );
                }
            }
        } );

        criteria.add( ts );
        container.add( criteria );
        container.add( resultsP );
        
        resultsP.clear();
        JarListPagedTable table = new JarListPagedTable();
        resultsP.add( table );
        
        layout.add(container);
    }
    
    public FormPanel doUploadForm() {
        FormPanel form = new FormPanel();
        form.setAction(GWT.getModuleBaseURL() + "file");
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);

        FileUpload up = new FileUpload();
        //up.setWidth("100px");
        up.setName(HTMLFileManagerFields.UPLOAD_FIELD_NAME_ATTACH);
        HorizontalPanel fields = new HorizontalPanel();
        fields.add(getHiddenField(HTMLFileManagerFields.FORM_FIELD_UUID, "uuid"));

        Button ok = new Button("upload");
        ok.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                showUploadingBusy();
                submitUpload();
            }
        });

        fields.add(up);
        fields.add(ok);

        form.add(fields);

        return form;
    }

    protected void submitUpload() {
        form.submit();
    }

    protected void showUploadingBusy() {
        // LoadingPopup.showMessage( "Uploading...");
    }

    private TextBox getHiddenField(String name, String value) {
        TextBox t = new TextBox();
        t.setName(name);
        t.setText(value);
        t.setVisible(false);
        return t;
    }
}
