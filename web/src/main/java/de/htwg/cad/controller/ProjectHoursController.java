package de.htwg.cad.controller;

import de.htwg.cad.domain.ProjectHours;
import de.htwg.cad.service.ProjectHoursService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project-hours")
public class ProjectHoursController {
    private final ProjectHoursService projectHoursService;

    public ProjectHoursController(ProjectHoursService projectHoursService) {
        this.projectHoursService = projectHoursService;
    }

    @GetMapping()
    public List<ProjectHours> all() {
        return projectHoursService.getAll();
    }

    @GetMapping("/{id}")
    public ProjectHours getProjectHoursById(@PathVariable(value = "id") String id) {
        return projectHoursService.getById(id);
    }
}
