package com.healthcare.repository;

import com.healthcare.model.Doctor;
import com.healthcare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUser(User user);
    Optional<Doctor> findByUserId(Long userId);
    List<Doctor> findByDepartment(String department);

    @Query("SELECT d FROM Doctor d WHERE LOWER(d.user.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(d.department) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Doctor> searchByNameOrDepartment(@Param("name") String name);
}
