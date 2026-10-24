package com.cibertec.authservice.config;

import com.cibertec.authservice.entidades.Usuario;
import com.cibertec.authservice.negocio.AuthService;
import com.cibertec.authservice.repositorio.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

	@Bean
	public CommandLineRunner initUsers(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!usuarioRepository.existsByUsername("admin")) {
				usuarioRepository.save(Usuario.builder()
						.nombre("Administrador Principal")
						.username("admin")
						.email("admin@bodegapro.pe")
						.password(passwordEncoder.encode("admin123"))
						.role(AuthService.ROLE_ADMIN)
						.build());
			}
			if (!usuarioRepository.existsByUsername("vendedor")) {
				usuarioRepository.save(Usuario.builder()
						.nombre("Vendedor Turno Mañana")
						.username("vendedor")
						.email("vendedor@bodegapro.pe")
						.password(passwordEncoder.encode("vendedor123"))
						.role(AuthService.ROLE_VENDEDOR)
						.build());
			}
		};
	}
}
