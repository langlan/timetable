package com.jytec.cs.web.advices;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class Exceptions {
	@ExceptionHandler
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	public @ResponseBody Map<String, Object> handleException(NoSuchElementException e, HttpServletResponse response) {
		return prepareResponse(e);
	}

	@ExceptionHandler
	@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
	public @ResponseBody Map<String, Object> handleException(IdcException e, HttpServletResponse response) {
		return prepareResponse(e);
	}

	@ExceptionHandler
	public @ResponseBody Map<String, Object> handleGlobal(Exception e, HttpServletResponse response) {
		// TODO: check if custom error-code.
		response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		return prepareResponse(e);
	}

	private Map<String, Object> prepareResponse(Exception e) {
		Map<String, Object> ret = new HashMap<>();
		ret.put("msg", e.getMessage());
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw, true));
		ret.put("traces", sw.toString());
		return ret;
	}

	public static class IdcException extends RuntimeException {
		private static final long serialVersionUID = -2693265474948279452L;

		public IdcException(String msg) {
			super(msg);
		}
	}
}
