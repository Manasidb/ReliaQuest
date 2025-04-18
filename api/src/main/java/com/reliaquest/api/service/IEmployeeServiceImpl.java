package com.reliaquest.api.service;

import com.reliaquest.api.dto.*;
import com.reliaquest.api.exception.EmployeeAlreadyExistsException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class IEmployeeServiceImpl implements IEmployeeService {

    private final RestTemplate restTemplate;
    private static final String MOCK_EMPLOYEE_API = "http://localhost:8112/api/v1/employee";

    @Autowired
    public IEmployeeServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Employee> getAllEmployees() {
        try {

            ResponseEntity<EmployeeResponseWrapper> response = fetchEmployeeData();
            return response.getBody().getData();

        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error occured while fetching employees: " + ex.getMessage());
        }
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {

        try {
            List<Employee> employees = searchEmployeesByName(searchString);
            if (employees.isEmpty()) {

                throw new EmployeeNotFoundException("Employee with name " + searchString + " not found.");
            }
            return employees;
        } catch (EmployeeNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error occured while fetching employee by name " + ex.getMessage());
        }
    }

    @Override
    public Employee getEmployeeById(String id) {
        String url = MOCK_EMPLOYEE_API + "/" + id;
        try {
            ResponseEntity<EmployeeResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, null, EmployeeResponse.class);

            Employee employee = response.getBody().getData();
            return employee;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new EmployeeNotFoundException("Employee with id " + id + " not found.");
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error occured while fetching employee by id " + ex.getMessage());
        }
    }

    @Override
    public Integer getHighestSalaryOfEmployees() {

        try {
            ResponseEntity<EmployeeResponseWrapper> response = fetchEmployeeData();
            return response.getBody().getData().stream()
                    .mapToInt(employee -> Integer.parseInt(employee.getSalary()))
                    .max()
                    .orElseThrow(() -> new RuntimeException("There is no maximum salary"));

        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error occured while fetching highest salary " + ex.getMessage());
        }
    }

    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {

        try {
            ResponseEntity<EmployeeResponseWrapper> response = fetchEmployeeData();

            return response.getBody().getData().stream()
                    .sorted(Comparator.comparingInt(employee -> Integer.parseInt(employee.getSalary())))
                    .limit(10)
                    .map(Employee::getName)
                    .toList();

        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error occured while fetching highest salary " + ex.getMessage());
        }
    }

    @Override
    public Employee createEmployee(CreateEmployeeRequest createEmployeeRequest) {

        try {
            if (!searchEmployeesByName(createEmployeeRequest.getName()).isEmpty()) {
                throw new EmployeeAlreadyExistsException(
                        "Employee with name " + createEmployeeRequest.getName() + " already exists");
            }

            HttpEntity<CreateEmployeeRequest> requestEntity = new HttpEntity<>(createEmployeeRequest, getHeader());
            ResponseEntity<EmployeeResponse> response =
                    restTemplate.postForEntity(MOCK_EMPLOYEE_API, requestEntity, EmployeeResponse.class);

            return response.getBody().getData();

        } catch (EmployeeAlreadyExistsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error during employee creation: " + ex.getMessage());
        }
    }

    @Override
    public String deleteEmployeeById(String id) {

        try {
            String employeeName = getEmployeeById(id).getName();
            if (employeeName == null) {
                throw new EmployeeNotFoundException("Employee with id " + id + " doesn't exists");
            }
            DeleteEmployeeRequest deleteEmployeeRequest = new DeleteEmployeeRequest();
            deleteEmployeeRequest.setName(employeeName);

            HttpEntity<DeleteEmployeeRequest> requestEntity = new HttpEntity<>(deleteEmployeeRequest, getHeader());

            ResponseEntity<DeleteEmployeeResponse> response = restTemplate.exchange(
                    MOCK_EMPLOYEE_API, HttpMethod.DELETE, requestEntity, DeleteEmployeeResponse.class, employeeName);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isData()) {
                return "Employee with id " + id + " deleted successfully";
            }
            throw new RuntimeException("Failed to delete employee with id " + id);
        } catch (EmployeeNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error occured while deleting employee by id " + ex.getMessage());
        }
    }

    private ResponseEntity<EmployeeResponseWrapper> fetchEmployeeData() {
        ResponseEntity<EmployeeResponseWrapper> response =
                restTemplate.exchange(MOCK_EMPLOYEE_API, HttpMethod.GET, null, EmployeeResponseWrapper.class);
        return response;
    }

    private HttpHeaders getHeader() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        return header;
    }

    private List<Employee> searchEmployeesByName(String searchString) {
        try {
            ResponseEntity<EmployeeResponseWrapper> response = fetchEmployeeData();

            return response.getBody().getData().stream()
                    .filter(emp ->
                            emp.getName() != null && emp.getName().toLowerCase().contains(searchString.toLowerCase()))
                    .toList();
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}
