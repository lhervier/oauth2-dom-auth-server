package com.github.lhervier.domino.oauth.server.services.impl;

import java.util.List;

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
	private List<IOAuthExtension<?>> exts;
	
	/**
	 * @see com.github.lhervier.domino.oauth.server.services.ExtensionService#getExtensions()
	 */
	@Override
	public List<IOAuthExtension<?>> getExtensions() {
		return this.exts;
	}

}
