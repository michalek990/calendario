package com.calendario.hrnest.domain.user;

import java.util.Optional;

/** Port — implementacja (adapter) żyje w warstwie infrastructure. */
public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
