package com.cibertec.authservice.negocio;

import com.cibertec.authservice.entidades.Usuario;
import com.cibertec.authservice.repositorio.UsuarioRepository;
import com.cibertec.authservice.security.JwtService;
import com.cibertec.common.dto.LoginRequest;
import com.cibertec.common.dto.LoginResponse;
import com.cibertec.common.dto.RegistroRequest;
import com.cibertec.common.dto.UsuarioResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

	public static final String ROLE_ADMIN = "ADMIN";
	public static final String ROLE_VENDEDOR = "VENDEDOR";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService
	) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public LoginResponse login(LoginRequest request) {
		Usuario user = usuarioRepository.findByUsername(request.username().trim())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));

		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
		}

		return new LoginResponse(
				jwtService.generateToken(user),
				"Bearer",
				jwtService.getExpirationMinutes(),
				user.getUsername(),
				user.getNombre(),
				user.getRole()
		);
	}

	public UsuarioResponse register(RegistroRequest request) {
		if (usuarioRepository.existsByUsername(request.username().trim())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
		}
		if (usuarioRepository.existsByEmail(request.email().trim())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
		}

		Usuario user = Usuario.builder()
				.nombre(request.nombre().trim())
				.username(request.username().trim())
				.email(request.email().trim())
				.password(passwordEncoder.encode(request.password()))
				.role(ROLE_VENDEDOR)
				.build();

		Usuario saved = usuarioRepository.save(user);
		return new UsuarioResponse(saved.getId(), saved.getUsername(), saved.getNombre(), saved.getEmail(), saved.getRole());
	}
}
