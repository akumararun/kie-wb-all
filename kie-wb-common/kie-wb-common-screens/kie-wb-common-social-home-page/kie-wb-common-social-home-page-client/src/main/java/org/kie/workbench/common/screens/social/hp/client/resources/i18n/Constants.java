/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.screens.social.hp.client.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface Constants
        extends Messages {

    public static final Constants INSTANCE = GWT.create(Constants.class);

    String PeoplePerspective();

    String RecentAssets();

    String Error_NoAccessRights();

    String Home();

    String PaginationMore();

    String LatestChanges();

    String AllRepositories();

    String ShowingUpdatesFor();

    String UserProfile();

    String UserRecentActivities();

    String SearchUsers();

    String UserName();

    String Email();

    String RealName();

    String EditUser();

    String NoSocialConnections();

    String Connections();
}
