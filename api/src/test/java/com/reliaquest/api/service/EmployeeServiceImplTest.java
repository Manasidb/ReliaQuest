package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import com.reliaquest.api.dto.*;
import com.reliaquest.api.exception.CustomRuntimeException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class EmployeeServiceImplTest {
    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private EmployeeServiceImpl employeeServiceImpl;

    @Value("${employee.api.url}")
    private String MOCK_EMPLOYEE_API_URL;

    private static List<Employee> employeeList;

    private static CreateEmployeeRequest createEmployeeRequest;

    @BeforeAll
    static void setup() {
        Employee employee1 = new Employee("1", "employee1", "1000000", "18", "title employee1", "employee1@gmail.com");
        Employee employee2 = new Employee("2", "employee2", "2000000", "19", "title employee2", "employee2@gmail.com");
        Employee employee3 = new Employee("3", "employee3", "3000000", "20", "title employee3", "employee3@gmail.com");
        Employee employee4 = new Employee("4", "employee4", "4000000", "21", "title employee4", "employee4@gmail.com");
        Employee employee5 = new Employee("5", "employee5", "5000000", "22", "title employee5", "employee5@gmail.com");
        Employee employee6 = new Employee("6", "employee6", "6000000", "23", "title employee6", "employee6@gmail.com");
        Employee employee7 = new Employee("7", "employee7", "7000000", "24", "title employee7", "employee7@gmail.com");
        Employee employee8 = new Employee("8", "employee8", "7000000", "25", "title employee8", "employee8@gmail.com");
        Employee employee9 = new Employee("9", "employee9", "9000000", "26", "title employee9", "employee9@gmail.com");
        Employee employee10 =
                new Employee("10", "employee10", "9900000", "27", "title employee10", "employee10@gmail.com");

        employeeList = List.of(
                employee1,
                employee2,
                employee3,
                employee4,
                employee5,
                employee6,
                employee7,
                employee8,
                employee9,
                employee10);

        createEmployeeRequest = new CreateEmployeeRequest();
        createEmployeeRequest.setName("abc");
        createEmployeeRequest.setAge("50");
        createEmployeeRequest.setSalary("100000");
        createEmployeeRequest.setTitle("title");
        createEmployeeRequest.setEmail("abc@gmail.com");
    }

    @Test
    void getAllEmployeesSuccess() {
        // Mock get all employee
        EmployeeResponseWrapper employeeResponseWrapper = new EmployeeResponseWrapper();
        employeeResponseWrapper.setData(employeeList);

        ResponseEntity<EmployeeResponseWrapper> expectedEmployeeResponseWrapper =
                new ResponseEntity<>(employeeResponseWrapper, HttpStatus.OK);

        List<Employee> expectedResponse =
                expectedEmployeeResponseWrapper.getBody().getData();

        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL), eq(HttpMethod.GET), isNull(), eq(EmployeeResponseWrapper.class)))
                .thenReturn(expectedEmployeeResponseWrapper);

        // Act
        List<Employee> actualResponse = employeeServiceImpl.getAllEmployees();

        // Assert
        assertEquals(expectedResponse.size(), actualResponse.size());
    }

    @Test
    void getAllEmployeesFallbackTriggered() {
        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL), eq(HttpMethod.GET), isNull(), eq(EmployeeResponseWrapper.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

        List<Employee> result = employeeServiceImpl.getAllEmployees();

        // assert result from fallback (assuming fallback returns empty list)
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmployeesByNameSearchSuccess() {
        // Mock get all employee
        EmployeeResponseWrapper employeeResponseWrapper = new EmployeeResponseWrapper();
        employeeResponseWrapper.setData(employeeList);

        ResponseEntity<EmployeeResponseWrapper> expectedEmployeeResponseWrapper =
                new ResponseEntity<>(employeeResponseWrapper, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL), eq(HttpMethod.GET), isNull(), eq(EmployeeResponseWrapper.class)))
                .thenReturn(expectedEmployeeResponseWrapper);

        // Act
        List<Employee> actualResponse = employeeServiceImpl.getEmployeesByNameSearch("employee2");

        // Assert
        assertEquals(1, actualResponse.size());
        assertEquals("employee2", actualResponse.get(0).getName());
    }

    @Test
    void getEmployeesByNameSearchNotFound() {
        String employeeName = "xyz";

        // Mock get all employee
        EmployeeResponseWrapper employeeResponseWrapper = new EmployeeResponseWrapper();
        employeeResponseWrapper.setData(Collections.emptyList());

        ResponseEntity<EmployeeResponseWrapper> expectedEmployeeResponseWrapper =
                new ResponseEntity<>(employeeResponseWrapper, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL), eq(HttpMethod.GET), isNull(), eq(EmployeeResponseWrapper.class)))
                .thenReturn(expectedEmployeeResponseWrapper);

        // Act and Assert
        EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class, () -> {
            employeeServiceImpl.getEmployeesByNameSearch(employeeName);
        });
        assertEquals("Employee with name xyz not found.", exception.getMessage());
    }

    @Test
    void getEmployeeByIdSuccess() {
        // Mock get employee by id
        Employee employee = new Employee("1", "employee1", "1000000", "18", "title employee1", "employee1@gmail.com");
        EmployeeResponse employeeResponse = new EmployeeResponse();
        employeeResponse.setData(employee);

        ResponseEntity<EmployeeResponse> expectedEmployeeResponseWrapper =
                new ResponseEntity<>(employeeResponse, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL + "/" + employee.getId()),
                        eq(HttpMethod.GET),
                        isNull(),
                        eq(EmployeeResponse.class)))
                .thenReturn(expectedEmployeeResponseWrapper);

        // Act
        Employee actualResponse = employeeServiceImpl.getEmployeeById("1");

        // Assert
        assertEquals(expectedEmployeeResponseWrapper.getBody().getData().getId(), actualResponse.getId());
    }

    @Test
    void getEmployeeByIdNotFound() {
        String employeeId = "123";
        // Empty headers for this test
        HttpHeaders headers = new HttpHeaders();

        // Mock RestTemplate throwing HttpClientErrorException (
        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        eq(EmployeeResponse.class)))
                .thenThrow(new HttpClientErrorException(
                        HttpStatus.NOT_FOUND, "Employee not found", headers, null, StandardCharsets.UTF_8));

        // Act and Assert
        EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class, () -> {
            employeeServiceImpl.getEmployeeById(employeeId);
        });

        assertEquals("Employee with id 123 not found.", exception.getMessage());
    }

    @Test
    void getEmployeeByIdFailure() {
        String employeeId = "123";
        HttpHeaders headers = new HttpHeaders();

        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        eq(EmployeeResponse.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

        // Act and Assert
        CustomRuntimeException exception = assertThrows(CustomRuntimeException.class, () -> {
            employeeServiceImpl.getEmployeeById(employeeId);
        });

        assertTrue(exception.getMessage().contains("Unexpected error occurred while fetching employee by id:"));
    }

    @Test
    void getHighestSalaryOfEmployeesSuccess() {
        // Mock get all employee
        EmployeeResponseWrapper employeeResponseWrapper = new EmployeeResponseWrapper();
        employeeResponseWrapper.setData(employeeList);

        ResponseEntity<EmployeeResponseWrapper> expectedEmployeeResponseWrapper =
                new ResponseEntity<>(employeeResponseWrapper, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL), eq(HttpMethod.GET), isNull(), eq(EmployeeResponseWrapper.class)))
                .thenReturn(expectedEmployeeResponseWrapper);

        // Act
        Integer result = employeeServiceImpl.getHighestSalaryOfEmployees();

        // Assert
        assertEquals(Integer.parseInt("9900000"), result);
    }

    @Test
    void getHighestSalaryOfEmployeesFailure() {

        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL), eq(HttpMethod.GET), isNull(), eq(EmployeeResponseWrapper.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

        // Act & Assert
        CustomRuntimeException exception = assertThrows(CustomRuntimeException.class, () -> {
            employeeServiceImpl.getHighestSalaryOfEmployees();
        });

        assertTrue(exception.getMessage().contains("Unexpected error occured while fetching highest salary"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNamesSuccess() {
        // Mock employees
        EmployeeResponseWrapper employeeResponseWrapper = new EmployeeResponseWrapper();
        employeeResponseWrapper.setData(employeeList);

        ResponseEntity<EmployeeResponseWrapper> expectedEmployeeResponseWrapper =
                new ResponseEntity<>(employeeResponseWrapper, HttpStatus.OK);

        List<String> expectedResult = expectedEmployeeResponseWrapper.getBody().getData().stream()
                .sorted(Comparator.comparingInt(employee -> Integer.parseInt(employee.getSalary())))
                .limit(10)
                .map(Employee::getName)
                .toList();

        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL), eq(HttpMethod.GET), isNull(), eq(EmployeeResponseWrapper.class)))
                .thenReturn(expectedEmployeeResponseWrapper);

        // Act
        List<String> result = employeeServiceImpl.getTopTenHighestEarningEmployeeNames();

        // Assert
        assertEquals(expectedResult, result);
    }

    @Test
    void getTopTenHighestEarningEmployeeNamesFailure() {
        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL), eq(HttpMethod.GET), isNull(), eq(EmployeeResponseWrapper.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

        // Act & Assert
        CustomRuntimeException exception = assertThrows(CustomRuntimeException.class, () -> {
            employeeServiceImpl.getTopTenHighestEarningEmployeeNames();
        });

        assertTrue(exception
                .getMessage()
                .contains("Unexpected error occured while fetching top ten highest earning employee"));
    }

    @Test
    void createEmployeeSuccess() {
        // Mock get employee by id
        EmployeeResponseWrapper employeeResponseWrapper = new EmployeeResponseWrapper();
        employeeResponseWrapper.setData(Collections.emptyList());

        ResponseEntity<EmployeeResponseWrapper> emptySearchResponse =
                new ResponseEntity<>(employeeResponseWrapper, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL), eq(HttpMethod.GET), isNull(), eq(EmployeeResponseWrapper.class)))
                .thenReturn(emptySearchResponse);

        // Mock create employee
        Employee employee = new Employee("1", "abc", "100000", "50", "title", "abc@gmail.com");
        EmployeeResponse employeeResponse = new EmployeeResponse();
        employeeResponse.setData(employee);

        ResponseEntity<EmployeeResponse> responseEntity = new ResponseEntity<>(employeeResponse, HttpStatus.CREATED);

        Mockito.when(restTemplate.postForEntity(
                        eq(MOCK_EMPLOYEE_API_URL), Mockito.any(HttpEntity.class), eq(EmployeeResponse.class)))
                .thenReturn(responseEntity);

        // Act
        Employee result = employeeServiceImpl.createEmployee(createEmployeeRequest);

        // Assert
        assertEquals("abc", result.getName());
        assertEquals("100000", result.getSalary());
    }

    @Test
    void createEmployeeFailure() {
        Mockito.when(restTemplate.postForEntity(
                        eq(MOCK_EMPLOYEE_API_URL), Mockito.any(HttpEntity.class), eq(EmployeeResponse.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

        CustomRuntimeException exception = assertThrows(CustomRuntimeException.class, () -> {
            employeeServiceImpl.createEmployee(createEmployeeRequest);
        });

        assertTrue(exception.getMessage().contains("Unexpected error during employee creation:"));
    }

    @Test
    void testDeleteEmployeeByIdSuccess() {
        String employeeId = "1";
        String employeeName = "abc";

        // Mock GET Employee by ID
        Employee mockEmployee = new Employee();
        mockEmployee.setId(employeeId);
        mockEmployee.setName(employeeName);
        mockEmployee.setAge("30");
        mockEmployee.setSalary("50000");
        mockEmployee.setTitle("Engineer");
        mockEmployee.setEmail("abc@gmail.com");

        EmployeeResponse mockGetResponse = new EmployeeResponse();
        mockGetResponse.setData(mockEmployee);

        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        eq(EmployeeResponse.class)))
                .thenReturn(new ResponseEntity<>(mockGetResponse, HttpStatus.OK));

        //  Mock DELETE Employee
        DeleteEmployeeResponse deleteResponse = new DeleteEmployeeResponse();
        deleteResponse.setData(true);

        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL),
                        eq(HttpMethod.DELETE),
                        Mockito.any(HttpEntity.class),
                        eq(DeleteEmployeeResponse.class),
                        (Object) Mockito.any()))
                .thenReturn(new ResponseEntity<>(deleteResponse, HttpStatus.OK));

        // Act
        String result = employeeServiceImpl.deleteEmployeeById(employeeId);

        // Assert
        assertEquals("Employee with id 1 deleted successfully", result);
    }

    @Test
    void testDeleteEmployeeByIdNotFound() {
        String employeeId = "1";
        HttpHeaders headers = new HttpHeaders();

        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL + "/" + employeeId),
                        eq(HttpMethod.GET),
                        isNull(),
                        eq(EmployeeResponse.class)))
                .thenThrow(new HttpClientErrorException(
                        HttpStatus.NOT_FOUND, "Employee not found", headers, null, StandardCharsets.UTF_8));

        EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class, () -> {
            employeeServiceImpl.deleteEmployeeById(employeeId);
        });
        assertTrue(exception.getMessage().contains("Employee with id " + employeeId + " not found."));
    }

    @Test
    void testDeleteEmployeeByIdFailure() {
        String employeeId = "1";
        Mockito.when(restTemplate.exchange(
                        eq(MOCK_EMPLOYEE_API_URL),
                        eq(HttpMethod.DELETE),
                        Mockito.any(HttpEntity.class),
                        eq(DeleteEmployeeResponse.class),
                        (Object) Mockito.any()))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

        CustomRuntimeException exception = assertThrows(CustomRuntimeException.class, () -> {
            employeeServiceImpl.deleteEmployeeById(employeeId);
        });

        assertTrue(exception.getMessage().contains("Unexpected error occured while deleting employee by id"));
    }
}
