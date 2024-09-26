package com.lautaro.repository;

import com.lautaro.entitiy.ValorNutricional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValorNutricionalRepository extends JpaRepository<ValorNutricional,Integer> {
}
