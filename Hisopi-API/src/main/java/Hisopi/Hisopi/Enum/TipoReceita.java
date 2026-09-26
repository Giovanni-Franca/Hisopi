package Hisopi.Hisopi.Enum;

public enum TipoReceita {
	PRODUTO_VENDA("produto_venda"),
    SUGESTAO_CONSUMO("sugestao_consumo");
	
	private String TipoReceita;
	
	TipoReceita(String TipoReceita){
		this.TipoReceita = TipoReceita;
	}
	
	public String getTipoReceita() {
		return TipoReceita;
	}
}
