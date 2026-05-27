package io.astrodesk.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<DbUserEntity, Long> {
    Optional<DbUserEntity> findByUsername(String username);
    Optional<DbUserEntity> findByFirstNameAndLastName(String firstName, String lastName);
    boolean existsByUsername(String username);
    List<DbUserEntity> findByRoleIn(List<UserRole> roles);

    @Query("SELECT u.userId FROM DbUserEntity u WHERE u.username = :username")
    Optional<Long> findUserIdByUsername(@Param("username") String username);
}
