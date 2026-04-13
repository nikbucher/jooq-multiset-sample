# jOOQ Multiset Sample

Demonstrates jOOQ's [`multiset()`](https://www.jooq.org/doc/latest/manual/sql-building/column-expressions/multiset-value-constructor/) for mapping nested SQL results into Java records — no N+1 queries.

```java
List<DaySummary> result = ctx.select(
        e.DATE,
        multiset(
                daySummaryCte.select(
                        field("description", String.class),
                        field("duration", Long.class)).from(daySummary)
                        .where(field("day_summary.date", LocalDate.class).eq(e.DATE)))
                .convertFrom(r -> r.map(mapping(Entry::new))))
        .from(e)
        .groupBy(e.DATE)
        .orderBy(e.DATE)
        .fetch(mapping(DaySummary::new));
```

See [`JooqMultisetaggSampleApplicationTests`](src/test/java/io/bucher/sample/JooqMultisetaggSampleApplicationTests.java) for the full example.

## Tech Stack

- Spring Boot
- jOOQ
- PostgreSQL
- Flyway
- Testcontainers

## Prerequisites

Java, Maven, Docker

## Build & Test

```bash
./mvnw verify
```

## License

[Unlicense](LICENSE) — public domain.
