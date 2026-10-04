package Hisopi.Hisopi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Hisopi.Hisopi.model.ReceitaInsumo;

public interface ReceitaInsumoRepository extends JpaRepository<ReceitaInsumo, Long> {

    List<ReceitaInsumo> findByReceitaId(Long idReceita);

    Optional<ReceitaInsumo> findByIdAndReceitaId(Long id, Long idReceita);

    boolean existsByReceitaIdAndInsumoId(Long idReceita, Long idInsumo);
}