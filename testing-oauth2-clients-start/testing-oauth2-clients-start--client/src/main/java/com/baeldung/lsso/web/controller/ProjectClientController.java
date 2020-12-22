package com.baeldung.lsso.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.baeldung.lsso.web.model.ProjectModel;
import com.baeldung.lsso.web.service.DummyService;

@Controller
public class ProjectClientController {

    @Value("${resourceserver.api.project.url:http://localhost:8081/lsso-resource-server/api/projects/}")
    private String projectApiUrl;

    @Autowired
    private WebClient webClient;

    @Autowired
    private DummyService dummyService;

    @GetMapping("/projects")
    public String getProjects(Model model) {
        List<ProjectModel> projects = this.webClient.get()
            .uri(projectApiUrl)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<ProjectModel>>() {
            })
            .block();
        model.addAttribute("projects", projects);
        return "projects";
    }

    @GetMapping("/addproject")
    public String addNewProject(Model model) {
        model.addAttribute("project", new ProjectModel(0L, "", LocalDate.now()));
        return "addproject";
    }

    @PostMapping("/projects")
    public String saveProject(ProjectModel project, Model model) {
        try {
            this.webClient.post()
                .uri(projectApiUrl)
                .bodyValue(project)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
            return "redirect:/projects";
        } catch (final HttpServerErrorException e) {
            model.addAttribute("msg", e.getResponseBodyAsString());
            return "addproject";
        }
    }

    @ResponseBody
    @GetMapping("/profile-simple")
//    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public String getUserProfileInfo(@RegisteredOAuth2AuthorizedClient("custom") OAuth2AuthorizedClient client) {
        dummyService.useIt();
        return "Your user profile";
    }

    @ResponseBody
    @GetMapping("/profile")
    public String getUserProfileInfoWithScopes(@RegisteredOAuth2AuthorizedClient("custom") OAuth2AuthorizedClient client) {
        final var scopes = client.getAccessToken().getScopes();

        if (scopes.contains("admin.users:read")) {
            return "All users";
        } else if (scopes.contains("users:read")) {
            return "Your user profile";
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden.");
        }
    }

    @ResponseBody
    @GetMapping("/principal-name")
    public String getPrincipalName(@RegisteredOAuth2AuthorizedClient("custom") OAuth2AuthorizedClient client) {
        if (client.getPrincipalName().endsWith("@baeldung.com")) {
            return "Welcome Admin!";
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
    }
}
