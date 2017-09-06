package com.github.lhervier.domino.oauth.library.client.bean;

import javax.faces.context.FacesContext;

import com.ibm.xsp.designer.context.ServletXSPContext;

public class DelegatedInitParamsBean implements InitParamsBean {

	/**
	 * Name of the parameter to be found in the xsp.properties file
	 */
	private static final String XSP_PROPERTIES = "oauth2.client.initParamsBeanName";
	
	/**
	 * The delegated bean
	 */
	private InitParamsBean delegated;
	
	/**
	 * Return the delegated objet
	 */
	private synchronized InitParamsBean getDelegated() {
		if( this.delegated != null )
			return this.delegated;
		FacesContext ctx = FacesContext.getCurrentInstance();
		
		// Get the xsp context. As it is a request scoped bean, we cannot inject it.
		ServletXSPContext xspContext = (ServletXSPContext) ctx.getApplication().getVariableResolver().resolveVariable(
				ctx, 
				"context"
		);
		
		// Get the bean to access the parameters
		String delegatedBeanName = xspContext.getProperty(XSP_PROPERTIES);
		if( delegatedBeanName == null )
			throw new RuntimeException("You must define a property named '" + XSP_PROPERTIES + "' in the xsp.properties file.");
		Object d = ctx.getApplication().getVariableResolver().resolveVariable(
				ctx, 
				delegatedBeanName
		);
		if( d == null )
			throw new RuntimeException("You must define a property named '" + XSP_PROPERTIES + "' in the xsp.properties file.");
		if( !(d instanceof InitParamsBean) )
			throw new RuntimeException("The managed bean '" + delegatedBeanName + "' must implement '" + InitParamsBean.class.getName() + "'");
		this.delegated = (InitParamsBean) d;
		return this.delegated;
	}
	
	/**
	 * @see com.github.lhervier.domino.oauth.library.client.bean.InitParamsBean#getAuthorizeEndPoint()
	 */
	@Override
	public String getAuthorizeEndPoint() {
		return this.getDelegated().getAuthorizeEndPoint();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.client.bean.InitParamsBean#getBaseURI()
	 */
	@Override
	public String getBaseURI() {
		return this.getDelegated().getBaseURI();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.client.bean.InitParamsBean#getClientId()
	 */
	@Override
	public String getClientId() {
		return this.getDelegated().getClientId();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.client.bean.InitParamsBean#getSecret()
	 */
	@Override
	public String getSecret() {
		return this.getDelegated().getSecret();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.client.bean.InitParamsBean#getTokenEndPoint()
	 */
	@Override
	public String getTokenEndPoint() {
		return this.getDelegated().getTokenEndPoint();
	}

	/**
	 * @see com.github.lhervier.domino.oauth.library.client.bean.InitParamsBean#isDisableHostNameVerifier()
	 */
	@Override
	public boolean isDisableHostNameVerifier() {
		return this.getDelegated().isDisableHostNameVerifier();
	}
}
