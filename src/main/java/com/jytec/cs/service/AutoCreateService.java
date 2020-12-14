package com.jytec.cs.service;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.jytec.cs.dao.SiteRepository;
import com.jytec.cs.dao.TeacherRepository;
import com.jytec.cs.domain.Site;
import com.jytec.cs.domain.Teacher;

/** code auto generation convention/strategy. */
@Service
public class AutoCreateService {
	private static final Log log = LogFactory.getLog(AutoCreateService.class);
	private @Autowired TeacherRepository teacherRepository;
	private @Autowired SiteRepository siteRepository;

	/**
	 * used when data-appeared-completely in importing resources. <p> if model exits will try to update code.
	 */
	public Teacher findTeacherByNameOrCreateWithCode(String name, String code) {
		Optional<Teacher> oteacher = teacherRepository.findByName(name);
		if (oteacher.isPresent()) {
			if (!code.equals(oteacher.get().getCode())) {
				log.info("Update Teacher code for 【" + name + "】");
				oteacher.get().setCode(code); // reset code for auto-create before.
				return teacherRepository.save(oteacher.get());
			} else {
				return oteacher.get();
			}
		} else {
			Teacher _teacher = new Teacher();
			_teacher.setName(name);
			_teacher.setCode(code);
			return teacherRepository.save(_teacher);
		}
	}

	/** used when data-appeared-incompletely in importing resources. */
	public Teacher findTeacherByNameOrCreateWithAutoCode(String name) {
		return teacherRepository.findByName(name).orElseGet(() -> this.createTeacherWithAutoCode(name, true));
	}

	public Teacher createTeacherWithAutoCode(String name, boolean save) {
		Teacher teacher = new Teacher();
		teacher.setName(name);
		if (save) {
			return this.save(teacher);
		}
		return teacher;
	}

	public Site createSiteWithAutoCode(String name, boolean save) {
		Site site = new Site();
		site.setName(name);
		if (save) {
			return this.save(site);
		}
		return site;
	}

	public Site save(Site newSiteWithoutCode) {
		Site e = newSiteWithoutCode;
		boolean expect = e.getId() == 0 && (e.getCode() == null || e.getCode().isEmpty());
		Assert.isTrue(expect, "此方法仅适用于新建且无 Code 的上课场所！！！");
		Assert.isTrue(e.getName() != null && !e.getName().isEmpty(), "name 不应为空！！！");
		
		Site saved = siteRepository.save(e);
		saved.setCode("T" + saved.getId()); // code strategy.
		log.info("自动创建场地【" + saved.getName() + "】－code【" + saved.getCode() + "】");
		return siteRepository.save(saved);
	}
	
	public Teacher save(Teacher newTeacherWithoutCode) {
		Teacher e = newTeacherWithoutCode;
		boolean expect = e.getId() == 0 && (e.getCode() == null || e.getCode().isEmpty());
		Assert.isTrue(expect, "此方法仅适用于新建且无 Code 的教师！！！");
		Assert.isTrue(e.getName() != null && !e.getName().isEmpty(), "name 不应为空！！！");
		
		Teacher saved = teacherRepository.save(e);
		saved.setCode("T" + saved.getId()); // code strategy.
		log.info("自动创建教师【" + saved.getName() + "】－code【" + saved.getCode() + "】");
		return teacherRepository.save(saved);
	}

}
