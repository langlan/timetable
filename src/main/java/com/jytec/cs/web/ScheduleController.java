package com.jytec.cs.web;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jytec.cs.domain.Schedule;
import com.jytec.cs.service.ScheduleService;
import com.jytec.cs.service.api.ScheduleSearchParams;
import com.jytec.cs.service.api.ScheduleStatisticParams;

@RestController
public class ScheduleController {
	private @Autowired ScheduleService scheduleService;

	@GetMapping("/schedules")
	public List<Schedule> search(ScheduleSearchParams params, Map<?, ?> mparams) {
		if (mparams.isEmpty() || params.getMapOfNonEmpty().isEmpty()) {
			return Collections.emptyList();
		}
		return scheduleService.search(params);
	}

	@GetMapping("/{id}")
	public Schedule get(@PathVariable Long id) {
		return scheduleService.get(id);
	}

	// Remove? for now also map get to test easily.
	@GetMapping({ "/schedules-statis" })
	public List<?> statisticByGet(ScheduleStatisticParams params) {
		return statistic(params);
	}

	@PostMapping({ "/schedules-statis" })
	public List<?> statistic(@RequestBody ScheduleStatisticParams params) {
		return scheduleService.statistic(params);
	}
}
