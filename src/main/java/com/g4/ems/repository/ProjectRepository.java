package com.g4.ems.repository;

import com.g4.ems.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByPlotNoAndBlock(String plotNo, String block);
}
