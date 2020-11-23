package com.baeldung.lsso.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class ResourceSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {// @formatter:off
        http
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, "/api/projects/**")
                    .hasAuthority("SCOPE_read")
                    .antMatchers(HttpMethod.POST, "/api/projects")
                    .hasAuthority("SCOPE_write")
                    .anyRequest()
                    .authenticated() // TODO What happens if we skip anyRequest().authenticated() and instead make specific URL only authenticated()
                .and()
                    .oauth2ResourceServer()
                    .jwt();
    }//@formatter:on
}