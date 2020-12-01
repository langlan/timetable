package com.jytec.cs.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jytec.cs.domain.Teacher;
import com.jytec.cs.service.TeacherService;
import com.jytec.cs.service.api.TeacherSearchParams;

@RestController
@RequestMapping("/teachers")
public class TeacherController extends AbstractModelController<Teacher, Long> {
	private @Autowired TeacherService teacherService;

	@GetMapping
	public List<Teacher> search(TeacherSearchParams params) {
		return teacherService.search(params);
	}
}
