package com.example.testerrorhandling.controller.error;

import org.hibernate.validator.engine.HibernateConstraintViolation;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.*;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.violations.ConstraintViolationProblem;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.metadata.ConstraintDescriptor;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807).
 */
@ControllerAdvice
public class ExceptionTranslator implements ProblemHandling{
    private static final String MESSAGE_KEY = "message";
    private static final String PATH_KEY = "path";
    private static final String VIOLATIONS_KEY = "violations";
    public static final String DOT = "\\.";

    /**
     * Post-process the Problem payload to add the message key for the front-end if needed.
     */
    @Override
    public ResponseEntity<Problem> process(@Nullable ResponseEntity<Problem> entity, NativeWebRequest request) {
        if (entity == null) {
            return entity;
        }
        Problem problem = entity.getBody();
        if (!(problem instanceof ConstraintViolationProblem || problem instanceof DefaultProblem)) {
            return entity;
        }
        ProblemBuilder builder = Problem.builder()
                .withType(Problem.DEFAULT_TYPE.equals(problem.getType()) ? ErrorConstants.DEFAULT_TYPE : problem.getType())
                .withStatus(problem.getStatus())
                .withTitle(problem.getTitle())
                .with(PATH_KEY, request.getNativeRequest(HttpServletRequest.class).getRequestURI());

        if (problem instanceof ConstraintViolationProblem) {
            builder
                    .with(VIOLATIONS_KEY, ((ConstraintViolationProblem) problem).getViolations())
                    .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION);
        } else {
            builder
                    .withCause(((DefaultProblem) problem).getCause())
                    .withDetail(problem.getDetail())
                    .withInstance(problem.getInstance());
            problem.getParameters().forEach(builder::with);
            if (!problem.getParameters().containsKey(MESSAGE_KEY) && problem.getStatus() != null) {
                builder.with(MESSAGE_KEY, "error.http." + problem.getStatus().getStatusCode());
            }
        }
        return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
    }


    @ExceptionHandler
    public ResponseEntity<Problem> handleNoSuchElementException(NoSuchElementException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
                .withStatus(Status.NOT_FOUND)
                .with(MESSAGE_KEY, ErrorConstants.ENTITY_NOT_FOUND_TYPE)
                .build();
        return create(ex, problem, request);
    }

    @Override
    public ResponseEntity<Problem> handleConstraintViolation(ConstraintViolationException exception, NativeWebRequest request) {
        final List<ViolationTest> violations = exception.getConstraintViolations().stream()
                .map(this::buildViolation)
                .collect(toList());

        return buildConstraintViolationProblem(exception, violations, request);
    }

    @Override
    public ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, NativeWebRequest request) {
        final List<ViolationTest> violations = buildViolations(exception.getBindingResult());
        return buildConstraintViolationProblem(exception, violations, request);
    }

    public ViolationTest buildViolation(final ConstraintViolation violation) {
        return new ViolationTest(formatFieldName(violation.getPropertyPath().toString()), violation.getMessage(),
                buildErrorType(violation.getConstraintDescriptor()), buildInvalidValue(violation));
    }

    private String buildErrorType(ConstraintDescriptor constraintDescriptor) {
        return constraintDescriptor.getAttributes().containsKey("errorType") ?
                (String) constraintDescriptor.getAttributes().get("errorType") :
                constraintDescriptor.getAnnotation().annotationType().getSimpleName();
    }
    private Object buildInvalidValue(ConstraintViolation violation) {
        var hv = (HibernateConstraintViolation) violation.unwrap(HibernateConstraintViolation.class);
        Object  invalidValue = hv.getDynamicPayload(Object.class);
        return invalidValue !=null? invalidValue : violation.getInvalidValue();
    }
    public ViolationTest buildViolation(final FieldError fieldError) {
        var violation = fieldError.unwrap(ConstraintViolation.class);
        return buildViolation(violation);
    }
    public ViolationTest buildViolation(final ObjectError objectError) {
        var violation = objectError.unwrap(ConstraintViolation.class);
        return buildViolation(violation);
    }
    public List<ViolationTest> buildViolations(final BindingResult result) {
        final Stream<ViolationTest> fieldErrors = result.getFieldErrors().stream().map(this::buildViolation);
        final Stream<ViolationTest> globalErrors = result.getGlobalErrors().stream().map(this::buildViolation);
        return Stream.concat(fieldErrors, globalErrors).collect(toList());
    }

    @Override
    public String formatFieldName(String fieldName) {
        if (fieldName.contains(".") ) {
            String[] parts =  fieldName.split(DOT);
            return parts[parts.length-1];
        }
        return fieldName;
    }

    public ResponseEntity<Problem> buildConstraintViolationProblem(Throwable throwable, Collection<ViolationTest> stream, NativeWebRequest request) {

        final URI type = defaultConstraintViolationType();
        final StatusType status = defaultConstraintViolationStatus();

        final List<ViolationTest> violations = stream.stream()
                // sorting to make tests deterministic
                .sorted(comparing(ViolationTest::getField).thenComparing(ViolationTest::getMessage))
                .collect(toList());

        final Problem problem = new ConstraintViolationProblemCustom(type, status, violations);

        return create(throwable, problem, request);
    }
}