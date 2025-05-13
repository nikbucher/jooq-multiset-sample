package io.bucher.sample;

import static io.bucher.sample.Tables.SOME_ENTRY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.Records.mapping;

import io.bucher.sample.domain.Day;
import io.bucher.sample.tables.records.SomeEntryRecord;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TxTest extends JooqIntegTest {

	@Test
	void create_changes_to_test_rollback() {
		assertThat(ctx().fetchCount(SOME_ENTRY)).isEqualTo(12);
		var result = ctx().selectDistinct(SOME_ENTRY.DATE)
				.from(SOME_ENTRY)
				.orderBy(SOME_ENTRY.DATE)
				.fetch(mapping(Day::new));

		assertThat(result).containsExactly(
				Day.from("2024-01-01"),
				Day.from("2024-01-02"),
				Day.from("2024-01-03"),
				Day.from("2024-01-04"),
				Day.from("2024-01-05")
		);

		assertThat(ctx().fetchCount(SOME_ENTRY)).isEqualTo(12);

		SomeEntryRecord record = ctx().newRecord(SOME_ENTRY);
		record.setDate(LocalDate.parse("2024-01-11"));
		record.setDescription("foo");
		record.setDuration(123L);
		record.store();

		assertThat(ctx().fetchCount(SOME_ENTRY)).isEqualTo(13);
	}

	@Test
	void other_test() {
//		var someEntryRecord = new SomeEntryDao(ctx().configuration()).fetchOneById(1L);
		var someEntryRecord = ctx().selectFrom(SOME_ENTRY)
				.where(SOME_ENTRY.getIdentity().getField().eq(1000L))
				.fetchOne();
		someEntryRecord.setDescription("foo");
		someEntryRecord.store();
		assertThat(ctx().fetchCount(SOME_ENTRY)).isEqualTo(12);
	}

	@Test
	void yet_another_test() {
		assertThat(ctx().fetchCount(SOME_ENTRY)).isEqualTo(12);
	}

}
