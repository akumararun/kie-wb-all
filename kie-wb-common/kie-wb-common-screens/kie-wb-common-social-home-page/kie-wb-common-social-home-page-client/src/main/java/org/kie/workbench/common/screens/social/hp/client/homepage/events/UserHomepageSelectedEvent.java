package org.kie.workbench.common.screens.social.hp.client.homepage.events;

public class UserHomepageSelectedEvent {

    private final String socialUserName;

    public UserHomepageSelectedEvent( String socialUserName ) {
        this.socialUserName = socialUserName;
    }

    public String getSocialUserName() {
        return socialUserName;
    }
}
