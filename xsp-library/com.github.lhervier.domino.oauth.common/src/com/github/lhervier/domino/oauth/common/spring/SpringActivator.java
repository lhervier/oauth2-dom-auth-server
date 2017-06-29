package com.github.lhervier.domino.oauth.common.spring;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Base Activator class for plugins that will
 * use SpringFramework.
 * You MUST implement a getDefault static method that returns the instance
 * (you can get the instance in the constructor).
 * @author Lionel HERVIER
 */
public abstract class SpringActivator extends Plugin {

	/**
	 * The ApplicationContext
	 */
	private AnnotationConfigApplicationContext springContext;
	
	/**
	 * The config classes
	 */
	private List<Class<?>> configClasses = new ArrayList<Class<?>>();
	
	/**
	 * Parent activator class
	 */
	private Class<? extends SpringActivator> parentActivator;
	
	/**
	 * Constructor
	 * @param parentActivator the parent activator
	 */
	public SpringActivator(Class<? extends SpringActivator> parentActivator) {
		System.out.println("Creating Spring Activator for : " + this.getClass().getName());
		this.parentActivator = parentActivator;
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
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}
	
	/**
	 * @return the spring context
	 */
	public synchronized ApplicationContext getSpringContext() {
		if( this.springContext != null )
			return this.springContext;
		
		// Extract the parent context
		ApplicationContext parentContext = null;
		if( this.parentActivator != null ) {
			try {
				Method m = this.parentActivator.getMethod("getDefault", new Class<?>[] {});
				SpringActivator parent = (SpringActivator) m.invoke(null, new Object[] {});
				parentContext = parent.getSpringContext();
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} finally {
			}
		}
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			Class<?>[] configs = new Class<?>[this.configClasses.size()];
			this.configClasses.toArray(configs);
			this.springContext = new AnnotationConfigApplicationContext();
			this.springContext.setParent(parentContext);
			this.springContext.register(configs);
			this.springContext.refresh();
			System.out.println("Initialized Spring Context for " + this.getClass().getName());
			return this.springContext;
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
