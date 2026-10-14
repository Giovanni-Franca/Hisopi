
package Hisopi.Hisopi.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import Hisopi.Hisopi.Enum.TipoNotificacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "notificacoes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notificacao_lote_tipo_validade",
                columnNames = {
                        "lote_id",
                        "tipo",
                        "data_validade_referencia"
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "espaco_id", nullable = false)
    private Espaco espaco;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private LoteInsumo lote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacao tipo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Column(name = "data_validade_referencia", nullable = false)
    private LocalDate dataValidadeReferencia;

    @Column(nullable = false)
    private LocalDateTime criadaEm = LocalDateTime.now();

    @Column(nullable = false)
    private boolean lida = false;
}
