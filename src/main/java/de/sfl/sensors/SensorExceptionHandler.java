package de.sfl.sensors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice(assignableTypes = SensorController.class)
public class SensorExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(SensorExceptionHandler.class);

	@ExceptionHandler(SensorNotFoundException.class)
	public ProblemDetail handleSensorNotFoundException(SensorNotFoundException ex) {
		logger.warn("Sensor not found: {}", ex.getMessage());
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problemDetail.setTitle("Sensor Not Found");
		return problemDetail;
	}

	@ExceptionHandler(SensorAlreadyExistsException.class)
	public ProblemDetail handleSensorAlreadyExistsException(SensorAlreadyExistsException ex) {
		logger.warn("Sensor already exists: {}", ex.getMessage());
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problemDetail.setTitle("Sensor Already Exists");
		return problemDetail;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
		String errors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining(", "));

		logger.warn("Validation failed: {}", errors);
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errors);
		problemDetail.setTitle("Validation Failed");
		return problemDetail;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleGenericException(Exception ex) {
		logger.error("Unexpected error occurred", ex);
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"An unexpected error occurred"
		);
		problemDetail.setTitle("Internal Server Error");
		return problemDetail;
	}
}
