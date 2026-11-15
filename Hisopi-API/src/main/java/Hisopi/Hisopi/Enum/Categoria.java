package Hisopi.Hisopi.Enum;

public enum Categoria {
	CARNES("Carnes"),
	CEREAIS("Cereais"),
	GRAOS("Grãos"),
	OVOS("Ovos"),
	LATICINIOS("Laticínios"),
	MASSAS("Massas"),
	FRUTAS("Frutas"),
	LEGUMES("Legumes"),
	VERDURAS("Verduras"),
	FARINACEOS("Farináceos"),
	OLEOS_E_GORDURAS("Óleos e gorduras"),
	TEMPEROS("Temperos"),
	BEBIDAS("Bebidas"),
	OUTROS("Outros");
	
	private String Categoria;
	
	Categoria(String Categoria){
		this.Categoria = Categoria;
	}
	
	public String getCategoria() {
		return Categoria;
	}
}
