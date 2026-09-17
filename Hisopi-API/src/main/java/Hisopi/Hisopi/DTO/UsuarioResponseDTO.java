package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.UserRole;

public record UsuarioResponseDTO(Long id,String nome,String email, UserRole role) {

}
