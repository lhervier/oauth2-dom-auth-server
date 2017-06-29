package com.github.lhervier.domino.oauth.library.server.ext;

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
	private Class<?>[] configClasses;
	
	/**
	 * Constructor
	 * @param config the spring config classes
	 */
	public SpringActivator(Class<?>... configs) {
		instance = this;
		this.configClasses = new Class<?>[configs.length + 1];
		this.configClasses[0] = SpringServletConfig.class;
		for( int i=0; i<configs.length; i++ )
			this.configClasses[i+1] = configs[i];
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
			this.springContext = new AnnotationConfigApplicationContext(this.configClasses);
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
