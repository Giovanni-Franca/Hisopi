package Hisopi.Hisopi.model;

import java.time.LocalDateTime;

import Hisopi.Hisopi.Enum.TipoReceita;
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
    private Espaco espaco;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReceita tipo;

    @Column(columnDefinition = "TEXT")
    private String modoPreparo;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

}