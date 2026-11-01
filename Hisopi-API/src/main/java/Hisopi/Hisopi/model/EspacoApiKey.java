package Hisopi.Hisopi.model;

import java.time.LocalDateTime;
import java.util.UUID;

import Hisopi.Hisopi.Enum.PapelMembro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "espaco_api_keys")
@Getter
@Setter
@NoArgsConstructor
public class EspacoApiKey {

    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String nome;
    @Column(nullable = false, length = 12)
    private String prefixo;
    @Column(nullable = false, unique = true, length = 64)
    private String hash;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Espaco espaco;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PapelMembro papel;
    private boolean ativa = true;
    private LocalDateTime criadaEm = LocalDateTime.now();
    private LocalDateTime ultimoUso;

}
