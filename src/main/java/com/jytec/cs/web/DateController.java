package com.jytec.cs.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jytec.cs.domain.Date;
import com.jytec.cs.service.TermService;
import com.jytec.cs.service.api.DateSearchParams;

@RestController
@RequestMapping("/dates")
public class DateController extends AbstractModelController<Date, String> {
	private @Autowired TermService termService;

	@GetMapping
	public List<Date> search(DateSearchParams params) {
		return termService.search(params);
	}
}
	