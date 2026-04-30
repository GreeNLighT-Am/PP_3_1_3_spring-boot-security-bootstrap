package com.greenlight.spring_boot_security.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.greenlight.spring_boot_security.models.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findUserById(int id);

    Optional<User> findUserByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findUserWithRolesByEmail(@Param("email") String email);

    // List<User> findAllDistinctFetchRoles();
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles")
    List<User> showAllUsers();

    //    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.firstName = :username")
    //    Optional<User> findUserWithRolesByFirstName(@Param("username") String username);

    //    Optional<User> findUserByFirstName(String username);

    //    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    //    Optional<User> showUserById(@Param("id") int id);

    // Optional<User> findUserByIdWithRoles(int id);
    // Optional<User> findByIdFetchRoles(int id);

}
