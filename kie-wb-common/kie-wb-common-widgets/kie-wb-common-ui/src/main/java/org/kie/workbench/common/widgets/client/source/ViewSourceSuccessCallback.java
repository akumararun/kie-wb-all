/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.workbench.common.widgets.client.source;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.widgets.client.source.ViewSourceView;

/**
 * Callback to set the ViewSource Widgets content
 */
public class ViewSourceSuccessCallback implements RemoteCallback<String> {

    final ViewSourceView viewSource;

    public ViewSourceSuccessCallback( final ViewSourceView viewSource ) {
        this.viewSource = viewSource;
    }

    @Override
    public void callback( final String drl ) {
        viewSource.setContent( drl );
        viewSource.hideBusyIndicator();
    }
}
