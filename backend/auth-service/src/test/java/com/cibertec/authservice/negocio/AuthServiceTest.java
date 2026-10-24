package com.cibertec.authservice.negocio;

import com.cibertec.common.dto.LoginRequest;
import com.cibertec.common.dto.LoginResponse;
import com.cibertec.common.dto.RegistroRequest;
import com.cibertec.authservice.entidades.Usuario;
import com.cibertec.authservice.repositorio.UsuarioRepository;
import com.cibertec.authservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private JwtService jwtService;

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(usuarioRepository, passwordEncoder, jwtService);
	}

	private Usuario usuarioConPassword(String passwordPlano) {
		return Usuario.builder()
				.id(1L)
				.nombre("Administrador")
				.username("admin")
				.email("admin@demo.com")
				.password(passwordEncoder.encode(passwordPlano))
				.role("ADMIN")
				.build();
	}

	@Test
	@DisplayName("Login correcto devuelve el token JWT y los datos del usuario")
	void loginCorrecto() {
		Usuario user = usuarioConPassword("admin123");
		when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(user));
		when(jwtService.generateToken(user)).thenReturn("token-jwt");
		when(jwtService.getExpirationMinutes()).thenReturn(120L);

		LoginResponse response = authService.login(new LoginRequest("admin", "admin123"));

		assertEquals("token-jwt", response.token());
		assertEquals("Bearer", response.tokenType());
		assertEquals("ADMIN", response.role());
	}

	@Test
	@DisplayName("Login con password incorrecto responde 401")
	void loginPasswordIncorrecto() {
		when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioConPassword("admin123")));

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> authService.login(new LoginRequest("admin", "incorrecto")));

		assertEquals(401, ex.getStatusCode().value());
	}

	@Test
	@DisplayName("Login con usuario inexistente responde 401")
	void loginUsuarioInexistente() {
		when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> authService.login(new LoginRequest("fantasma", "loquesea")));

		assertEquals(401, ex.getStatusCode().value());
	}

	@Test
	@DisplayName("Registrar un usuario guarda el password cifrado con BCrypt y rol VENDEDOR")
	void registroCifraPassword() {
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

		authService.register(new RegistroRequest("Maria Lopez", "maria", "maria@demo.com", "secret123"));

		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(captor.capture());
		Usuario guardado = captor.getValue();

		assertNotEquals("secret123", guardado.getPassword());
		assertTrue(passwordEncoder.matches("secret123", guardado.getPassword()));
		assertEquals("VENDEDOR", guardado.getRole());
	}

	@Test
	@DisplayName("Registrar un usuario repetido responde 409")
	void registroUsuarioDuplicado() {
		when(usuarioRepository.existsByUsername("admin")).thenReturn(true);

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> authService.register(new RegistroRequest("Otro", "admin", "otro@demo.com", "secret123")));

		assertEquals(409, ex.getStatusCode().value());
	}
}

