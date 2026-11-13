package Hisopi.Hisopi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Hisopi.Hisopi.DTO.NovoEspacoComChaveDTO;
import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.model.Espaco;
import Hisopi.Hisopi.model.MembroEspaco;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.EspacoRepository;
import Hisopi.Hisopi.repository.MembroEspacoRepository;
import jakarta.transaction.Transactional;


@Service
public class ParceiroService {
	@Autowired
	private EspacoRepository repE;
	@Autowired
	private MembroEspacoRepository repM;
	@Autowired
	private ApiKeyService apiKeyService;

    @Transactional
    public ApiKeyService.ChaveCriada criarEspacoComChave(NovoEspacoComChaveDTO dto, Usuario dono) {
        Espaco espaco = new Espaco();
        espaco.setNome(dto.nomeEspaco());
        espaco.setTipo(dto.tipo());
        repE.save(espaco);

        MembroEspaco membro = new MembroEspaco();
        membro.setEspaco(espaco);
        membro.setUsuario(dono);
        membro.setPapel(PapelMembro.DONO);
        repM.save(membro);

        return apiKeyService.gerar(espaco, dto.nomeChave());
    }
}