package io.astrodesk.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<DbUserEntity, Long> {
    Optional<DbUserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
