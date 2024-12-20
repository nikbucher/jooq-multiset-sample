package io.bucher.sample;

import static io.bucher.sample.Tables.SOME_ENTRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.Records.mapping;

import io.bucher.sample.domain.Day;
import io.bucher.sample.tables.records.SomeEntryRecord;
import java.time.LocalDate;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TxSpringBootTest {

	@Autowired
	DSLContext ctx;

	@BeforeEach
	void before() {
		ctx.startTransaction().execute();
	}

	@AfterEach
	void after() {
		ctx.rollback().execute();
	}

	@Test
	void create_changes_to_test_rollback() {
		assertThat(ctx.fetchCount(SOME_ENTRY)).isEqualTo(12);
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
				Day.from("2024-01-05")
		);

		assertThat(ctx.fetchCount(SOME_ENTRY)).isEqualTo(12);

		SomeEntryRecord record = ctx.newRecord(SOME_ENTRY);
		record.setDate(LocalDate.parse("2024-01-11"));
		record.setDescription("foo");
		record.setDuration(123L);
		record.store();

		assertThat(ctx.fetchCount(SOME_ENTRY)).isEqualTo(13);
	}

	@Test
	void other_test() {
		assertThat(ctx.fetchCount(SOME_ENTRY)).isEqualTo(12);
	}

	@Test
	void yet_another_test() {
		assertThat(ctx.fetchCount(SOME_ENTRY)).isEqualTo(12);
	}

}