package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    @JsonProperty("id")
    String id;

    @JsonProperty("employee_name")
    String name;

    @JsonProperty("employee_salary")
    String salary;

    @JsonProperty("employee_age")
    String age;

    @JsonProperty("employee_title")
    String title;

    @JsonProperty("employee_email")
    String email;
}
