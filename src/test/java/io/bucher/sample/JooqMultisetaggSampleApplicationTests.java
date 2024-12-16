package io.bucher.sample;

import static io.bucher.sample.Tables.SOME_ENTRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multisetAgg;
import static org.jooq.impl.DSL.sum;

import io.bucher.sample.domain.Day;
import io.bucher.sample.domain.DaySummary;
import io.bucher.sample.domain.Entry;
import java.util.List;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class JooqMultisetaggSampleApplicationTests {

	@Autowired
	DSLContext ctx;

	@Test
	void should_fetch_days() {

		var result = ctx.select(SOME_ENTRY.DATE)
				.from(SOME_ENTRY)
				.groupBy(SOME_ENTRY.DATE)
				.orderBy(SOME_ENTRY.DATE)
				.fetch(mapping(Day::new));

		assertThat(result).containsExactly(
				Day.from("2024-01-01"),
				Day.from("2024-01-02"),
				Day.from("2024-01-03"),
				Day.from("2024-01-04"),
				Day.from("2024-01-05"),
				Day.from("2024-01-06"),
				Day.from("2024-01-07")
		);
	}

	@Test
	void should_fetch_daily_entries() {
		var e = SOME_ENTRY.as("e");

		List<DaySummary> result = ctx.select(
						e.DATE,
						multisetAgg(
								e.DESCRIPTION,
								sum(e.DURATION).cast(long.class)
						).convertFrom(r -> r.map(mapping(Entry::new)))
				)
				.from(e)
				.groupBy(e.DATE)
				.orderBy(e.DATE)
				.fetch(mapping(DaySummary::new));

		assertThat(result).hasSize(7);

	}

}
