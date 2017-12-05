package com.github.lhervier.domino.oauth.server.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.ext.IOAuthAuthorizeExtension;
import com.github.lhervier.domino.oauth.server.services.ExtensionService;

@Service
public class ExtensionServiceImpl implements ExtensionService {

	/**
	 * The extensions
	 */
	@Autowired
	private Map<String, IOAuthAuthorizeExtension> exts;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.ExtensionService#getResponseTypes()
	 */
	@Override
	public List<String> getResponseTypes() {
		List<String> ret = new ArrayList<String>();
		ret.addAll(this.exts.keySet());
		return ret;
	}

	/**
	 * @see com.github.lhervier.domino.oauth.server.services.ExtensionService#getExtension(java.lang.String)
	 */
	@Override
	public IOAuthAuthorizeExtension getExtension(String responseType) {
		return this.exts.get(responseType);
	}
}
