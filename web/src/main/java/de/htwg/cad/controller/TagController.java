package de.htwg.cad.controller;

import de.htwg.cad.domain.Tag;
import de.htwg.cad.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping()
    public List<Tag> all() {
        return tagService.getAll();
    }

    @GetMapping("/{id}")
    public Tag getTagById(@PathVariable(value = "id") String id) {
        return tagService.getById(id);
    }

    @PostMapping()
    public Tag save(@RequestBody Tag tag) {
        return tagService.create(tag);
    }
}
