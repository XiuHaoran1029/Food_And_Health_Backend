package org.example.food_a.repository;

import org.example.food_a.entity.UserMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMedicineRepository extends JpaRepository<UserMedicine, Long> {
}
