package com.github.lhervier.domino.oauth.ext.openid;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.lhervier.domino.oauth.library.server.ext.DominoServlet;

/**
 * Servlet for the "userInfo" openid endpoint
 * @author Lionel HERVIER
 */
public class UserInfoServlet extends DominoServlet {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1167165578644747248L;
	
	/**
	 * Constructeur
	 */
	public UserInfoServlet() {
		super(UserInfoConfig.class);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		UserInfoBean bean = this.getSpringContext().getBean(UserInfoBean.class);
		bean.goGet();
	}
}
