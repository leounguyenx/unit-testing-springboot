package com.bright.emstesting;

import com.bright.emstesting.exception.employee.DuplicateEmailException;
import com.bright.emstesting.model.Employee;
import com.bright.emstesting.repository.EmployeeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class EmployeeRepositoryTest {
    @Autowired
    EmployeeRepository repository;

    @Autowired
    private TestEntityManager testEntityManager;     // Flush data immediately

    Employee employee;


    @BeforeEach
    public void setup() {
        employee = Employee.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@gmail.com")
                .departmentCode("IT")
                .build();
    }

    @Test
    @DisplayName("Test for creating a new employee")
    public void givenNonExistentEmployee_whenSaved_thenReturnsResponseDto() {
        // Given: An employee is not in the database

        // When
        // saveAndFlush: Save and hit the data to DB immediately
        Employee savedEmployee = repository.saveAndFlush(employee);
        // Then
        // Test whether entity is saved or not
        assertNotNull(savedEmployee);

        String expectedFirstName = employee.getFirstName();
        String expectedLastName = employee.getLastName();

        assertEquals(expectedFirstName, savedEmployee.getFirstName());
        assertEquals(expectedLastName, savedEmployee.getLastName());
    }

    @Test
    @DisplayName("Test for existing employee")
    public void givenExistentEmployee_whenSaved_thenThrowsDuplicateException() {
        // Given: An existent employee
        Employee savedEmployee = repository.saveAndFlush(employee);

        // When: Save the same employee
        Employee employee2 = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.smith@gmail.com")
                .build();

        // Then: Throws exception when save existing employee
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(employee2));
    }

    @Test
    @DisplayName("Test for finding an employee by email")
    public void givenExistentEmployee_whenFindingEmployeeByEmail_thenReturnEmployee() {
        // Given: An existent employee
        Employee savedEmployee = repository.saveAndFlush(employee);

        // When: Find existent employee by email
        Optional<Employee> actualEmployee = repository.findByEmail(employee.getEmail());

        // Then: Verify firstName and lastName
        assertTrue(actualEmployee.isPresent());
        assertEquals(savedEmployee.getFirstName(), actualEmployee.get().getFirstName());
        assertEquals(savedEmployee.getLastName(), actualEmployee.get().getLastName());
    }

    @Test
    @DisplayName("Test for finding an employee by departmentCode")
    public void givenExistentEmployee_whenFindingEmployeeByDepartmentCode_thenReturnEmployeeList() {
        // Given: An existent employee
        Employee savedEmployee = repository.saveAndFlush(employee);

        // Save another employee
        Employee employee2 = Employee.builder()
                .firstName("Tiny")
                .lastName("Cutie")
                .email("tiny.cutie@gmail.com")
                .departmentCode("IT")
                .build();
        repository.saveAndFlush(employee2);

        // When: Find employee list by department code
        List<Employee> actualEmployeeList = repository.findByDepartmentCodeIgnoreCase("IT");

        // Get all employee's emails
        List<String> actualEmailList = actualEmployeeList.stream().map(Employee::getEmail).collect(Collectors.toList());

        // Then
        Assertions.assertNotNull(actualEmailList);
        Assertions.assertEquals(2, actualEmailList.size());
        // Verify
        assertTrue(actualEmployeeList.stream().anyMatch(employee -> employee.getDepartmentCode().equalsIgnoreCase("IT")));
        assertThat(actualEmailList).containsExactly("john.smith@gmail.com", "tiny.cutie@gmail.com");
    }

    @Test
    @DisplayName("Test for deleting an exist employee")
    public void givenExistentEmployee_whenDeletingEmployee_thenDeleteEmployee() {
        // Given:
        Employee savedEmployee = repository.saveAndFlush(employee);
        // When: Delete an employee

        /// Using query -> Hit DB directly
        repository.deleteByEmail(savedEmployee.getEmail());

        /// Perform by JPA and it will in the persistence state. Use flush to hit DB immediately
//        repository.deleteById(savedEmployee.getId());
//        testEntityManager.flush();

        // Then: Find deleted employee to verify it
        Optional<Employee> foundEmployee = repository.findByEmail(savedEmployee.getEmail());
        assertFalse(foundEmployee.isPresent());

        /// Or can use this way
//        Employee foundEmployee = repository.findByEmail(savedEmployee.getEmail()).orElse(null);
//        assertNull(foundEmployee);
    }

}
