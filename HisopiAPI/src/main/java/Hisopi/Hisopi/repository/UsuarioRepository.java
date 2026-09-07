package Hisopi.Hisopi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import Hisopi.Hisopi.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
	
}
