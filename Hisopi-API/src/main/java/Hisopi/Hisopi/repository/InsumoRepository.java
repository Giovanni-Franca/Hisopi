package Hisopi.Hisopi.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Hisopi.Hisopi.model.Insumo;


public interface InsumoRepository extends JpaRepository<Insumo, Long> {
    List<Insumo> findByEspacoIdAndAtivoTrue(Long idEspaco);
}