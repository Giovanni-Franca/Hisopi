package Hisopi.Hisopi.repository;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Hisopi.Hisopi.model.MembroEspaco;


public interface MembroEspacoRepository extends JpaRepository<MembroEspaco, Long> {
    List<MembroEspaco> findByEspacoId(Long idEspaco);
    Optional<MembroEspaco> findByEspacoIdAndUsuarioId(Long idEspaco, Long idUsuario);
    boolean existsByEspacoIdAndUsuarioId(Long idEspaco, Long idUsuario);
}