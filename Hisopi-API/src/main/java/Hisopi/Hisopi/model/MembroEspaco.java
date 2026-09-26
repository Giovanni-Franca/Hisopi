package Hisopi.Hisopi.model;

import java.time.LocalDateTime;

import Hisopi.Hisopi.Enum.PapelMembro;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "membros_espaco",
    uniqueConstraints = @UniqueConstraint(columnNames = {"espaco_id", "usuario_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class MembroEspaco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "espaco_id", nullable = false)
    private Espaco espaco;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PapelMembro papel;

    @Column(nullable = false)
    private LocalDateTime entradaEm = LocalDateTime.now();

}