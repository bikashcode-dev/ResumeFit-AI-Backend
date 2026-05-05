package com.resumefit.exception;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        String message,
        List<String> errors
) {
}
