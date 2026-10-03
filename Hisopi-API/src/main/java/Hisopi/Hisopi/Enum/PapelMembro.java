package Hisopi.Hisopi.Enum;

public enum PapelMembro {
	DONO(4),     
    ADMIN(3),    
    GERENTE(2),  
    OPERADOR(1);
    
	private final int PapelMembro;
	
	PapelMembro(int PapelMembro){
		this.PapelMembro = PapelMembro;
	}
	
	public boolean temPermissao(PapelMembro exigido) {
		return this.PapelMembro >= exigido.PapelMembro;
	}
	
	public boolean superior(PapelMembro outro) {
		return this.PapelMembro > outro.PapelMembro;
	}
}
