package com.baeldung.lsso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Client;

import com.baeldung.lsso.web.controller.ProjectClientController;
import com.baeldung.lsso.web.service.DummyService;

import java.time.Instant;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

//@SpringBootTest
//@AutoConfigureMockMvc
@WebMvcTest
class OAuth2ClientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DummyService dummyService;

    @Autowired
    private ClientRegistrationRepository clientRepository;

    @Test
    void givenSecuredEndpoint_whenCallingEndpoint_thenRedirect() throws Exception {
        dummyService.useIt();
        mockMvc.perform(get("/profile-simple"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void givenSecuredEndpoint_whenCallingEndpoint_thenSuccess() throws Exception {
        mockMvc.perform(get("/profile-simple").with(oauth2Client("custom")))
                .andExpect(status().isOk());
    }

    @Test
    void givenOauth2Client_whenUsingScopes_thenSuccess() throws Exception {
        mockMvc.perform(get("/profile")
                .with(oauth2Client("custom")
                        .accessToken(
                                new OAuth2AccessToken(
                                        BEARER,
                                        "token",
                                        null,
                                        Instant.now(),
                                        Collections.singleton("admin.users:read")))))
                .andExpect(content().string("All users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/profile")
                .with(oauth2Client("custom")
                        .accessToken(
                                new OAuth2AccessToken(
                                        BEARER,
                                        "token",
                                        null,
                                        Instant.now(),
                                        Collections.singleton("users:read")))))
                .andExpect(content().string("Your user profile"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/profile")
                .with(oauth2Client("custom")))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenOauth2Client_whenSetPrincipalName_thenSuccess() throws Exception {
        mockMvc.perform(get("/principal-name")
                .with(oauth2Client("custom").principalName("admin@baeldung.com")))
                .andExpect(content().string("Welcome Admin!"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/principal-name")
                .with(oauth2Client("custom").principalName("admin@epam.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenClient_whenGetRegistration_thenSuccess() {
        final var clientRegistration = clientRepository.findByRegistrationId("custom");
        assertThat(clientRegistration.getClientId()).isEqualTo("lssoClient");
        assertThat(clientRegistration.getRegistrationId()).isEqualTo("custom");
    }

    @Test
    void givenRealClient_whenCallingEndpoint_thenSuccess() throws Exception {
        mockMvc.perform(get("/profile-simple")
                .with(oauth2Client().clientRegistration(clientRepository.findByRegistrationId("custom"))))
                .andExpect(status().isOk());
    }
}
