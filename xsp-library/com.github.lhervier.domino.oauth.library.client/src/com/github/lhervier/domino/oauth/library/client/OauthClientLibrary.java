package com.github.lhervier.domino.oauth.library.client;

import com.ibm.xsp.library.AbstractXspLibrary;

public class OauthClientLibrary extends AbstractXspLibrary {
	@Override
    public String getLibraryId() {
        return "oauth.client";
    }
    @Override
    public String getPluginId() {
        return Activator.PLUGIN_ID;
    }
    @Override
    public String[] getFacesConfigFiles() {
        return new String[] {
        		"/com/github/lhervier/domino/oauth/library/client/config/faces-config.xml"
        };
    }
    @Override
    public String[] getXspConfigFiles() {
        return new String[] {};
    }
}
