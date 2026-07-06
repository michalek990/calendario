package com.calendario.hrnest.domain.user;

import java.util.List;
import java.util.Optional;

/** Port — implementacja (adapter) żyje w warstwie infrastructure. */
public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    List<User> findAll();

    boolean existsByEmail(String email);

    /** Czy dany użytkownik jest przełożonym co najmniej jednej osoby. */
    boolean existsBySupervisorId(Long supervisorId);

    /** Czy co najmniej jeden pracownik ma przypisany dany zakład — używane przy usuwaniu zakładu. */
    boolean existsByFacility(String facility);

    /** Wszyscy pracownicy przypisani do danego zakładu — używane przy zmianie nazwy zakładu. */
    List<User> findByFacility(String facility);
}
