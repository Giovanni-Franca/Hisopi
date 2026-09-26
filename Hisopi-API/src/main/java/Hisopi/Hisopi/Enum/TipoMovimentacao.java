package Hisopi.Hisopi.Enum;

public enum TipoMovimentacao {
	 ENTRADA("entrada"),
     SAIDA_USO("saida_uso"),
     PERDA_VALIDADE("perda_validade"),
     PERDA_OUTRO("perda_outro"),
     DOACAO("doacao"),
     AJUSTE("ajuste");
	
	private String TipoMovimentacao;
		
	TipoMovimentacao(String TipoMovimentacao){
		this.TipoMovimentacao = TipoMovimentacao;
	}
	
	public String getTipoMovimentacao() {
		return TipoMovimentacao;
	}
}
