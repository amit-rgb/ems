package com.g4.ems.controller;

import com.g4.ems.dto.DashboardResponse;
import com.g4.ems.service.ConstructionFinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectDashboardController {

    private final ConstructionFinanceService constructionFinanceService;

    @GetMapping("/{projectId}/dashboard")
    public DashboardResponse getDashboard(@PathVariable Long projectId) {
        return constructionFinanceService.getProjectDashboard(projectId);
    }
}
