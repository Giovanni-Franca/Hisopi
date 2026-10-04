package Hisopi.Hisopi.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import Hisopi.Hisopi.Enum.TipoMovimentacao;
import Hisopi.Hisopi.model.MovimentacaoEstoque;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByInsumoEspacoIdAndTipoAndDataMovimentacaoBetween(
        Long idEspaco, TipoMovimentacao tipo,
        LocalDateTime inicio, LocalDateTime fim);
    
    @EntityGraph(attributePaths = "insumo")
    List<MovimentacaoEstoque> findByInsumoIdOrderByDataMovimentacaoDesc(Long idInsumo);

    @Query("""
            select m from MovimentacaoEstoque m
            join fetch m.insumo i
            where i.espaco.id = :idEspaco
              and m.tipo in :tipos
              and m.dataMovimentacao >= :inicio
              and m.dataMovimentacao < :fimExclusivo
            order by m.dataMovimentacao desc
            """)
    List<MovimentacaoEstoque> buscarPorTipos(
            @Param("idEspaco") Long idEspaco,
            @Param("tipos") Collection<TipoMovimentacao> tipos,
            @Param("inicio") LocalDateTime inicio,
            @Param("fimExclusivo") LocalDateTime fimExclusivo);

}