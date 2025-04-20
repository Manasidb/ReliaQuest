package com.reliaquest.api.controller;

import com.reliaquest.api.dto.CreateEmployeeRequest;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.service.EmployeeServiceImpl;
import com.reliaquest.api.service.IEmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/employee")
public class EmployeeControllerImpl implements IEmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private final IEmployeeService iEmployeeService;

    @Autowired
    public EmployeeControllerImpl(IEmployeeService iEmployeeService) {
        this.iEmployeeService = iEmployeeService;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(iEmployeeService.getAllEmployees());
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable("searchString") String employeeName) {
        logger.info("IEmployeeControllerImpl : getEmployeesByNameSearch() : employeeName : {}", employeeName);
        return ResponseEntity.ok(iEmployeeService.getEmployeesByNameSearch(employeeName));
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(@PathVariable(value = "id", required = true) String employeeId) {
        logger.info("IEmployeeControllerImpl : getEmployeeById() : employeeId : {}", employeeId);
        return ResponseEntity.ok(iEmployeeService.getEmployeeById(employeeId));
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return ResponseEntity.ok(iEmployeeService.getHighestSalaryOfEmployees());
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return ResponseEntity.ok(iEmployeeService.getTopTenHighestEarningEmployeeNames());
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody CreateEmployeeRequest createEmployeeRequest) {
        logger.info("IEmployeeControllerImpl : createEmployee() : createEmployeeRequest : {}", createEmployeeRequest);
        Employee createdEmployee = iEmployeeService.createEmployee(createEmployeeRequest);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@Valid @PathVariable(value = "id") String employeeId) {
        logger.info("IEmployeeControllerImpl : deleteEmployeeById() : employeeId : {}", employeeId);
        return ResponseEntity.ok(iEmployeeService.deleteEmployeeById(employeeId));
    }
}
