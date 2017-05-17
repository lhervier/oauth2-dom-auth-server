package com.github.lhervier.domino.oauth.library.client;
import org.eclipse.core.runtime.Plugin;
public class Activator extends Plugin {
    public static final String PLUGIN_ID = Activator.class.getPackage().getName();
    private static Activator instance;
    public Activator() {
        instance = this;
    }
    public static Activator getDefault() {
        return instance;
    }
}
