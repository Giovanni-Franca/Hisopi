package Hisopi.Hisopi.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Log de auditoria de toda entrada/saída — é o que permite os
// relatórios de desperdício (sem isso você só tem o saldo final,
// sem saber quanto foi vendido/usado vs. perdido).
@Entity
@Table(name = "movimentacoes_estoque")
@Getter
@Setter
@NoArgsConstructor
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @ManyToOne
    @JoinColumn(name = "lote_id")
    private LoteInsumo lote; // pode ser nulo em ajustes manuais sem lote específico

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private Double quantidade;

    private String motivo;

    @ManyToOne
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario usuarioResponsavel;

    @Column(nullable = false)
    private LocalDateTime dataMovimentacao = LocalDateTime.now();

    public enum TipoMovimentacao {
        ENTRADA,
        SAIDA_USO,       // consumo (venda, receita preparada, uso doméstico)
        PERDA_VALIDADE,
        PERDA_OUTRO,     // quebra, contaminação, erro de manuseio etc.
        DOACAO,
        AJUSTE           // correção manual de inventário (contagem física)
    }
}