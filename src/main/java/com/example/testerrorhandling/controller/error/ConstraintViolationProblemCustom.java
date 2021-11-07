package com.example.testerrorhandling.controller.error;

import org.zalando.problem.StatusType;
import org.zalando.problem.ThrowableProblem;
import org.zalando.problem.violations.Violation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * custom ConstraintViolation pour pouvoir ajouter une custom list de ViolationTest
 */
public class ConstraintViolationProblemCustom extends ThrowableProblem {

    public static final String TYPE_VALUE = "https://zalando.github.io/problem/constraint-violation";
    public static final URI TYPE = URI.create(TYPE_VALUE);

    private final URI type;
    private final StatusType status;
    private final List<ViolationTest> violations;

    public ConstraintViolationProblemCustom(final StatusType status, final List<ViolationTest> violations) {
        this(TYPE, status, violations != null ? new ArrayList<>(violations) : new ArrayList<>());
    }

    public ConstraintViolationProblemCustom(final URI type, final StatusType status, final List<ViolationTest> violations) {
        this.type = type;
        this.status = status;
        this.violations = violations != null ? Collections.unmodifiableList(violations) : Collections.emptyList();
    }

    @Override
    public URI getType() {
        return type;
    }

    @Override
    public String getTitle() {
        return "Constraint Violation";
    }

    @Override
    public StatusType getStatus() {
        return status;
    }

    public List<ViolationTest> getViolations() {
        return violations;
    }
}
