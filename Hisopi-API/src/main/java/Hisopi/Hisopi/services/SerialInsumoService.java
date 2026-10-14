
package Hisopi.Hisopi.services;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import Hisopi.Hisopi.repository.InsumoRepository;

@Service
public class SerialInsumoService {

    private static final String PREFIXO = "HSP-";
    private static final String CARACTERES =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final int TAMANHO_SERIAL = 8;

    private final SecureRandom random = new SecureRandom();
    private final InsumoRepository repI;

    public SerialInsumoService(InsumoRepository repI) {
        this.repI = repI;
    }

    public String gerarSerial() {
        String serial;

        do {
            StringBuilder codigo = new StringBuilder(PREFIXO);

            for (int i = 0; i < TAMANHO_SERIAL; i++) {
                int indice = random.nextInt(CARACTERES.length());
                codigo.append(CARACTERES.charAt(indice));
            }

            serial = codigo.toString();

        } while (repI.existsBySerial(serial));

        return serial;
    }
}