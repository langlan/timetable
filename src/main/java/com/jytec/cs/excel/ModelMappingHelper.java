package com.jytec.cs.excel;

import static com.jytec.cs.excel.parse.Texts.atLocaton;
import static java.util.stream.Collectors.toSet;
import static langlan.sql.weaver.u.Variables.isNotEmpty;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.persistence.NonUniqueResultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;

import com.jytec.cs.dao.ClassCourseRepository;
import com.jytec.cs.dao.ClassRepository;
import com.jytec.cs.dao.CourseRepository;
import com.jytec.cs.dao.DeptRepository;
import com.jytec.cs.dao.MajorRepository;
import com.jytec.cs.dao.ScheduleRepository;
import com.jytec.cs.dao.SiteRepository;
import com.jytec.cs.dao.TeacherRepository;
import com.jytec.cs.domain.Class;
import com.jytec.cs.domain.ClassCourse;
import com.jytec.cs.domain.Course;
import com.jytec.cs.domain.Dept;
import com.jytec.cs.domain.Major;
import com.jytec.cs.domain.Schedule;
import com.jytec.cs.domain.Site;
import com.jytec.cs.domain.Teacher;
import com.jytec.cs.domain.Term;
import com.jytec.cs.service.AutoCreateService;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ModelMappingHelper {
	private static final Log log = LogFactory.getLog(TrainingScheduleImporter.class);
	private @Autowired DeptRepository deptRepository;
	private @Autowired MajorRepository majorRepository;
	private @Autowired ClassCourseRepository classCourseRepository;
	private @Autowired ClassRepository classRepository;
	private @Autowired CourseRepository courseRepository;
	private @Autowired TeacherRepository teacherRepository;
	private @Autowired SiteRepository siteRepository;
	private @Autowired ScheduleRepository scheduleRespository;
	private @Autowired AutoCreateService autoCreateService;

	private Term term;
	private Map<String, ClassCourse> classCoursesIndexedByNames;
	private Map<String, ClassCourse> classCoursesIndexedByClassNameAndCourseCode;
	private Map<String, Class> classesIndexedByName;
	private Map<String, Course> coursesIndexedByCode;
	private Map<String, Teacher> teachersIndexedByName;
	private Map<String, Dept> deptsIndexdByName;
	private Map<String, Major> majorsIndexdByName;
	private Map<String, Site> newSitesIndexdByName = new HashMap<>();
	private List<Schedule> newSchedules = new LinkedList<>();
	List<Dept> newDepts = new LinkedList<>();
	List<Major> newMajors = new LinkedList<>();
	List<Class> newClasses = new LinkedList<>();
	List<Course> newCourses = new LinkedList<>();
	List<Teacher> newTeachers = new LinkedList<>();
	List<Teacher> existsTeachers4UpdateCode = new LinkedList<>();
	private Set<String> newTeachersWithoutCode = new HashSet<String>();
	List<ClassCourse> newClassCourses = new LinkedList<ClassCourse>();

	public ModelMappingHelper term(Term term) {
		this.term = term;
		return this;
	}

	private void initClassCoursesIndexedByNames() {
		List<Object[]> all = classCourseRepository.findAllIndexedByNames(term.getId());
		classCoursesIndexedByNames = all.stream()
				.collect(Collectors.toMap(it -> it[0].toString(), it -> (ClassCourse) it[1]));
		if (all.size() != classCoursesIndexedByNames.size())
			throw new IllegalStateException("发现现存【班级选课表】中存在重复数据！");
	}

	public Site findSite(String siteName, Cell cell) {
		// NODE: name is not actually a unique key. but for theory course, we suppose so.
		// TODO: Examine: how training-schedule specify a site when name not unique.
		try {
			return siteRepository.findUniqueByName(siteName).orElseGet(() -> {
				if (newSitesIndexdByName == null) {
					newSitesIndexdByName = new HashMap<>();
				}
				Site ret = newSitesIndexdByName.get(siteName);
				if (ret == null) {
					String msg = "找不到上课地点【" + siteName + "】" + atLocaton(cell);
					log.warn(msg);
					// throw new IllegalStateException(warn);
					ret = autoCreateService.createSiteWithAutoCode(siteName, false);
					newSitesIndexdByName.put(siteName, ret);
				}
				return ret;
			});
		} catch (IncorrectResultSizeDataAccessException | NonUniqueResultException e) {
			throw new IllegalStateException("非唯一：存在多个同名上课地点【" + siteName + "】" + atLocaton(cell));
		}
	}

	public Teacher findTeacher(String teacherName, ClassCourse classCourse, Cell cell) {
		// validate/locate-by teacher-name.
		if (classCourse.getTeacher().getName().equals(teacherName)) { // validate
			return classCourse.getTeacher();
		} else {
			if (!classCourse.getTeacherNames().contains(teacherName)) {
				String msg = "课程教师名与选课数据不匹配：" + "在导【" + teacherName + "】 VS 选课【" + classCourse.getTeacherNames() + "】"
						+ atLocaton(cell);
				log.warn(msg);
			}

			return findteacherOrStageByName(teacherName, cell);
		}
	}

	/** NOTE: course-name is not unique */
	public ClassCourse findClassCourse(String classNameWithDegree, String courseName, Cell cell) {
		initClassCoursesIndexedByNames();
		String classCourseKey = classNameWithDegree + "-" + courseName;
		ClassCourse classCourse = classCoursesIndexedByNames.get(classCourseKey);

		if (classCourse == null) {
			throw new IllegalStateException("无法找到【班级选课】记录：" + classCourseKey + atLocaton(cell));
		}
		return classCourse;
	}

	private void initDeptsIndexedByName() {
		if (deptsIndexdByName == null) {
			deptsIndexdByName = stream(deptRepository.findAll()).collect(Collectors.toMap(Dept::getName, it -> it));
		}
	}

	private void initMajorsIndexedByName() {
		if (majorsIndexdByName == null) {
			majorsIndexdByName = stream(majorRepository.findAll()).collect(Collectors.toMap(Major::getName, it -> it));
		}
	}

	private void initClassesIndexedByName() {
		if (classesIndexedByName == null) {
			classesIndexedByName = stream(classRepository.findAll())
					.collect(Collectors.toMap(Class::getName, it -> it));
		}
	}

	private void initCoursesIndexedByCode() {
		if (coursesIndexedByCode == null) {
			coursesIndexedByCode = stream(courseRepository.findAll())
					.collect(Collectors.toMap(Course::getCode, it -> it));
		}
	}

	private void initTeachersIndexedByName() {
		if (teachersIndexedByName == null) {
			teachersIndexedByName = stream(teacherRepository.findAll())
					.collect(Collectors.toMap(Teacher::getName, it -> it));
		}
	}

	private void initClassCoursesIndexedByClassNameAndCourseCode() {
		if (classCoursesIndexedByClassNameAndCourseCode == null) {
			List<Object[]> all = classCourseRepository.findAllIndexedByClassNameCourseCode(term.getId());
			classCoursesIndexedByClassNameAndCourseCode = all.stream()
					.collect(Collectors.toMap(it -> it[0].toString(), it -> (ClassCourse) it[1]));
		}
	}

	public Class findClassOrStageByName(Class cls, Major major) {
		initClassesIndexedByName();
		Class ret = classesIndexedByName.get(cls.getName());
		if (ret == null) {
			ret = cls;
			cls.setMajor(major);
			cls.setDeptId(major.getDept().getId());
			classesIndexedByName.put(cls.getName(), cls);
			newClasses.add(cls);
		}
		return ret;
	}

	public Course findCourseOrStageByCourseCode(Course forSave) {
		initCoursesIndexedByCode();
		Course ret = coursesIndexedByCode.get(forSave.getCode());
		if (ret == null) {
			ret = forSave;
			coursesIndexedByCode.put(forSave.getCode(), forSave);
			newCourses.add(forSave);
		}
		return ret;
	}

	public Teacher findteacherOrStageByName(Teacher forSave) {
		initTeachersIndexedByName();
		Teacher ret = teachersIndexedByName.get(forSave.getName());
		if (ret == null) {
			ret = forSave;
			teachersIndexedByName.put(forSave.getName(), forSave);
			newTeachers.add(forSave);
		} else if (isNotEmpty(forSave) && !isEmpty(forSave.getCode())
				&& (isEmpty(forSave.getCode()) || forSave.getCode().startsWith("T"))) {
			ret.setCode(forSave.getCode());
			if (ret.getId() != 0) {
				existsTeachers4UpdateCode.add(ret);
			}
		}
		return ret;
	}

	public Teacher findteacherOrStageByName(String teacherName, Cell cell) {
		initTeachersIndexedByName();
		Teacher ret = teachersIndexedByName.get(teacherName);
		if (ret == null) {
			if (cell != null) { // used by schedule
				String msg = "找不到教师【" + teacherName + "】" + atLocaton(cell);
				// throw new IllegalStateException(msg);
				log.warn(msg);
			} // or can used by class-course
			Teacher teacher = autoCreateService.createTeacherWithAutoCode(teacherName, false);
			newTeachers.add(teacher);
		}
		return ret;
	}

	public void stageTeachersWithoutCodeIfAbsent(Collection<String> otherTeacherNames) {
		this.newTeachersWithoutCode.addAll(otherTeacherNames);
	}

	public ClassCourse findClassCourseOrStageByClassNameAndCourseCode(ClassCourse classCourse, Class cls, Course course,
			Teacher teacher) {
		String classCourseKey = cls.getName() + "-" + course.getCode();
		initClassCoursesIndexedByClassNameAndCourseCode();
		ClassCourse ret = classCoursesIndexedByClassNameAndCourseCode.get(classCourseKey);
		if (ret == null) {
			ret = classCourse;
			classCourse.setCourse(course);
			classCourse.setTheClass(cls);
			classCourse.setTeacher(teacher);
			classCoursesIndexedByClassNameAndCourseCode.put(classCourseKey, classCourse);
			newClassCourses.add(ret);
		}
		return ret;
	}

	public Dept findDeptOrStageByName(Dept dept) {
		initDeptsIndexedByName();
		Dept ret = deptsIndexdByName.get(dept.getName());
		if (ret == null) {
			ret = dept;
			ret.setName(dept.getName());
			deptsIndexdByName.put(dept.getName(), ret);
			newDepts.add(ret);
		} else if (isEmpty(ret.getShortName()) && isNotEmpty(dept.getShortName())) {
			ret.setShortName(dept.getShortName());
			deptRepository.save(ret); // save directly without notify.
			log.info("Update short name for Dept.");
		}
		return ret;
	}

	public Major findMajorOrStageByName(Major major, Dept dept) {
		initMajorsIndexedByName();
		Major ret = majorsIndexdByName.get(major.getName());
		if (ret == null) {
			ret = major;
			ret.setDept(dept);
			majorsIndexdByName.put(major.getName(), ret);
			newMajors.add(ret);
		}
		return ret;
	}

	public void saveStaged() {
		deptRepository.saveAll(newDepts);
		majorRepository.saveAll(newMajors);
		newClasses.forEach(it -> it.setDeptId(it.getMajor().getDept().getId()));
		classRepository.saveAll(newClasses);
		courseRepository.saveAll(newCourses);

		// teacherRepository.saveAll(newTeachers.stream().filter(it -> !isEmpty(it.getCode())).collect(toList()));
		teacherRepository.saveAll(newTeachers);
		teacherRepository.saveAll(existsTeachers4UpdateCode);
		// newTeachers.stream().filter(it -> isEmpty(it.getCode() && it.getId()==null)).map(Teacher::getName)
		getNewTeachersWithoutCode().forEach(autoCreateService::findTeacherByNameOrCreateWithAutoCode);
		classCourseRepository.saveAll(newClassCourses);
		// schedule only
		siteRepository.saveAll(newSitesIndexdByName.values());
		scheduleRespository.saveAll(newSchedules);
	}

	public Collection<String> getNewTeachersWithoutCode() {
		newTeachersWithoutCode = newTeachersWithoutCode.stream().filter(it -> teachersIndexedByName.get(it) == null)
				.collect(toSet());
		return newTeachersWithoutCode;
	}

	static <T> Stream<T> stream(Iterable<T> its) {
		return StreamSupport.stream(its.spliterator(), false);
	}

	boolean isEmpty(String v) {
		return v == null || v.isEmpty();
	}

	public static class StagedCounts {
		int depts, majors, classes, courses, teachers, classCourses;
		int sites, schedules;

		StagedCounts delta(StagedCounts less) {
			StagedCounts d = new StagedCounts();
			d.depts = this.depts - less.depts;
			d.majors = this.majors - less.majors;
			d.classes = this.classes - less.classes;
			d.courses = this.courses - less.courses;
			d.teachers = this.teachers - less.teachers;
			d.classCourses = this.classCourses - less.classCourses;
			d.sites = this.sites = less.sites;
			d.schedules = this.schedules - less.schedules;
			return d;
		}
	}

	public StagedCounts getStatedCounts() {
		StagedCounts ret = new StagedCounts();
		ret.depts = newDepts.size();
		ret.majors = newMajors.size();
		ret.classes = newClasses.size();
		ret.courses = newCourses.size();
		ret.teachers = newTeachers.size() + getNewTeachersWithoutCode().size();
		ret.classCourses = newClassCourses.size();
		ret.sites = newSitesIndexdByName.size();
		ret.schedules = newSchedules.size();
		return ret;
	}

	public void stageAll(List<Schedule> schedules) {
		this.newSchedules.addAll(schedules);
	}

}