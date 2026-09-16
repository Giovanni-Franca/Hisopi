package Hisopi.Hisopi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Hisopi.Hisopi.model.ReceitaInsumo;

public interface ReceitaInsumoRepository extends JpaRepository<ReceitaInsumo, Long> {
    List<ReceitaInsumo> findByReceitaId(Long idReceita);
    List<ReceitaInsumo> findByInsumoId(Long idInsumo);
}