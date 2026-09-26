package Hisopi.Hisopi.Enum;

public enum PapelMembro {
	DONO("dono"),     
    ADMIN("admin"),    
    GERENTE("gerente"),  
    OPERADOR("operador");
    
	private String PapelMembro;
	
	PapelMembro(String PapelMembro){
		this.PapelMembro = PapelMembro;
	}
	
	public String getPapelMembro() {
		return PapelMembro;
	}
}
