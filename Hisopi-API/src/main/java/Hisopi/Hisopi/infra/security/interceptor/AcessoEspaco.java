package Hisopi.Hisopi.infra.security.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import Hisopi.Hisopi.Enum.PapelMembro;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AcessoEspaco {
	PapelMembro papelMinimo() default PapelMembro.OPERADOR;
}
