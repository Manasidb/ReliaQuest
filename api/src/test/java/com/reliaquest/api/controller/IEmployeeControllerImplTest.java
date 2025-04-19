package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.reliaquest.api.dto.CreateEmployeeRequest;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.service.IEmployeeService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
public class IEmployeeControllerImplTest {

    @Autowired
    private IEmployeeControllerImpl employeeController;

    @MockBean
    private IEmployeeService iEmployeeService;

    private Employee employee1;

    @BeforeEach
    void setup() {
        employee1 = new Employee("1", "employee1", "1000000", "18", "title employee1", "employee1@gmail.com");
    }

    @Test
    void testGetAllEmployees() {
        List<Employee> employees = List.of(employee1);
        Mockito.when(iEmployeeService.getAllEmployees()).thenReturn(employees);

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("employee1", response.getBody().get(0).getName());
    }

    @Test
    void testGetEmployeesByNameSearch() {
        List<Employee> employees = List.of(employee1);
        Mockito.when(iEmployeeService.getEmployeesByNameSearch("employee1")).thenReturn(employees);

        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch("employee1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetEmployeeById() {
        Mockito.when(iEmployeeService.getEmployeeById("1")).thenReturn(employee1);

        ResponseEntity<Employee> response = employeeController.getEmployeeById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("employee1", response.getBody().getName());
    }

    @Test
    void testGetHighestSalaryOfEmployees() {
        Mockito.when(iEmployeeService.getHighestSalaryOfEmployees()).thenReturn(1000000);

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1000000, response.getBody());
    }

    @Test
    void testGetTopTenHighestEarningEmployeeNames() {
        List<String> topEarners = List.of("employee1", "employee2");
        Mockito.when(iEmployeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(topEarners);

        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testCreateEmployee() {
        CreateEmployeeRequest request =
                new CreateEmployeeRequest("employee1", "1000000", "18", "title employee1", "employee1@gmail.com");
        Mockito.when(iEmployeeService.createEmployee(request)).thenReturn(employee1);

        ResponseEntity<Employee> response = employeeController.createEmployee(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("employee1", response.getBody().getName());
    }

    @Test
    void testDeleteEmployeeById() {
        Mockito.when(iEmployeeService.deleteEmployeeById("1")).thenReturn("Deleted");

        ResponseEntity<String> response = employeeController.deleteEmployeeById("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Deleted", response.getBody());
    }
}
