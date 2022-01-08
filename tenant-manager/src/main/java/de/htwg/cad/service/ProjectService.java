package de.htwg.cad.service;

import de.htwg.cad.domain.Project;

import java.util.List;

public interface ProjectService {
    Project create(Project project);

    Project getById(String id);

    List<Project> getAll();
}
