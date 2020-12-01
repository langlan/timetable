package com.jytec.cs.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jytec.cs.domain.Major;
import com.jytec.cs.service.MajorService;
import com.jytec.cs.service.api.MajorSearchParams;

@RestController
@RequestMapping("/majors")
public class MajorController extends AbstractModelController<Major, Integer>{
	private @Autowired MajorService majorService;

	@GetMapping
	public List<Major> search(MajorSearchParams params){
		return majorService.search(params);
	}
}
