package io.bucher.sample.domain;

import java.time.LocalDate;

public record Day(LocalDate date) {

	public static Day from(String date) {
		var parsedDate = LocalDate.parse(date);
		return new Day(parsedDate);
	}
}
