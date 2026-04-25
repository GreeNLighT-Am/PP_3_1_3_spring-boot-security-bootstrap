package com.greenlight.spring_boot_security.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.greenlight.spring_boot_security.models.Role;

import java.util.Collection;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Collection<Role> findByIdIn(List<Integer> ids);
}
