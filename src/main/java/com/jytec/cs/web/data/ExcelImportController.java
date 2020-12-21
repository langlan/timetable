package com.jytec.cs.web.data;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.jytec.cs.dao.TermRepository;
import com.jytec.cs.excel.AbstractImporter;
import com.jytec.cs.excel.ClassCourseImporter;
import com.jytec.cs.excel.DataCleanService;
import com.jytec.cs.excel.ScheduleImporter;
import com.jytec.cs.excel.SiteImporter;
import com.jytec.cs.excel.TrainingScheduleImporter;
import com.jytec.cs.excel.api.FileType;
import com.jytec.cs.excel.api.ImportParams;
import com.jytec.cs.excel.api.ImportReport;
import com.jytec.cs.service.TermService;
import com.jytec.cs.util.Dates;
import com.jytec.cs.util.Dates.CalenderWrapper;

@RequestMapping("/data")
@Controller
public class ExcelImportController {
	private @Autowired ClassCourseImporter classCourseImporter;
	private @Autowired SiteImporter siteImporter;
	private @Autowired ScheduleImporter scheduleImporter;
	private @Autowired TrainingScheduleImporter trainingScheduleImporter;
	private @Autowired TermService termService;
	private @Autowired TermRepository termRepository;
	private @Autowired DataCleanService dataCleanService;

	@Value("${spring.cs.upload.dir}")
	private File dir;

	@GetMapping
	public String index(ModelMap mm, HttpSession session) {
		mm.put("terms", termRepository.findAll());
		UploadedFile lru = (UploadedFile) session.getAttribute("last-recent-uploaded");
		if (lru != null) {
			mm.put("originalFileName", lru.originalName);
		}

		// classYear
		CalenderWrapper w = Dates.wrapper();
		int cyear = w.getYear();
		List<String> years = new LinkedList<>();
		for (int year = cyear - 3; year <= cyear + 1; year++) {
			years.add(Integer.toString(year % 2000));
		}
		mm.put("years", years);
		// currDate
		mm.put("currDate", w.format());

		return "upload";
	}

	@PostMapping
	public String handleFileUpload(@RequestParam("ufile") MultipartFile ufile, FileType fileType, ImportParams params,
			@RequestParam Map<String, String> reqParams, RedirectAttributesModelMap mm, HttpSession session) {
		UploadedFile lru = (UploadedFile) session.getAttribute("last-recent-uploaded");
		for (Entry<String, String> e : reqParams.entrySet()) {
			mm.addFlashAttribute(e.getKey(), e.getValue());
		}
		if (ufile != null && ufile.getSize() > 0) {
			lru = new UploadedFile();
			lru.dest = new File(dir, "last-recent-uploaded");
			lru.originalName = ufile.getOriginalFilename();
			try {
				lru.dest.getParentFile().mkdirs();
				ufile.transferTo(lru.dest);
				session.setAttribute("last-recent-uploaded", lru);
			} catch (IllegalStateException | IOException e) {
				mm.addFlashAttribute("message", e.getMessage());
			}
		} else if (lru == null) {
			mm.addFlashAttribute("message", "请选择文件");
		}

		if (lru != null) {
			// mm.put("originalFileName", lru.originalName);
			if (fileType == null) {
				mm.addFlashAttribute("message", "请选择文件内容类型");
			} else {
				params.file(lru.dest);
				AbstractImporter impoter = getImporter(fileType);
				try {
					ImportReport report = impoter.importFile(params);
					mm.addFlashAttribute("report", report);
				} catch (Exception e) {
					mm.addFlashAttribute("message", e.getMessage());
				}
			}
		}
		return "redirect:/data";
	}

	@PostMapping("/del")
	public String del(String termId, FileType[] fileTypes, RedirectAttributesModelMap mm) {
		dataCleanService.cleanData(termId, fileTypes);
		String msg = "成功删除所选数据！";
		// List<FileType> _fileTypes = Arrays.asList(fileTypes);
		mm.addFlashAttribute("message", msg);
		return "redirect:/data";
	}

	@PostMapping("/term-init")
	public String termInit(@DateTimeFormat(iso = ISO.DATE) Date firstWeek, int numberOfWeeks,
			RedirectAttributesModelMap mm) {
		termService.initTermDate(firstWeek, numberOfWeeks);
		mm.addFlashAttribute("message", "成功初始化学期日历数据！");
		return "redirect:/data";
	}

	@PostMapping("/term-del")
	public String termDel(String termId, RedirectAttributesModelMap mm) {
		termService.deleteTermDate(termId);
		// mm.addFlashAttribute("message", "暂不支持删除学期记录！");
		mm.addFlashAttribute("message", "成功删除学期日历数据！");
		return "redirect:/data";
	}

	@ExceptionHandler
	public String termInit(Exception e, ModelMap mm, HttpSession session) {
		mm.put("message", e.getMessage());
		return index(mm, session);
	}

	private AbstractImporter getImporter(FileType fileType) {
		switch (fileType) {
		case CLASS_COURSE:
			return classCourseImporter;
		case SITE:
			return siteImporter;
		case SCHEDULE:
			return scheduleImporter;
		case SCHEDULE_TRAINING:
			return trainingScheduleImporter;
		}
		return null;
	}

	class UploadedFile {
		String fileType;
		String originalName;
		File dest;
	}

}
