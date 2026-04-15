package com.example.dataservice.repository;

import com.example.dataservice.domain.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergyRepository extends JpaRepository<Allergy, Long> {
}
