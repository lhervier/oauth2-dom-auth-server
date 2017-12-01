package com.github.lhervier.domino.oauth.server.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.lhervier.domino.oauth.server.ext.IOAuthExtension;
import com.github.lhervier.domino.oauth.server.services.ExtensionService;

@Service
public class ExtensionServiceImpl implements ExtensionService {

	/**
	 * The extensions
	 */
	@Autowired
	private Map<String, IOAuthExtension<?>> exts;
	
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
	public IOAuthExtension<?> getExtension(String responseType) {
		return this.exts.get(responseType);
	}
}
