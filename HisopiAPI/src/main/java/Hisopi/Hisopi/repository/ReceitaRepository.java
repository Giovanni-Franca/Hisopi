package Hisopi.Hisopi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Hisopi.Hisopi.model.Receita;


public interface ReceitaRepository extends JpaRepository<Receita, Long> {
    List<Receita> findByEspacoId(Long idEspaco);
    List<Receita> findByTipo(Receita.TipoReceita tipo);
}