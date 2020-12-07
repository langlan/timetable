package com.jytec.cs.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jytec.cs.domain.Course;
import com.jytec.cs.domain.Major;
import com.jytec.cs.service.ClassCourseService;
import com.jytec.cs.service.api.CourseSearchParams;

@RestController
@RequestMapping("/api/courses")
public class CourseController extends AbstractModelController<Major, String>{
	private @Autowired ClassCourseService classCourseService;

	@GetMapping
	public List<Course> search(CourseSearchParams params){
		return classCourseService.search(params);
	}
	
}
