package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEmployeeRequest {

    @JsonProperty("name")
    @NotBlank(message = "Name should not be blank")
    String name;

    @JsonProperty("salary")
    @NotNull(message = "Salary should not be null") @Min(value = 1, message = "Salary should be greater than zero")
    String salary;

    @JsonProperty("age")
    @NotNull(message = "Salary must not be null") @Min(value = 16, message = "Minimum age should be 16")
    @Max(value = 75, message = "Maximum age should be 75")
    String age;

    @NotBlank(message = "Title should not be blank")
    @JsonProperty("title")
    String title;

    @JsonProperty("email")
    @Email(message = "Email should be valid")
    String email;
}
