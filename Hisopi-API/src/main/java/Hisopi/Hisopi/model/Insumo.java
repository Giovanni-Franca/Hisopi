package Hisopi.Hisopi.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "insumos")
@Getter
@Setter
@NoArgsConstructor
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "espaco_id", nullable = false)
    private Espaco espaco;

    @Column(nullable = false)
    private String nome;

    // Livre (ex: "Laticínios", "Grãos", "Embalagens") — opcional,
    // útil para filtros e relatórios por categoria.
    private String categoria;

    @Column(nullable = false)
    private String unidadeMedida; // "g", "kg", "ml", "l", "un"

    // Saldo agregado, mantido pelo service ao registrar movimentações —
    // evita ter que somar todos os lotes a cada leitura.
    @Column(nullable = false)
    private Double estoqueAtual = 0.0;

    @Column(nullable = false)
    private Double estoqueMinimo;

    private Double custoUnitario;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}