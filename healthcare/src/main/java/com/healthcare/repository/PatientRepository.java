package com.healthcare.repository;

import com.healthcare.model.Patient;
import com.healthcare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUser(User user);
    Optional<Patient> findByUserId(Long userId);

    @Query("SELECT p FROM Patient p WHERE LOWER(p.user.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Patient> searchByNameOrEmail(@Param("name") String name);
}
