package com.bright.emstesting.service.impl;

import com.bright.emstesting.dto.request.EmployeeRequestDto;
import com.bright.emstesting.dto.response.EmployeeResponseDto;
import com.bright.emstesting.exception.employee.DuplicateEmailException;
import com.bright.emstesting.model.Employee;
import com.bright.emstesting.repository.EmployeeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {
    // Mock employeeRepository
    @Mock
    EmployeeRepository employeeRepository;

    // Inject employeeRepository to employeeService
    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;
    private EmployeeRequestDto employeeRequestDto;

    @BeforeEach
    void setUp() {
        // An employee
        employee = Employee.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@gmail.com")
                .departmentCode("IT")
                .build();

        // An employeeRequestDto
        employeeRequestDto = new EmployeeRequestDto(
                "John", "Smith", "john.smith@gmail.com", "IT"
        );
    }

    @Test
    @DisplayName("Create an employee when email doesn't exist")
    void givenEmployeeRequestDto_whenCreateEmployee_thenReturnEmmployeeResponseDto() {
        // Set the Mockito behavior
        Mockito.when(employeeRepository.save(Mockito.any(Employee.class))).thenReturn(employee);

        // When
        EmployeeResponseDto employeeResponseDto = employeeService.createEmployee(employeeRequestDto);

        // Then
        assertNotNull(employeeResponseDto);
        assertThat(employeeResponseDto)
                .isEqualTo( new EmployeeResponseDto(
                        employeeRequestDto.firstName(),
                        employeeRequestDto.lastName(),
                        employeeRequestDto.departmentCode()
                ));
        Mockito.verify(employeeRepository, Mockito.times(1)).findByEmail(employeeRequestDto.email());
        Mockito.verify(employeeRepository, Mockito.times(1)).save(Mockito.any(Employee.class));
    }

    @Test
    @DisplayName("Create an employee with existing email should throw Exception")
    public void givenExistingEmployee_whenCreateEmployee_thenThrowsException() {
        Mockito.when(employeeRepository.findByEmail(employeeRequestDto.email())).thenReturn(Optional.of(employee));
        Assertions.assertThrows(DuplicateEmailException.class, () -> employeeService.createEmployee(employeeRequestDto));
        Mockito.verify(employeeRepository, Mockito.times(1)).findByEmail(employeeRequestDto.email());
        Mockito.verify(employeeRepository, Mockito.never()).save(Mockito.any(Employee.class));
    }

    @Test
    @DisplayName("Update employee when email exists should return updated response dto")
    public void givenExistingEmployeeRequestDto_whenUpdateEmployee_thenReturnUpdatedEmployeeResponseDto() {
        // Set the Mockito behavior
        Mockito.when(employeeRepository.findByEmail(employeeRequestDto.email())).thenReturn(Optional.of(employee));
        Mockito.when(employeeRepository.save(Mockito.any(Employee.class))).thenReturn(employee);

        // When
        EmployeeResponseDto employeeResponseDto =
                employeeService.updateEmployee(employeeRequestDto.email(), employeeRequestDto);
        // Then
        Mockito.verify(employeeRepository, Mockito.times(1)).findByEmail(employeeRequestDto.email());
        Mockito.verify(employeeRepository, Mockito.times(1)).save(Mockito.any(Employee.class));
        assertThat(employeeResponseDto).isEqualTo( new EmployeeResponseDto(
                employeeRequestDto.firstName(),
                employeeRequestDto.lastName(),
                employeeRequestDto.departmentCode()
        ));
    }

    @Test
    @DisplayName("Delete an existing employee")
    void givenExistingEmail_whenDelete_thenDeleted(){
        // Set the Mockito behavior
        Mockito.when(employeeRepository.findByEmail(employeeRequestDto.email())).thenReturn(Optional.of(employee));
        // When
        employeeService.deleteEmployee(employeeRequestDto.email());
        // Then
        Mockito.verify(employeeRepository, Mockito.times(1)).findByEmail(employeeRequestDto.email());
        Mockito.verify(employeeRepository, Mockito.times(1)).deleteByEmail(employeeRequestDto.email());
    }
}