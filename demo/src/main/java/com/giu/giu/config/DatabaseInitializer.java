package com.giu.giu.config;

import com.giu.giu.model.Rol;
import com.giu.giu.model.Usuario;
import com.giu.giu.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate,
                               UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureActivoColumn();
        ensureAdminUser();
    }

    private void ensureActivoColumn() {
        List<String> columns = jdbcTemplate.query("PRAGMA table_info('usuarios')",
            (rs, rowNum) -> rs.getString("name"));

        if (!columns.contains("activo")) {
            jdbcTemplate.execute("ALTER TABLE usuarios ADD COLUMN activo INTEGER NOT NULL DEFAULT 1");
        }
    }

    private void ensureAdminUser() {
        usuarioRepository.findByEmail("admin@admin").orElseGet(() -> {
            Usuario admin = new Usuario();
            admin.setEmail("admin@admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRol(Rol.ADMINISTRADOR);
            admin.setActivo(true);
            return usuarioRepository.save(admin);
        });
    }
}
