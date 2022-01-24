package de.htwg.cad.service;

import de.htwg.cad.domain.ProjectHours;

import java.util.List;

public interface ProjectHoursService {
    ProjectHours getById(String id);

    List<ProjectHours> getAll();
}
