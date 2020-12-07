/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jytec.cs.web;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class WebApiTests {

	@Autowired
	private MockMvc mockMvc;

	/*
	 * @BeforeEach public void deleteAllBeforeTests() throws Exception { personRepository.deleteAll(); }
	 */

	@Test
	public void termsTest() throws Exception {
		mockMvc.perform(get("/terms")).andDo(print()) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$[0].id").exists());
		mockMvc.perform(get("/terms?termId=202009")).andDo(print()) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$[0].id").value("202009"));
		mockMvc.perform(get("/terms?term=209909")) //
				.andExpect(status().isOk()) //
				.andExpect(content().json("[]"));

		mockMvc.perform(get("/terms/202009")) //
				.andExpect(jsonPath("$.id").value("202009"));
	}

	@Test
	public void weeksTest() throws Exception {
		mockMvc.perform(get("/weeks?termId=202009")).andDo(print()) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$[0].firstDay").value("2020-09-07"));
		mockMvc.perform(get("/weeks?term=202009&weekno=1")) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$[0].termId").value("202009"))//
				.andExpect(jsonPath("$[0].weekno").value(1)) //
				.andExpect(jsonPath("$[1]").doesNotExist());
	}
	
	// /dates? : weekno, dayOfWeek, holiday, date, year, month
	
	@Test
	public void deptsTest() throws Exception {
		mockMvc.perform(get("/depts")).andDo(print()) 
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$[9].name").exists());
	}
	
	@Test
	public void majorsTest() throws Exception {
		mockMvc.perform(get("/majors")).andDo(print()) 
		.andExpect(status().isOk()) //
		.andExpect(jsonPath("$[9].name").exists());
	}
	
	@Test
	public void classesTest() throws Exception {
		mockMvc.perform(get("/classes")).andDo(print()) 
		.andExpect(status().isOk()) //
		.andExpect(jsonPath("$.length()").value(greaterThan(100)));
		mockMvc.perform(get("/classes?majorId=2")).andDo(print()) 
		.andExpect(status().isOk()) //
		.andExpect(jsonPath("$.length()").value(lessThan(30)))
		.andExpect(jsonPath("$[0].majorId").value(2));
	} 
	
	@Test
	public void sitesTest() throws Exception {
		mockMvc.perform(get("/sites/1")).andExpect(status().isOk()) //
		.andExpect(jsonPath("$.id").value(1));
		mockMvc.perform(get("/sites/code/90194")).andExpect(status().isOk()) //
		.andExpect(jsonPath("$.code").value("90194"))
		.andExpect(jsonPath("$.name").value("信息223A"));
	}
}
