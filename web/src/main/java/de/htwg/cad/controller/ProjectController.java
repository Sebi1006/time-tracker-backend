package de.htwg.cad.controller;

import de.htwg.cad.domain.Project;
import de.htwg.cad.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping()
    public List<Project> all() {
        return projectService.getAll();
    }

    @GetMapping("/{id}")
    public Project getProjectById(@PathVariable(value = "id") String id) {
        return projectService.getById(id);
    }

    @PostMapping()
    public Project save(@RequestBody Project project) {
        return projectService.create(project);
    }
}
