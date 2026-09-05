package Hisopi.Hisopi.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// PRODUTO_VENDA: ficha técnica de um item vendido (uso empresarial).
// SUGESTAO_CONSUMO: receita recomendada para aproveitar insumos
// perto do vencimento (uso doméstico — motor de "o que fazer com
// o que vai vencer").
@Entity
@Table(name = "receitas")
@Getter
@Setter
@NoArgsConstructor
public class Receita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "espaco_id")
    private Espaco espaco; // nulo para sugestões genéricas da plataforma

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReceita tipo;

    @Lob
    private String modoPreparo;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum TipoReceita {
        PRODUTO_VENDA,
        SUGESTAO_CONSUMO
    }
}