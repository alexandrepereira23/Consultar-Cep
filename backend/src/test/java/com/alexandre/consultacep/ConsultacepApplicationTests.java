package com.alexandre.consultacep;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class ConsultacepApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void deveDisponibilizarOpenApiDocs() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").exists())
				.andExpect(jsonPath("$.paths['/api/v1/ceps/{cep}']").exists())
				.andExpect(jsonPath("$.components.schemas.EnderecoBasicoResponse").exists())
				.andExpect(jsonPath("$.components.schemas.EnderecoDetalhadoResponse").exists())
				.andExpect(jsonPath("$.components.schemas.LocalizacaoResponse").exists())
				.andExpect(jsonPath("$.components.schemas.ErroResposta").exists());
	}

	@Test
	void deveRedirecionarSwaggerUiHtmlParaIndex() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/swagger-ui/index.html")));
	}

}
