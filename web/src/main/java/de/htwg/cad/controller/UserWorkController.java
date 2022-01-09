package de.htwg.cad.controller;

import de.htwg.cad.domain.UserWork;
import de.htwg.cad.service.UserWorkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user-work")
public class UserWorkController {
    private final UserWorkService userWorkService;

    public UserWorkController(UserWorkService userWorkService) {
        this.userWorkService = userWorkService;
    }

    @GetMapping()
    public List<UserWork> all() {
        return userWorkService.getAll();
    }

    @GetMapping("/{id}")
    public UserWork getUserWorkById(@PathVariable(value = "id") String id) {
        return userWorkService.getById(id);
    }

    @PostMapping()
    public UserWork save(@RequestBody UserWork userWork) {
        return userWorkService.create(userWork);
    }

    @PutMapping(value = "/{id}")
    public UserWork update(@PathVariable("id") String id, @RequestBody UserWork userWork) {
        return userWorkService.update(userWork, id);
    }
}
