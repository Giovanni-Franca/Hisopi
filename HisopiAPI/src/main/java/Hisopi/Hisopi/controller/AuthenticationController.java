package Hisopi.Hisopi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Hisopi.Hisopi.DTO.LoginResponseDTO;
import Hisopi.Hisopi.DTO.RegisterDTO;
import Hisopi.Hisopi.DTO.authenticationDTO;
import Hisopi.Hisopi.infra.security.TokenService;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.UsuarioRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private UsuarioRepository repU;
	@Autowired 
	TokenService tokenService;
	
	@PostMapping("/login")
	public ResponseEntity login(@RequestBody @Valid authenticationDTO data) {
		var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(),data.senha());
		var auth = this.authenticationManager.authenticate(usernamePassword);
		
		var token = tokenService.generateToken((Usuario) auth.getPrincipal());
		
		return ResponseEntity.ok(new LoginResponseDTO(token));
	}
	
	@PostMapping("/register")
	public ResponseEntity register(@RequestBody @Valid RegisterDTO data) {
		if(this.repU.findByEmail(data.login()) != null) return ResponseEntity.badRequest().build();
		String encryptedPassword = new BCryptPasswordEncoder().encode(data.senha());
		Usuario newUser = new Usuario(data.nome(), data.login(), encryptedPassword, data.role());
		
		this.repU.save(newUser);
		
		return ResponseEntity.ok().build();
	}
}
