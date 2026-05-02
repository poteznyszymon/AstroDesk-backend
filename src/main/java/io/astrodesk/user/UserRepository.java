package io.astrodesk.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<DbUserEntity, Long> {
    Optional<DbUserEntity> findByUsername(String username);
    Optional<DbUserEntity> findByFirstNameAndLastName(String firstName, String lastName);
    boolean existsByUsername(String username);
    List<DbUserEntity> findByRoleIn(List<UserRole> roles);
}
