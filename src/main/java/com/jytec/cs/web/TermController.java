package com.jytec.cs.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jytec.cs.domain.Term;
import com.jytec.cs.service.TermService;
import com.jytec.cs.service.api.TermSearchParams;

@RestController
@RequestMapping("/terms")
public class TermController extends AbstractModelController<Term, String> {
	private @Autowired TermService termService;

	@GetMapping
	public List<Term> search(TermSearchParams params) {
		return termService.search(params);
	}
}
