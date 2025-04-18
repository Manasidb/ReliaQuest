package com.reliaquest.api.controller;

import com.reliaquest.api.dto.CreateEmployeeRequest;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.service.IEmployeeService;
import com.reliaquest.api.service.IEmployeeServiceImpl;
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
public class IEmployeeControllerImpl implements IEmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(IEmployeeServiceImpl.class);
    private final IEmployeeService iEmployeeService;

    @Autowired
    public IEmployeeControllerImpl(IEmployeeService iEmployeeService) {
        this.iEmployeeService = iEmployeeService;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return new ResponseEntity<>(iEmployeeService.getAllEmployees(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable("searchString") String employeeName) {
        logger.info("IEmployeeControllerImpl : getEmployeesByNameSearch() : employeeName : {}", employeeName);
        return new ResponseEntity<>(iEmployeeService.getEmployeesByNameSearch(employeeName), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") String employeeId) {
        logger.info("IEmployeeControllerImpl : getEmployeeById() : employeeId : {}", employeeId);
        return new ResponseEntity<>(iEmployeeService.getEmployeeById(employeeId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        return new ResponseEntity<>(iEmployeeService.getHighestSalaryOfEmployees(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        return new ResponseEntity<>(iEmployeeService.getTopTenHighestEarningEmployeeNames(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody CreateEmployeeRequest createEmployeeRequest) {
        logger.info("IEmployeeControllerImpl : createEmployee() : createEmployeeRequest : {}", createEmployeeRequest);
        return new ResponseEntity<>(iEmployeeService.createEmployee(createEmployeeRequest), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable("id") String employeeId) {
        logger.info("IEmployeeControllerImpl : deleteEmployeeById() : employeeId : {}", employeeId);
        return new ResponseEntity<>(iEmployeeService.deleteEmployeeById(employeeId), HttpStatus.CREATED);
    }
}
