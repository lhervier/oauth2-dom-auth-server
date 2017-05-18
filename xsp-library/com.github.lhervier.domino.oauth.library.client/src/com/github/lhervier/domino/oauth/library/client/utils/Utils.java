package com.github.lhervier.domino.oauth.library.client.utils;

import com.github.lhervier.domino.oauth.common.utils.JSFUtils;
import com.github.lhervier.domino.oauth.library.client.bean.ParamsBean;

public class Utils {

	/**
	 * Retourne la bean de paramétrage
	 * @return la bean de paramétrage
	 */
	public static final ParamsBean getParamsBean() {
		return (ParamsBean) JSFUtils.getBean("paramsBean");
	}
}
