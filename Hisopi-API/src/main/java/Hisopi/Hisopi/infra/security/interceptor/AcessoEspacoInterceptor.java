package Hisopi.Hisopi.infra.security.interceptor;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import Hisopi.Hisopi.Enum.PapelMembro;
import Hisopi.Hisopi.repository.MembroEspacoRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AcessoEspacoInterceptor implements HandlerInterceptor{

	@Autowired
	private MembroEspacoRepository repM;
	
	// ordem com os papéis em ordem decrescente
	private static final List<PapelMembro> ordem = List.of(PapelMembro.DONO,PapelMembro.ADMIN,PapelMembro.GERENTE,PapelMembro.OPERADOR);
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// TODO Auto-generated method stub
		return HandlerInterceptor.super.preHandle(request, response, handler);
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
