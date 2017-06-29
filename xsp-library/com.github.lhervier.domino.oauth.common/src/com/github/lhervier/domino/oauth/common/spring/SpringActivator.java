package com.github.lhervier.domino.oauth.common.spring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Base Activator class for plugins that will
 * use SpringFramework
 * @author Lionel HERVIER
 */
public abstract class SpringActivator extends Plugin {

	/**
	 * The instance
	 */
	private static SpringActivator instance;
	
	/**
	 * The ApplicationContext
	 */
	private AnnotationConfigApplicationContext springContext;
	
	/**
	 * The config classes
	 */
	private List<Class<?>> configClasses = new ArrayList<Class<?>>();
	
	/**
	 * Constructor
	 * @param config the spring config classes
	 */
	public SpringActivator() {
		instance = this;
		this.configClasses = new ArrayList<Class<?>>();
	}
	
	/**
	 * To declare a new config class
	 * @param cls the classes
	 */
	protected final void addConfig(Class<?>... cls) {
		for( Class<?> cl : cls )
			this.configClasses.add(cl);
	}
	
	/**
	 * @return the current instance
	 */
	public static SpringActivator getDefault() {
		return instance;
	}
	
	/**
	 * @return the spring context
	 */
	public ApplicationContext getSpringContext() {
		return this.springContext;
	}
	
	/**
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			Class<?>[] configs = new Class<?>[this.configClasses.size()];
			this.configClasses.toArray(configs);
			this.springContext = new AnnotationConfigApplicationContext(configs);
		} finally {
			Thread.currentThread().setContextClassLoader(loader);
		}
	}

	/**
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		this.springContext.close();
		super.stop(context);
	}
	
	
}
