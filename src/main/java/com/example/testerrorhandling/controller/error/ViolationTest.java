package com.example.testerrorhandling.controller.error;

import lombok.Data;
import org.zalando.problem.violations.Violation;
@Data
public class ViolationTest{

    private final String field;
    private final String message;
    private final String errorType;
    private final Object invalidValue;

}
