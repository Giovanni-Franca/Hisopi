package Hisopi.Hisopi.model;

import java.time.LocalDateTime;

import Hisopi.Hisopi.Enum.TipoEspaco;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEspaco tipo;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

}