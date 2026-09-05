package Hisopi.Hisopi.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Hisopi.Hisopi.model.MovimentacaoEstoque;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    List<MovimentacaoEstoque> findByInsumoIdOrderByDataMovimentacaoDesc(Long idInsumo);

    List<MovimentacaoEstoque> findByInsumoEspacoIdAndTipoAndDataMovimentacaoBetween(
        Long idEspaco, MovimentacaoEstoque.TipoMovimentacao tipo,
        LocalDateTime inicio, LocalDateTime fim);
}