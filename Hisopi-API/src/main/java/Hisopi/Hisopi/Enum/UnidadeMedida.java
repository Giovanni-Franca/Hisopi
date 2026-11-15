package Hisopi.Hisopi.Enum;

public enum UnidadeMedida {
	KG("kg", "Quilograma"),
	G("g", "Grama"),
	L("l", "Litro"),
	ML("ml", "Mililitro"),
	UN("un", "Unidade"),
	CX("cx", "Caixa"),
	PCT("pct", "Pacote"),
	DUZIA("dz", "Dúzia");

	private final String sigla;
	private final String descricao;

	UnidadeMedida(String sigla, String descricao) {
		this.sigla = sigla;
		this.descricao = descricao;
	}

	public String getSigla() { return sigla; }
	public String getDescricao() { return descricao; }
}
