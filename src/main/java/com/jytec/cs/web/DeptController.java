package com.jytec.cs.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jytec.cs.domain.Dept;
import com.jytec.cs.service.DeptService;
import com.jytec.cs.service.api.DeptSearchParams;

@RestController
@RequestMapping("/dept")
public class DeptController extends AbstractModelController<Dept, Integer> {
	private @Autowired DeptService deptService;

	@GetMapping
	public List<Dept> search(DeptSearchParams params) {
		return deptService.search(params);
	}
}
