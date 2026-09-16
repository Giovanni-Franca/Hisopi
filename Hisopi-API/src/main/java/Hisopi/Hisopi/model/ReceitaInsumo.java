package Hisopi.Hisopi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Ficha técnica: quanto de cada insumo uma receita consome por unidade.
@Entity
@Table(name = "receita_insumo")
@Getter
@Setter
@NoArgsConstructor
public class ReceitaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "receita_id", nullable = false)
    private Receita receita;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @Column(nullable = false)
    private Double quantidadePorUnidade;
}