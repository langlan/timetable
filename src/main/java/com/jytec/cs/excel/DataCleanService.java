package com.jytec.cs.excel;

import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jytec.cs.dao.ClassCourseRepository;
import com.jytec.cs.dao.ScheduleRepository;
import com.jytec.cs.dao.SiteRepository;
import com.jytec.cs.domain.Schedule;
import com.jytec.cs.excel.api.FileType;

@Service
public class DataCleanService {
	private Log log = LogFactory.getLog(getClass());
	private @Autowired ClassCourseRepository classCourseRepository;
	private @Autowired SiteRepository siteRepository;
	private @Autowired ScheduleRepository scheduleRepository;

	@Transactional
	public void cleanData(String termId, FileType... fileTypes) {
		List<FileType> _fileTypes = Arrays.asList(fileTypes);
		if (_fileTypes.contains(FileType.CLASS_COURSE) || _fileTypes.contains(FileType.SCHEDULE)
				|| _fileTypes.contains(FileType.SCHEDULE_TRAINING)) {
			if (termId == null || termId.isEmpty()) {
				throw new IllegalArgumentException("须为要删除的（教学任务或排课）数据指定学期！");
			}
		}
		for (FileType fileType : fileTypes) {
			int cnt = 0;
			switch (fileType) {
			case CLASS_COURSE:
				cnt = classCourseRepository.deleteAllByTermId(termId);
				log.info("删除班级选课记录【" + cnt + "】, termId: " + termId);
				break;
			case SITE:
				siteRepository.deleteAll();
				break;
			case SCHEDULE:
				cnt = scheduleRepository.deleteAllByTermIdAndCourseType(termId, Schedule.COURSE_TYPE_NORMAL);
				log.info("删除理论排课记录【" + cnt + "】, termId: " + termId);
				break;
			case SCHEDULE_TRAINING:
				cnt = scheduleRepository.deleteAllByTermIdAndCourseType(termId, Schedule.COURSE_TYPE_TRAINING);
				log.info("删除培训排课记录【" + cnt + "】, termId: " + termId);
				break;
			}
		}
	}
}
