package com.baeldung.lsso.web.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;

import com.baeldung.lsso.web.model.ProjectModel;

@Controller
public class ProjectClientController {
    private final WebClient webClient;

    @Value("${resourceserver.api.project.url}")
    private String resourceServerUri;

    public ProjectClientController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/projects")
    public String getProjects(Model model) {
        List<ProjectModel> projects = webClient
                .get()
                .uri(resourceServerUri)
                //                .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("custom"))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProjectModel>>() {})
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

            return "redirect:/projects";
        } catch (final HttpServerErrorException e) {
            model.addAttribute("msg", e.getResponseBodyAsString());
            return "addproject";
        }
    }
}
