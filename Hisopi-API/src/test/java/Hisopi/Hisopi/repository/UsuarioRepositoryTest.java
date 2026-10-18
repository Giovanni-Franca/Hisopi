package Hisopi.Hisopi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import Hisopi.Hisopi.Enum.UserRole;
import Hisopi.Hisopi.infra.exception.ErrorResponseWriter;
import Hisopi.Hisopi.model.Usuario;
import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTest {

    @MockitoBean
    private ErrorResponseWriter errorResponseWriter;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    private Usuario criarUsuario(String email) {
        return new Usuario("Maria", email, "senha123", UserRole.ADMIN);
    }

    @Test
    void deveSalvarUsuarioEPreencherCriadoEm() {
        Usuario salvo = usuarioRepository.save(criarUsuario("maria@email.com"));

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getCriadoEm()).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void devePersistirERecuperarTodasAsRoles(UserRole role) {
        Usuario u = criarUsuario("maria@email.com");
        u.setRole(role);
        Usuario salvo = usuarioRepository.saveAndFlush(u);
        entityManager.clear();

        Usuario recuperado = usuarioRepository.findById(salvo.getId()).orElseThrow();

        assertThat(recuperado.getRole()).isEqualTo(role);
    }

    @Test
    void deveBuscarUserDetailsPorEmail() {
        usuarioRepository.save(criarUsuario("maria@email.com"));

        UserDetails resultado = usuarioRepository.findByEmail("maria@email.com");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsername()).isEqualTo("maria@email.com");
        assertThat(resultado.getPassword()).isEqualTo("senha123");
    }

    @Test
    void deveRetornarNullParaEmailInexistente() {
        UserDetails resultado = usuarioRepository.findByEmail("inexistente@email.com");

        assertThat(resultado).isNull();
    }

    @Test
    void deveDistinguirEmailsDiferentes() {
        usuarioRepository.save(criarUsuario("maria@email.com"));
        usuarioRepository.save(criarUsuario("joao@email.com"));

        UserDetails resultado = usuarioRepository.findByEmail("joao@email.com");

        assertThat(resultado.getUsername()).isEqualTo("joao@email.com");
    }

    @Test
    void naoDevePermitirEmailDuplicado() {
        usuarioRepository.saveAndFlush(criarUsuario("maria@email.com"));

        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(criarUsuario("maria@email.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarUsuarioSemNome() {
        Usuario u = criarUsuario("a@email.com");
        u.setNome(null);

        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(u))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarUsuarioSemEmail() {
        Usuario u = criarUsuario(null);

        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(u))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarUsuarioSemSenha() {
        Usuario u = criarUsuario("a@email.com");
        u.setSenha(null);

        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(u))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDeveSalvarUsuarioSemRole() {
        Usuario u = criarUsuario("a@email.com");
        u.setRole(null);

        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(u))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}