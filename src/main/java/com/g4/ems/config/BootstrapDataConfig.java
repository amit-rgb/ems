package com.g4.ems.config;

import com.g4.ems.domain.Project;
import com.g4.ems.repository.ProjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class BootstrapDataConfig {

    @Bean
    CommandLineRunner seedDefaultProject(ProjectRepository projectRepository) {
        return args -> projectRepository.findByPlotNoAndBlock("G-4", "Block-G")
                .orElseGet(() -> projectRepository.save(Project.builder()
                        .name("G4 Home Project")
                        .location("Chandausi-Bahjoi Road")
                        .plotNo("G-4")
                        .block("Block-G")
                        .roadWidthFeet(new BigDecimal("25"))
                        .totalBudget(new BigDecimal("5000000.00"))
                        .build()));
    }
}
