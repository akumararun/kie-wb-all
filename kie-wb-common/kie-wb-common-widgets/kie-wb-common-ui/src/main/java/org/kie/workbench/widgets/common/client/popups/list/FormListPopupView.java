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

package org.kie.workbench.widgets.common.client.popups.list;

import org.kie.commons.data.Pair;

import java.util.List;

public interface FormListPopupView {

    interface Presenter {

        void onOk();

    }

    void setPresenter( final Presenter presenter );

    void setItems( final List<Pair<String, String>> items );

    void show();

    Pair<String, String> getSelectedItem();

    void showFieldEmptyWarning();
}
