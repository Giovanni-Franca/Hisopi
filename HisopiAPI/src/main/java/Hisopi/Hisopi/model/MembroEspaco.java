package Hisopi.Hisopi.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Tabela de junção N:N entre Usuario e Espaco, com papel de acesso.
// Numa conta PESSOAL, normalmente só existe 1 registro (o dono).
// Numa ORGANIZACAO, pode ter vários colaboradores com papéis distintos.
@Entity
@Table(
    name = "membros_espaco",
    uniqueConstraints = @UniqueConstraint(columnNames = {"espaco_id", "usuario_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class MembroEspaco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "espaco_id", nullable = false)
    private Espaco espaco;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PapelMembro papel;

    @Column(nullable = false)
    private LocalDateTime entradaEm = LocalDateTime.now();

    public enum PapelMembro {
        DONO,     // criador do espaço, permissões totais
        ADMIN,    // gerencia membros e configurações
        GERENTE,  // gerencia insumos, lotes, relatórios
        OPERADOR  // registra entradas/saídas do dia a dia
    }
}