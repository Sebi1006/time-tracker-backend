package de.htwg.cad.controller;

import de.htwg.cad.domain.Work;
import de.htwg.cad.service.WorkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/work")
public class WorkController {
    private final WorkService workService;

    public WorkController(WorkService workService) {
        this.workService = workService;
    }

    @GetMapping()
    public List<Work> all() {
        return workService.getAll();
    }

    @GetMapping("/{id}")
    public Work getWorkById(@PathVariable(value = "id") String id) {
        return workService.getById(id);
    }

    @PostMapping()
    public Work save(@RequestBody Work work) {
        return workService.create(work);
    }
}
