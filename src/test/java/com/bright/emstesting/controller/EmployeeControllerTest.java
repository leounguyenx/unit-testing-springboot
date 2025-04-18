package com.bright.emstesting.controller;

import com.bright.emstesting.dto.request.EmployeeRequestDto;
import com.bright.emstesting.dto.response.EmployeeResponseDto;
import com.bright.emstesting.repository.EmployeeRepository;
import com.bright.emstesting.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.List;
import java.util.Optional;


@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    // Simulate HTTP request/response
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeRequestDto employeeRequestDto =
            new EmployeeRequestDto("John", "Doe", "john.doe@gmail.com", "IT");
    private EmployeeResponseDto employeeResponseDto =
            new EmployeeResponseDto("John", "Doe", "IT");
    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("POST /employees should create an return employee")
    void givenEmployeeDto_whenCreate_thenReturnSameEmployeeResponseDto() throws Exception {
        // Set the Mockito behavior for creating an employee
        Mockito.when(employeeService.createEmployee(employeeRequestDto)).thenReturn(employeeResponseDto);


        // When: Send a post request to create an employee
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeRequestDto))

        )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(employeeResponseDto)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("GET /employees should return all employees")
    void getAllEmployees_thenReturnAllEmployeeResponseDto() throws Exception {
        EmployeeResponseDto employeeResponseDto2 = new EmployeeResponseDto("Jane", "Doe", "IT");
        Mockito.when(employeeService.getAllEmployees()).thenReturn(List.of(employeeResponseDto, employeeResponseDto2));
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/employees")
        )
                .andExpectAll(
                        MockMvcResultMatchers.status().isOk(),
                        MockMvcResultMatchers.jsonPath("$[0].firstName").value("John"),
                        MockMvcResultMatchers.jsonPath("$[1].lastName").value("Doe")
                )
                .andDo(MockMvcResultHandlers.print());
    }
}