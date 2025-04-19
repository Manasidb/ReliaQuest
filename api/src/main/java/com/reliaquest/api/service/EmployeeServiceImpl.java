package com.reliaquest.api.service;

import com.reliaquest.api.dto.*;
import com.reliaquest.api.exception.CustomRuntimeException;
import com.reliaquest.api.exception.EmployeeAlreadyExistsException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class EmployeeServiceImpl implements IEmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final RestTemplate restTemplate;

    @Value("${employee.api.url}")
    private String MOCK_EMPLOYEE_API_URL;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    public EmployeeServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "employeeService", fallbackMethod = "fallbackGetEmployees")
    @Retry(name = "employeeService", fallbackMethod = "fallbackGetEmployees")
    @Override
    public List<Employee> getAllEmployees() {
        ResponseEntity<EmployeeResponseWrapper> response = fetchEmployeeData();
        return response.getBody().getData();
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String employeeName) {

        try {
            List<Employee> employees = searchEmployeesByName(employeeName);
            if (employees.isEmpty()) {
                logger.error("getEmployeesByNameSearch() response: Employee with name {} not found", employeeName);
                throw new EmployeeNotFoundException("Employee with name " + employeeName + " not found.");
            }
            return employees;
        } catch (EmployeeNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("getEmployeesByNameSearch() : Unexpected error occured while fetching employee by name : "
                    + ex.getMessage());
            throw new CustomRuntimeException(
                    "Unexpected error occured while fetching employee by name " + ex.getCause());
        }
    }

    @Cacheable(value = "employeeById", key = "#employeeId")
    @Override
    public Employee getEmployeeById(String employeeId) {
        String url = MOCK_EMPLOYEE_API_URL + "/" + employeeId;
        try {
            ResponseEntity<EmployeeResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, null, EmployeeResponse.class);

            Employee employee = response.getBody().getData();
            return employee;
        } catch (HttpClientErrorException ex) {
            // Check if it's a 404 Not Found error
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.error("getEmployeeById() response: Employee with employeeId {} not found", employeeId);
                throw new EmployeeNotFoundException("Employee with id " + employeeId + " not found.");
            }
            // For other HttpClientErrorExceptions, log the status and rethrow
            logger.error(
                    "getEmployeeById() response: HTTP error occurred with status {} for employeeId {}",
                    ex.getStatusCode(),
                    employeeId);
            throw new CustomRuntimeException(
                    "HTTP error occurred while fetching employee by id: " + ex.getStatusCode());

        } catch (Exception ex) {
            logger.error(
                    "getEmployeeById() : Unexpected error occurred while fetching employee by id : " + ex.getMessage());
            throw new CustomRuntimeException(
                    "Unexpected error occurred while fetching employee by id: " + ex.getCause());
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
            logger.error("getHighestSalaryOfEmployees() : Unexpected error occured while fetching highest salary : "
                    + ex.getMessage());
            throw new CustomRuntimeException("Unexpected error occured while fetching highest salary " + ex.getCause());
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
            logger.error(
                    "getTopTenHighestEarningEmployeeNames() : Unexpected error occured while fetching top ten highest earning employee : "
                            + ex.getMessage());
            throw new CustomRuntimeException(
                    "Unexpected error occured while fetching top ten highest earning employee " + ex.getCause());
        }
    }

    @Override
    public Employee createEmployee(CreateEmployeeRequest createEmployeeRequest) {
        try {
            if (!searchEmployeesByName(createEmployeeRequest.getName()).isEmpty()) {
                logger.error(
                        "createEmployee() : Employee with name " + createEmployeeRequest.getName() + " already exists");
                throw new EmployeeAlreadyExistsException(
                        "Employee with name " + createEmployeeRequest.getName() + " already exists");
            }

            HttpEntity<CreateEmployeeRequest> requestEntity = new HttpEntity<>(createEmployeeRequest, getHeader());
            ResponseEntity<EmployeeResponse> response =
                    restTemplate.postForEntity(MOCK_EMPLOYEE_API_URL, requestEntity, EmployeeResponse.class);

            logger.debug("createEmployee() createEmployeeResponse : " + response);
            return response.getBody().getData();

        } catch (EmployeeAlreadyExistsException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("createEmployee() : Unexpected error during employee creation: " + ex.getMessage());
            throw new CustomRuntimeException("Unexpected error during employee creation: " + ex.getCause());
        }
    }

    @CacheEvict(value = "employeeById", key = "#employeeId")
    @Override
    public String deleteEmployeeById(String employeeId) {
        try {
            String employeeName = getEmployeeById(employeeId).getName();
            DeleteEmployeeRequest deleteEmployeeRequest = new DeleteEmployeeRequest();
            deleteEmployeeRequest.setName(employeeName);

            HttpEntity<DeleteEmployeeRequest> requestEntity = new HttpEntity<>(deleteEmployeeRequest, getHeader());

            ResponseEntity<DeleteEmployeeResponse> response = restTemplate.exchange(
                    MOCK_EMPLOYEE_API_URL,
                    HttpMethod.DELETE,
                    requestEntity,
                    DeleteEmployeeResponse.class,
                    employeeName);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody().isData()) {
                return "Employee with id " + employeeId + " deleted successfully";
            }
            throw new CustomRuntimeException("Failed to delete employee with id " + employeeId);
        } catch (EmployeeNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("deleteEmployeeById() : Unexpected error occured while deleting employee by id : "
                    + ex.getMessage());
            throw new CustomRuntimeException("Unexpected error occured while deleting employee by id " + ex.getCause());
        }
    }

    public Employee getFromCache(String employeeId) {
        Cache cache = cacheManager.getCache("employeeById");
        if (cache != null) {
            return cache.get(employeeId, Employee.class);
        }
        return null;
    }

    private ResponseEntity<EmployeeResponseWrapper> fetchEmployeeData() {
        ResponseEntity<EmployeeResponseWrapper> response =
                restTemplate.exchange(MOCK_EMPLOYEE_API_URL, HttpMethod.GET, null, EmployeeResponseWrapper.class);
        return response;
    }

    private HttpHeaders getHeader() {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        return header;
    }

    private List<Employee> searchEmployeesByName(String employeeName) {
        try {
            ResponseEntity<EmployeeResponseWrapper> response = fetchEmployeeData();

            String searchName = employeeName.trim().toLowerCase();
            return response.getBody().getData().stream()
                    .filter(emp ->
                            emp.getName() != null && emp.getName().toLowerCase().contains(searchName))
                    .toList();
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    /**
     * @return we can return cached/default data in fallbackmethod
     */
    public List<Employee> fallbackGetEmployees(Throwable t) {
        logger.warn("Fallback triggered due to: {}", t.toString());
        return List.of();
    }
}
