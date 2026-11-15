package Hisopi.Hisopi.model;

import java.time.LocalDateTime;

import Hisopi.Hisopi.Enum.Categoria;
import Hisopi.Hisopi.Enum.UnidadeMedida;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @Column(nullable = false, unique = true, updatable = false, length = 12)
    private String serial;

    @Column(nullable = false)
    private String nome;

    // Livre (ex: "Laticínios", "Grãos", "Embalagens") — opcional,
    // útil para filtros e relatórios por categoria.
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnidadeMedida unidadeMedida; // "g", "kg", "ml", "l", "un"

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