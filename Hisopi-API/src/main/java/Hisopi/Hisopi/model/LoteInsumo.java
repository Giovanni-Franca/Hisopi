package Hisopi.Hisopi.model;


import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Cada "entrada" física do insumo, com validade própria — é o que
// permite FEFO (First-Expire-First-Out) e alertas de vencimento.
@Entity
@Table(name = "lotes_insumo")
@Getter
@Setter
@NoArgsConstructor
public class LoteInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @Column(nullable = false)
    private Double quantidadeInicial;

    @Column(nullable = false)
    private Double quantidadeAtual;

    @Column(nullable = false)
    private LocalDate dataEntrada;

    @Column(nullable = false)
    private LocalDate dataValidade;

    private String fornecedor;
}