package com.jytec.cs.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jytec.cs.domain.Site;
import com.jytec.cs.service.SiteService;
import com.jytec.cs.service.api.SiteSearchParams;

@RestController
@RequestMapping("/sites")
public class SiteController extends AbstractModelController<Site, Integer>{
	private @Autowired SiteService siteService;

	@GetMapping
	public List<Site> search(SiteSearchParams params){
		return siteService.search(params);
	}
	
}
