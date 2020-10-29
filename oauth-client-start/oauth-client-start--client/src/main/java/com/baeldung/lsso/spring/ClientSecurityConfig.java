package com.baeldung.lsso.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@EnableWebSecurity
public class ClientSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {// @formatter:off
        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .anyRequest().authenticated()
                .and()
                //                .oauth2Client().authorizationCodeGrant().authorizationRequestRepository();
                .oauth2Login()
                .and()
                .logout().logoutSuccessUrl("/");
    }// @formatter:on

    @Bean
    WebClient webClient(ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        // To include the access token in the request header.
        final var clientExchangeFilter =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository,
                        authorizedClientRepository);

        // All HTTP communication will possess the access token, so we need to make sure this webClient instance is
        // used only for communication with the resource server.
        clientExchangeFilter.setDefaultOAuth2AuthorizedClient(true);

        return WebClient.builder().apply(clientExchangeFilter.oauth2Configuration()).build();
    }
}