package com.lautaro.repository;

import com.lautaro.entitiy.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificadoRepository  extends JpaRepository<Certificado,Integer> {
}
