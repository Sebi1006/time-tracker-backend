package de.htwg.cad.service;

import de.htwg.cad.domain.UserWork;

import java.util.List;

public interface UserWorkService {
    UserWork create(UserWork userWork);

    UserWork getById(String id);

    List<UserWork> getAll();

    UserWork update(UserWork userWork);
}
