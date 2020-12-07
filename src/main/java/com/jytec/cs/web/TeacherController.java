package com.jytec.cs.web;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.Teacher;
import com.jytec.cs.service.TeacherService;
import com.jytec.cs.service.api.TeacherSearchParams;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController extends AbstractModelController<Teacher, Long> {
	private @Autowired TeacherService teacherService;

	@GetMapping
	public List<Teacher> search(TeacherSearchParams params) {
		return teacherService.search(params);
	}
	
	@GetMapping("/idc/{idc}")
	public Class getByIdc(@PathVariable Integer idc) {
		Optional<Class> r = teacherService.findByIdc(idc);
		return r.get();
	}
}
