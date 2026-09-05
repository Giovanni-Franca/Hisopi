package Hisopi.Hisopi.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "espacos")
@Getter
@Setter
@NoArgsConstructor
public class Espaco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    // PESSOAL: despensa/geladeira de uma pessoa física.
    // ORGANIZACAO: empresa, pode ter múltiplos membros e filiais (cada
    // filial vira outro Espaco do tipo ORGANIZACAO).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEspaco tipo;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public enum TipoEspaco {
        PESSOAL,
        ORGANIZACAO
    }
}