package com.lautaro.repository;

import com.lautaro.entitiy.HistorialPrecios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialPrecioRepository extends JpaRepository<HistorialPrecios,Integer> {
}
