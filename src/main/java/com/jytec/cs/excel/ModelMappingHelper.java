package com.jytec.cs.excel;

import static com.jytec.cs.domain.Schedule.COURSE_TYPE_NORMAL;
import static com.jytec.cs.domain.Schedule.COURSE_TYPE_TRAINING;
import static com.jytec.cs.excel.parse.Texts.atLocaton;
import static langlan.sql.weaver.u.Variables.isNotEmpty;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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
import com.jytec.cs.excel.TextParser.ScheduledCourse;
import com.jytec.cs.excel.exceptions.ModelMappingException;
import com.jytec.cs.service.AutoCreateService;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ModelMappingHelper {
	private static final Log log = LogFactory.getLog(ModelMappingHelper.class);
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
	private Map<String, Site> newSitesWithoutCodeIndexdByName = new HashMap<>();
	List<Schedule> newSchedules = new LinkedList<>();
	List<Dept> newDepts = new LinkedList<>();
	List<Major> newMajors = new LinkedList<>();
	List<Class> newClasses = new LinkedList<>();
	List<Course> newCourses = new LinkedList<>();
	List<Teacher> newTeachers = new LinkedList<>();
	List<Teacher> existsTeachers4UpdateCode = new LinkedList<>();
	private Map<String, Teacher> newTeachersWithoutCodeIndexedByName = new HashMap<>();
	List<ClassCourse> newClassCourses = new LinkedList<ClassCourse>();
	Map<String, Set<Cell>> classCourseNotFountExceptions = new LinkedHashMap<>();
	Map<String, Set<Cell>> teacherNotMatchExceptions = new LinkedHashMap<>();
	Map<String, Set<Cell>> teacherNotFoundExceptions = new LinkedHashMap<>();
	Map<String, Set<Cell>> siteNotFoundExceptions = new LinkedHashMap<>();
	/** clear it if suppress exceptions */
	boolean hasAnyClassCourseNotFountExceptions;
	boolean hasAnyTeacherNotMatchExceptions;
	boolean hasAnyTeacherNotFoundExceptions;
	boolean hasAnySiteNotFoundExceptions;

	public ModelMappingHelper term(Term term) {
		this.term = term;
		return this;
	}

	public String saveStaged() {
		if (hasAnyClassCourseNotFountExceptions || hasAnyTeacherNotFoundExceptions || hasAnyTeacherNotMatchExceptions
				|| hasAnySiteNotFoundExceptions) {
			String msg = "存在异常，不予保存！";
			log.info(msg);
			return msg;
		}
		deptRepository.saveAll(newDepts);
		majorRepository.saveAll(newMajors);
		newClasses.forEach(it -> it.setDeptId(it.getMajor().getDept().getId()));
		classRepository.saveAll(newClasses);
		courseRepository.saveAll(newCourses);

		// teacherRepository.saveAll(newTeachers.stream().filter(it -> !isEmpty(it.getCode())).collect(toList()));
		// newTeachers.forEach(t -> VOID(isEmpty(t.getCode()) ? autoCreateService.save(t) : teacherRepository.save(t)));
		teacherRepository.saveAll(newTeachers);
		teacherRepository.saveAll(existsTeachers4UpdateCode);
		newTeachersWithoutCodeIndexedByName.values().forEach(autoCreateService::save); // class-course or schedule.
		classCourseRepository.saveAll(newClassCourses);
		// schedule only
		newSitesWithoutCodeIndexdByName.values().forEach(it -> autoCreateService.save(it));
		scheduleRespository.saveAll(newSchedules);
		return null;
	}

	private void initClassCoursesIndexedByNames() {
		List<Object[]> all = classCourseRepository.findAllIndexedByNames(term.getId());
		classCoursesIndexedByNames = all.stream()
				.collect(Collectors.toMap(it -> it[0].toString(), it -> (ClassCourse) it[1]));
		if (all.size() != classCoursesIndexedByNames.size())
			throw new IllegalStateException("发现现存【班级选课表】中存在重复数据！");
	}

	Map<String, List<Site>> sitesIndexdByName = null;
	List<Site> sitesWithNotUniqueName;

	public Site findSite(String siteName, Cell cell) {
		if (isEmpty(siteName)) {
			log.debug("上课地点为空：" + atLocaton(cell));
			return null;
		}
		if (sitesIndexdByName == null) {
			sitesIndexdByName = new HashMap<>();
			List<Site> au = siteRepository.findAllWithUniqueName();
			au.forEach(it -> sitesIndexdByName.put(it.getName(), Collections.singletonList(it)));
			List<Site> anu = siteRepository.findAllWithNotUniqueName();
			anu.forEach(it -> {
				List<Site> lst = new LinkedList<>();
				lst.add(it);
				sitesIndexdByName.merge(it.getName(), lst, (left, right) -> {
					left.addAll(right);
					return left;
				});
			});
			sitesWithNotUniqueName = anu;
		}
		// NODE: name is not actually a unique key. but for theory course, we suppose so.
		// TODO: Examine: how training-schedule specify a site when name not unique.
		List<Site> sites = sitesIndexdByName.get(siteName);
		if (sites != null) {
			if (sites.size() == 1) {
				return sites.get(0);
			} else {
				throw new ModelMappingException("非唯一：存在多个同名上课地点【" + siteName + "】，无法抉择" + atLocaton(cell));
			}
		} else {
			// try sites with not-unique name
			for (Site s : sitesWithNotUniqueName) {
				if (siteName.equals(s.getRoomType()) || siteName.equals(s.getName4Training())) {
					return s;
				}
			}
			/////////////////////////////////////////////////////
			Set<Cell> cells = siteNotFoundExceptions.get(siteName);
			if (cells == null) {
				cells = new LinkedHashSet<Cell>();
				siteNotFoundExceptions.put(siteName, cells);
			}
			cells.add(cell);
			hasAnySiteNotFoundExceptions = true;
			/////////////////////////////////////////////////////
			if (newSitesWithoutCodeIndexdByName == null) {
				newSitesWithoutCodeIndexdByName = new HashMap<>();
			}
			Site ret = newSitesWithoutCodeIndexdByName.get(siteName);
			if (ret == null) {
				String msg = "找不到上课地点【" + siteName + "】" + atLocaton(cell);
				log.warn(msg);
				// throw new IllegalStateException(warn);
				ret = autoCreateService.createSiteWithAutoCode(siteName, false);
				newSitesWithoutCodeIndexdByName.put(siteName, ret);
			}
			return ret;
		}
	}

	public Teacher findTeacher(String teacherName, ClassCourse classCourse, Cell cell) {
		// validate/locate-by teacher-name.
		if (classCourse.getTeacher().getName().equals(teacherName)) { // validate
			return classCourse.getTeacher();
		} else {
			if (!classCourse.getTeacherNames().contains(teacherName)) {
				String key = "在导【" + teacherName + "】 VS 选课【" + classCourse.getTeacherNames() + "】";
				String msg = "课程教师名与选课数据不匹配：" + key + atLocaton(cell);
				/////////////////////////////////////////////////////
				Set<Cell> cells = teacherNotMatchExceptions.get(key);
				if (cells == null) {
					cells = new LinkedHashSet<Cell>();
					teacherNotMatchExceptions.put(key, cells);
				}
				cells.add(cell);
				hasAnyTeacherNotMatchExceptions = true;
				/////////////////////////////////////////////////////
				log.warn(msg);
			}
			Teacher ret = findteacherOrStageByName(teacherName, cell);
			Assert.notNull(ret, "教师为空：" + cell.getAddress());
			if (ret.getId() == 0) {
				/////////////////////////////////////////////////////
				Set<Cell> cells = teacherNotFoundExceptions.get(teacherName);
				if (cells == null) {
					cells = new LinkedHashSet<Cell>();
					teacherNotFoundExceptions.put(teacherName, cells);
				}
				cells.add(cell);
				hasAnyTeacherNotFoundExceptions = true;
				/////////////////////////////////////////////////////
			}
			return ret;
		}
	}

	/** NOTE: course-name is not unique */
	public ClassCourse findClassCourse(String classNameWithDegree, String courseName, Cell cell) {
		initClassCoursesIndexedByNames();
		String classCourseKey = classNameWithDegree + "-" + courseName;
		ClassCourse classCourse = classCoursesIndexedByNames.get(classCourseKey);

		if (classCourse == null) {
			Set<Cell> cells = classCourseNotFountExceptions.get(classCourseKey);
			if (cells == null) {
				cells = new LinkedHashSet<Cell>();
				classCourseNotFountExceptions.put(classCourseKey, cells);
			}
			hasAnyClassCourseNotFountExceptions = true;
			cells.add(cell);
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

	private Map<String, Teacher> initTeachersIndexedByName() {
		if (teachersIndexedByName == null) {
			teachersIndexedByName = stream(teacherRepository.findAll())
					.collect(Collectors.toMap(Teacher::getName, it -> it));
		}
		return teachersIndexedByName;
	}

	private Map<String, ClassCourse> initClassCoursesIndexedByClassNameAndCourseCode() {
		if (classCoursesIndexedByClassNameAndCourseCode == null) {
			List<Object[]> all = classCourseRepository.findAllIndexedByClassNameCourseCode(term.getId());
			classCoursesIndexedByClassNameAndCourseCode = all.stream()
					.collect(Collectors.toMap(it -> it[0].toString(), it -> (ClassCourse) it[1]));
		}
		return classCoursesIndexedByClassNameAndCourseCode;
	}

	public Class findClassByName(String classNameWithDegree, Cell classCell) throws IllegalStateException {
		initClassesIndexedByName();
		Class ret = classesIndexedByName.get(classNameWithDegree);
		if (ret == null) {
			throw new IllegalStateException("找不到班级【" + classNameWithDegree + "】" + atLocaton(classCell));
		}
		return ret;
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

	/** only used by class-course importing */
	public Teacher findteacherOrStageByName(Teacher forSave) {
		Assert.isTrue(!isEmpty(forSave.getCode()), "教师职工号不应为空！");
		initTeachersIndexedByName();
		Teacher ret = teachersIndexedByName.get(forSave.getName());
		if (ret == null) {
			ret = forSave;
			teachersIndexedByName.put(forSave.getName(), forSave);
			newTeachers.add(forSave);
			// ensure kick out the same name in without-code map.
			// even the other-side checked, or even the caller of other-side checked.
			Teacher shit = newTeachersWithoutCodeIndexedByName.remove(forSave.getName());
			Assert.isNull(shit, "Shit shouldn't happen, check what you done.");
		} else if (isNotEmpty(forSave) && !isEmpty(forSave.getCode())
				&& (isEmpty(forSave.getCode()) || forSave.getCode().startsWith("T"))) {
			ret.setCode(forSave.getCode());
			if (ret.getId() != 0) {
				existsTeachers4UpdateCode.add(ret);
			}
		}
		return ret;
	}

	/** only used by class-course importing */
	public void stageTeacherWithoutCodeIfAbsent(String teacherName) {
		this.findteacherOrStageByName(teacherName, null);
	}

	/** used by schedule or class-course importing */
	public Teacher findteacherOrStageByName(String teacherName, Cell cell) {
		if (isEmpty(teacherName)) {
			return null;
		}
		Teacher ret = initTeachersIndexedByName().get(teacherName);
		if (ret == null) {
			if (cell != null) { // used by schedule
				String msg = "找不到教师【" + teacherName + "】" + atLocaton(cell);
				// throw new IllegalStateException(msg);
				log.warn(msg);
			} // or can used by class-course
			ret = newTeachersWithoutCodeIndexedByName.get(teacherName);
			if (ret == null) {
				ret = autoCreateService.createTeacherWithAutoCode(teacherName, false);
				newTeachersWithoutCodeIndexedByName.put(teacherName, ret);
			}
		}
		return ret;
	}

	public ClassCourse findClassCourseOrStageByClassNameAndCourseCode(ClassCourse classCourse, Class cls, Course course,
			Teacher teacher) {
		String classCourseKey = cls.getName() + "-" + course.getCode();
		ClassCourse ret = initClassCoursesIndexedByClassNameAndCourseCode().get(classCourseKey);
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

	static <T> Stream<T> stream(Iterable<T> its) {
		return StreamSupport.stream(its.spliterator(), false);
	}

	static void VOID(Object o) {
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
		ret.teachers = newTeachers.size() + newTeachersWithoutCodeIndexedByName.size();
		ret.classCourses = newClassCourses.size();
		ret.sites = newSitesWithoutCodeIndexdByName.size();
		ret.schedules = newSchedules.size();
		return ret;
	}

	public void stageAll(List<Schedule> schedules) {
		this.newSchedules.addAll(schedules);
	}

	static class ClassCourseWeek {
		final ClassCourse classCourse;
		final byte weekno;

		public ClassCourseWeek(ClassCourse classCourse, byte weekno) {
			Assert.notNull(classCourse.getTheClass().getId(), "classId cannot be null");
			Assert.notNull(classCourse.getCourse().getCode(), "courseCode cannot be null");
			Assert.isTrue(weekno != 0, "week no cannot be null");
			this.classCourse = classCourse;
			this.weekno = weekno;
		}

		@Override
		public int hashCode() {
			return Objects.hash(classCourse.getTheClass().getId(), classCourse.getCourse().getCode(), weekno);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof ClassCourseWeek)) {
				return false;
			}
			ClassCourseWeek c = (ClassCourseWeek) obj;
			return classCourse.getTheClass().getId() == c.classCourse.getTheClass().getId()
					&& classCourse.getCourse().getCode().equals(c.classCourse.getCourse().getCode())
					&& weekno == c.weekno;
		}
	}

	private Map<ClassCourseWeek, Integer> countsOfTheoryByWeekno, countsOfTrainingByWeekno;

	private final Map<ClassCourseWeek, Integer> countsOfCourseByWeekno(String courseType) {
		Map<ClassCourseWeek, Integer> ret = COURSE_TYPE_NORMAL.equals(courseType) ? countsOfTheoryByWeekno
				: countsOfTrainingByWeekno;
		if (ret == null) {
			ret = new HashMap<>();
			if (COURSE_TYPE_NORMAL.equals(courseType)) {
				countsOfTheoryByWeekno = ret;
			} else {
				Assert.isTrue(COURSE_TYPE_TRAINING.contentEquals(courseType), "Undefined Course Type");
				countsOfTrainingByWeekno = ret;
			}
			List<Map<String, Object>> all = scheduleRespository.countsOfEachWeek(term.getId(), courseType);
			for (Map<String, Object> e : all) {
				ClassCourse cc = ClassCourse.of((long) e.get("classId"), (String) e.get("courseCode"));
				ClassCourseWeek key = new ClassCourseWeek(cc, (byte) e.get("weekno"));
				int cnt = ((Number) e.get("cnt")).intValue();
				ret.put(key, cnt);
			}
		}
		return ret;
	}

	private int countsByWeek(ClassCourse classCourse, String courseType, byte... weeknos) {
		Map<ClassCourseWeek, Integer> map = countsOfCourseByWeekno(courseType);
		int total = 0;
		for (byte weekno : weeknos) {
			ClassCourseWeek ccw = new ClassCourseWeek(classCourse, weekno);
			Integer cnt = map.get(ccw);
			total += (cnt != null ? cnt : 0);
		}
		return total;
	}

	public int countTheoryScheduleByWeek(ClassCourse classCourse, byte... weeknos) {
		return countsByWeek(classCourse, COURSE_TYPE_NORMAL, weeknos);
	}

	public int countTrainingScheduleByWeek(ClassCourse classCourse, byte... weeknos) {
		return countsByWeek(classCourse, COURSE_TYPE_TRAINING, weeknos);
	}

	public void resolve(ScheduledCourse[] scs, String[] classNames, byte dayOfWeek, Cell scheduledCell) {
		for (ScheduledCourse sc : scs) {
			sc.dayOfWeek = dayOfWeek;
			sc.classCourses = new ArrayList<>(classNames.length);
			sc.resolveSuccess = classNames.length > 0;
			for (String classNameWithDegree : classNames) {
				ClassCourse cc = findClassCourse(classNameWithDegree, sc.courseName, scheduledCell);
				sc.resolveSuccess &= cc != null;
				sc.classCourses.add(cc);
			}
			sc.site = findSite(sc.siteName, scheduledCell);
			if (sc.resolveSuccess) {
				ClassCourse cc = sc.classCourses.get(0);
				sc.teachers = new ArrayList<>(sc.teacherName.length);
				for (String teacherName : sc.teacherName) {
					sc.teachers.add(findTeacher(teacherName, cc, scheduledCell));
				}
			}
		}
	}

	// countOfTheoryByWeek();

}