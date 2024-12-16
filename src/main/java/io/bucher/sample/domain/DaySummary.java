package io.bucher.sample.domain;

import java.time.LocalDate;
import java.util.List;

public record DaySummary(
		LocalDate date,
		List<Entry> entries
) {
}
