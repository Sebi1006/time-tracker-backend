package de.htwg.cad.service;

import de.htwg.cad.domain.Work;

import java.util.List;

public interface WorkService {
    Work create(Work work);

    Work getById(String id);

    List<Work> getAll();
}
