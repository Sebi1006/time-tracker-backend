package de.htwg.cad.service;

import de.htwg.cad.domain.Tag;

import java.util.List;

public interface TagService {
    Tag create(Tag tag);

    Tag getById(String id);

    List<Tag> getAll();
}
