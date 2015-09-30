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

package org.optaplanner.workbench.screens.domaineditor.client.widgets.planner;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import org.uberfire.commons.data.Pair;

public interface PlannerDataObjectEditorView
        extends IsWidget {

    interface Presenter {

        void onNotInPlanningChange( boolean value );

        void onPlanningEntityChange( boolean value );

        void onPlanningSolutionChange( boolean value );

        void onPlanningSolutionScoreTypeChange();
    }

    void setPresenter( Presenter presenter );

    void setNotInPlanningValue( boolean value );

    void setPlanningEntityValue( boolean value );

    void setPlanningSolutionValue( boolean value );

    void setPlanningSolutionScoreTypeOptions( List<Pair<String, String>> planningSolutionScoreTypeOptions );

    String getPlanningSolutionScoreType();

    void setPlanningSolutionScoreType( String scoreType );

    void showPlanningSolutionScoreType( boolean show );

    void clear( );

}
