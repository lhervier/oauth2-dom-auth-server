package com.github.lhervier.domino.oauth.ext.openid;

import org.springframework.stereotype.Component;

@Component
public class MessageService {

	public void message(String msg) {
		System.out.println("Message from the service : " + msg);
	}
}
