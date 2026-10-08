package Hisopi.Hisopi.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Hisopi.Hisopi.model.LoteInsumo;


public interface LoteInsumoRepository extends JpaRepository<LoteInsumo, Long> {
    
	@Query("""
		    select l from LoteInsumo l
		    join fetch l.insumo i
		    where i.espaco.id = :idEspaco
		      and l.dataValidade <= :limite
		      and l.quantidadeAtual > 0
		    order by l.dataValidade asc
		    """)
		List<LoteInsumo> buscarVencidosOuVencendo(
		        @Param("idEspaco") Long idEspaco,
		        @Param("limite") LocalDate limite);
	
	// Ordenado por validade ASC = FEFO (o primeiro a vencer vem primeiro)
    List<LoteInsumo> findByInsumoIdAndQuantidadeAtualGreaterThanOrderByDataValidadeAsc(
        Long idInsumo, Double zero);

    List<LoteInsumo> findByInsumoEspacoIdAndDataValidadeBetweenAndQuantidadeAtualGreaterThan(
        Long idEspaco, LocalDate inicio, LocalDate fim, Double zero);

	Optional<LoteInsumo> findByIdAndInsumoEspacoId(Long idLote, Long idEspaco);
}