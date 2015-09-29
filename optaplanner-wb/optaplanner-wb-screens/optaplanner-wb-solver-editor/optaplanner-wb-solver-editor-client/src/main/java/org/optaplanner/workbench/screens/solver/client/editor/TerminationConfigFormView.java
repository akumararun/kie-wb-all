/*
 * Copyright 2015 JBoss Inc
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
package org.optaplanner.workbench.screens.solver.client.editor;

import com.google.gwt.user.client.ui.IsWidget;

public interface TerminationConfigFormView
        extends IsWidget {

    void setPresenter( TerminationConfigForm form );

    void setDaysSpentLimit( Long days );

    void setHoursSpentLimit( Long hours );

    void setMinutesSpentLimit( Long minutes );

    void setSecondsSpentLimit( Long seconds );

    void setUnimprovedDaysSpentLimit( Long days );

    void setUnimprovedHoursSpentLimit( Long hours );

    void setUnimprovedMinutesSpentLimit( Long minutes );

    void setUnimprovedSecondsSpentLimit( Long seconds );

    void showSpentLimit(boolean show);

    void showUnimprovedSpentLimit(boolean show);

}
