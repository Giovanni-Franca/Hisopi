package Hisopi.Hisopi.Enum;

public enum TipoEspaco {
	PESSOAL("pessoal"),
    ORGANIZACAO("organicacao");
	
	private String TipoEspaco;
		
	TipoEspaco(String TipoEspaco){
		this.TipoEspaco = TipoEspaco;
	}
	
	public String getTipoEspaco() {
		return TipoEspaco;
	}
}
