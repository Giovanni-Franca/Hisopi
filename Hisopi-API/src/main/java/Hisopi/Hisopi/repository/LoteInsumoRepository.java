package Hisopi.Hisopi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Hisopi.Hisopi.model.LoteInsumo;


public interface LoteInsumoRepository extends JpaRepository<LoteInsumo, Long> {
    // Ordenado por validade ASC = FEFO (o primeiro a vencer vem primeiro)
    List<LoteInsumo> findByInsumoIdAndQuantidadeAtualGreaterThanOrderByDataValidadeAsc(
        Long idInsumo, Double zero);

    List<LoteInsumo> findByInsumoEspacoIdAndDataValidadeBetweenAndQuantidadeAtualGreaterThan(
        Long idEspaco, LocalDate inicio, LocalDate fim, Double zero);
}