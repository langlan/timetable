package com.jytec.cs.service;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	 * used when data-appeared-completely in importing resources. <p>
	 * if model exits will try to update code.  
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
		return teacherRepository.findByName(name).orElseGet(() -> this.createTeacherWithAutoCode(name));
	}

	public Teacher createTeacherWithAutoCode(String name) {
		Teacher teacher = new Teacher();
		teacher.setName(name);
		teacher = teacherRepository.save(teacher);
		teacher.setCode("T" + teacher.getId()); // code strategy.
		log.info("自动创建教师【" + name + "】－code【" + teacher.getCode() + "】");
		return teacherRepository.save(teacher);
	}

	public Site createSiteWithAutoCode(String name) {
		Site site = new Site();
		site.setName(name);
		site = siteRepository.save(site);
		site.setCode("T" + site.getId()); // code strategy.
		return siteRepository.save(site);
	}

}
