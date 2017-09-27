package com.github.lhervier.domino.oauth.client;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ComponentScan
@EnableAspectJAutoProxy(proxyTargetClass=true)
@EnableWebMvc
public class OauthClientConfig extends WebMvcConfigurerAdapter {

}
