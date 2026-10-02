package com.jmix.report;

import com.jmix.exception.InvalidReportRequestException;
import java.time.Clock;
import java.time.LocalDate;

public record ReportPeriod(LocalDate from, LocalDate to) {

    public static ReportPeriod resolve(LocalDate requestedFrom, LocalDate requestedTo, Clock clock) {
        LocalDate to = requestedTo == null ? LocalDate.now(clock) : requestedTo;
        LocalDate from = requestedFrom == null ? to.minusMonths(11).withDayOfMonth(1) : requestedFrom;
        if (from.isAfter(to)) {
            throw new InvalidReportRequestException("The 'from' date must be on or before the 'to' date.");
        }
        return new ReportPeriod(from, to);
    }
}
