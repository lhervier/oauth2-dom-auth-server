package com.github.lhervier.domino.oauth.library.server;

import com.ibm.xsp.library.AbstractXspLibrary;

public class OauthServerLibrary extends AbstractXspLibrary {
	@Override
    public String getLibraryId() {
        return "oauth.server";
    }
    @Override
    public String getPluginId() {
        return Activator.PLUGIN_ID;
    }
    @Override
    public String[] getFacesConfigFiles() {
        return new String[] {
        		"/com/github/lhervier/domino/oauth/library/server/config/faces-config.xml"
        };
    }
    @Override
    public String[] getXspConfigFiles() {
        return new String[] {};
    }
}
