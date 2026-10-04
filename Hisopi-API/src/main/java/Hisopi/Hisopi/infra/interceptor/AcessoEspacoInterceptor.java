package Hisopi.Hisopi.infra.interceptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.infra.exception.AcessoNegadoException;
import Hisopi.Hisopi.infra.exception.ErrorResponseWriter;
import Hisopi.Hisopi.model.MembroEspaco;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.MembroEspacoRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AcessoEspacoInterceptor implements HandlerInterceptor{

	private final ErrorResponseWriter errorResponseWriter;
	@Autowired
	private MembroEspacoRepository repM;
	
	public AcessoEspacoInterceptor(ErrorResponseWriter errorResponseWriter,
            MembroEspacoRepository repM) {
		this.errorResponseWriter = errorResponseWriter;
		this.repM = repM;
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// se não for endpoint de controller só deixa passar
		if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
		
		AcessoEspaco anotacao = hm.getMethodAnnotation(AcessoEspaco.class);
		if(anotacao == null) {
			// tenta preencher anotacao
			anotacao = hm.getBeanType().getAnnotation(AcessoEspaco.class);
			if(anotacao == null) {
				// se mesmo assim não preencher, o espaco não preceisa de checagem
				return true; 
			}
		}
		
		var auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof Usuario usuarioLogado)) {
			errorResponseWriter.write(response,HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Usuário não autenticado");
            return false;
        }
		
		@SuppressWarnings("unchecked")
		Map<String, String> pathVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		String idEspacoStr = pathVars != null ? pathVars.get("idEspaco") : null;
		
		if(idEspacoStr == null) {
			throw new AcessoNegadoException("Acesso negado ao espaço");
		}
		
		long idEspaco;
		try {
			idEspaco = Long.parseLong(idEspacoStr);
		} catch (NumberFormatException e) {
			errorResponseWriter.write(
					response, 
					HttpStatus.BAD_REQUEST, 
					"BAD_REQUEST", 
					"Identificador inválido");
			return false;
		}
		
		Optional <MembroEspaco> membro = repM.findByEspacoIdAndUsuarioId(idEspaco, usuarioLogado.getId());
		if(membro.isEmpty() || !membro.get().getPapel().temPermissao(anotacao.papelMinimo())) {
			throw new AcessoNegadoException("Acesso negado");
		}
		
		request.setAttribute("membroLogado", membro.get());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) throws Exception {
		// TODO Auto-generated method stub
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}

	
}
