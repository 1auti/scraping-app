package com.lautaro.repository;

import com.lautaro.entitiy.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagenRepository  extends JpaRepository<Imagen,Integer> {
}
