package Hisopi.Hisopi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Hisopi.Hisopi.Enum.TipoReceita;
import Hisopi.Hisopi.model.Receita;


public interface ReceitaRepository extends JpaRepository<Receita, Long> {
    List<Receita> findByEspacoId(Long idEspaco);
    List<Receita> findByTipo(TipoReceita tipo);
    Optional<Receita> findByIdAndEspacoId(Long id, Long idEspaco);
    List<Receita> findByEspacoIdAndTipo(Long idEspaco, TipoReceita tipo);
}