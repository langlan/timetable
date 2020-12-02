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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
		mockMvc.perform(get("/terms?termYear=2020&termMonth=09")).andDo(print()) //
				.andExpect(status().isOk()) //
				.andExpect(jsonPath("$[0].id").value("202009"));
		mockMvc.perform(get("/terms?term=209909")) //
				.andExpect(status().isOk()) //
				.andExpect(content().json("[]"));

		mockMvc.perform(get("/terms/202009")) //
				.andExpect(jsonPath("$.id").value("202009"));
	}

	
}
