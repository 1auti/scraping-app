package com.lautaro.repository;

import com.lautaro.entitiy.Supermercado;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SupermercadoRepository extends JpaRepository<Supermercado,Integer> {

    Optional<Supermercado> findByNombre(String nombre);

}
