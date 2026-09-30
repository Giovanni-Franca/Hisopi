package Hisopi.Hisopi.infra.security.interceptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.model.MembroEspaco;
import Hisopi.Hisopi.model.Usuario;
import Hisopi.Hisopi.repository.MembroEspacoRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AcessoEspacoInterceptor implements HandlerInterceptor{

	@Autowired
	private MembroEspacoRepository repM;
	
	// ordem dos papéis em ordem decrescente
	private static final List<PapelMembro> ordem = List.of(PapelMembro.DONO,PapelMembro.ADMIN,PapelMembro.GERENTE,PapelMembro.OPERADOR);
	
	
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
		
		@SuppressWarnings("unchecked")
		Map<String, String> pathVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		String idEspacoStr = pathVars != null ? pathVars.get("idEspaco") : null;
		
		if(idEspacoStr == null) {
			throw new IllegalStateException("Endpoint com anotacao precisa de {idEspaco}");
		}
		long idEspaco = Long.valueOf(idEspacoStr);
		Optional <MembroEspaco> membro = repM.findByEspacoIdAndUsuarioId(idEspaco, usuarioLogado.getId());
		if(membro.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return false;
		}
		
		int nivelUsuario = ordem.indexOf(membro.get().getPapel());
		int nivelExigido = ordem.indexOf(anotacao.papelMinimo());
		
		// meio contra-intuitivo, mas o indice maior do usuario indica um cargo menor
		if(nivelUsuario > nivelExigido) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return false;
		}
		
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
