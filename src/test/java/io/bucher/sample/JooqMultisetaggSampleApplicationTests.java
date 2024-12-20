package io.bucher.sample;

import static io.bucher.sample.Tables.SOME_ENTRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.sum;

import io.bucher.sample.domain.Day;
import io.bucher.sample.domain.DaySummary;
import io.bucher.sample.domain.Entry;
import io.bucher.sample.tables.records.SomeEntryRecord;
import java.time.LocalDate;
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
		assertThat(ctx.fetchCount(SOME_ENTRY)).isEqualTo(12);
		ctx.transaction(txc -> {
			var txCtx = txc.dsl();
			try {
				var result = txCtx.select(SOME_ENTRY.DATE)
						.from(SOME_ENTRY)
						.groupBy(SOME_ENTRY.DATE)
						.orderBy(SOME_ENTRY.DATE)
						.fetch(mapping(Day::new));

				assertThat(result).containsExactly(
						Day.from("2024-01-01"),
						Day.from("2024-01-02"),
						Day.from("2024-01-03"),
						Day.from("2024-01-04"),
						Day.from("2024-01-05")
				);

				assertThat(txCtx.fetchCount(SOME_ENTRY)).isEqualTo(12);

				SomeEntryRecord record = txCtx.newRecord(SOME_ENTRY);
				record.setDate(LocalDate.parse("2024-01-11"));
				record.setDescription("foo");
				record.setDuration(123L);
				record.store();

				assertThat(txCtx.fetchCount(SOME_ENTRY)).isEqualTo(13);
			} finally {
				txCtx.rollback().execute();
			}
		});
		assertThat(ctx.fetchCount(SOME_ENTRY)).isEqualTo(12);
	}

	@Test
	void should_fetch_daily_entries() {
		var e = SOME_ENTRY.as("e");
		var daySummary = name("day_summary");

		var daySummaryCte = ctx.with(daySummary).as(
				select(
						e.DATE.as("date"),
						e.DESCRIPTION.as("description"),
						sum(e.DURATION).as("duration")
				).from(e)
						.groupBy(e.DATE, e.DESCRIPTION)
		);

		List<DaySummary> result = ctx.select(
						e.DATE,
						multiset(
								daySummaryCte.select(
												field("description", String.class),
												field("duration", Long.class)
										).from(daySummary)
										.where(field("day_summary.date", LocalDate.class).eq(e.DATE))
						).convertFrom(r -> r.map(mapping(Entry::new)))
				)
				.from(e)
				.groupBy(e.DATE)
				.orderBy(e.DATE)
				.fetch(mapping(DaySummary::new));

		assertThat(result).containsExactly(
				new DaySummary(LocalDate.parse("2024-01-01"),
						List.of(
								new Entry("Task A description", 90),
								new Entry("Task B description", 120),
								new Entry("Task C description", 180)
						)
				),
				new DaySummary(LocalDate.parse("2024-01-02"),
						List.of(
								new Entry("Task B description", 120),
								new Entry("Task C description", 150)
						)
				),
				new DaySummary(LocalDate.parse("2024-01-03"),
						List.of(
								new Entry("Task A description", 310),
								new Entry("Task C description", 90)
						)
				),
				new DaySummary(LocalDate.parse("2024-01-04"),
						List.of(
								new Entry("Task C description", 100)
						)
				),
				new DaySummary(LocalDate.parse("2024-01-05"),
						List.of(
								new Entry("Task B description", 390)
						)
				)
		);

	}

}
