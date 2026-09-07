 package Hisopi.Hisopi.DTO;

import Hisopi.Hisopi.Enum.UserRole;

public record RegisterDTO(String nome, String login, String senha, UserRole role) {

}
