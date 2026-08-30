package com.g4.ems.controller;

import com.g4.ems.domain.Project;
import com.g4.ems.domain.enums.ConstructionStage;
import com.g4.ems.domain.enums.ExpenseCategory;
import com.g4.ems.domain.enums.ExpenseStatus;
import com.g4.ems.domain.enums.PaymentMode;
import com.g4.ems.repository.ProjectRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebDashboardController {

    private final ProjectRepository projectRepository;

    public WebDashboardController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Project defaultProject = projectRepository.findByPlotNoAndBlock("G-4", "Block-G")
                .orElse(null);

        model.addAttribute("defaultProject", defaultProject);
        model.addAttribute("expenseCategories", ExpenseCategory.values());
        model.addAttribute("constructionStages", ConstructionStage.values());
        model.addAttribute("paymentModes", PaymentMode.values());
        model.addAttribute("expenseStatuses", ExpenseStatus.values());
        return "dashboard";
    }

    @GetMapping("/vendors/register")
    public String vendorRegistration() {
        return "vendor-registration";
    }
}
