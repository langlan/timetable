package com.jytec.cs.web;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Major;
import com.jytec.cs.service.ClassCourseService;
import com.jytec.cs.service.api.ClassSearchParams;

@RestController
@RequestMapping("/classes")
public class ClassController extends AbstractModelController<Major, Integer>{
	private @Autowired ClassCourseService classCourseService;

	@GetMapping
	public List<Class> search(ClassSearchParams params){
		return classCourseService.search(params);
	}
	
	@GetMapping("/idc/{idc}")
	public Class getByIdc(@PathVariable Integer idc) {
		Optional<Class> r = classCourseService.findClassByIdc(idc);
		return r.get();
	}
}
