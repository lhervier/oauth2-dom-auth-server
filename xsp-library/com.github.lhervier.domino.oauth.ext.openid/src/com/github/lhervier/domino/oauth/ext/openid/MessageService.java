package com.github.lhervier.domino.oauth.ext.openid;

import org.springframework.stereotype.Component;

@Component
public class MessageService {

	public String getMessage(String msg) {
		return "Message from the service : " + msg;
	}
}
