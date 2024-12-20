package io.bucher.sample;

import java.sql.Connection;
import java.sql.DriverManager;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;

class JooqIntegTest {

	private static final ThreadLocal<PostgreSQLContainer<?>> TL_POSTGRES = new ThreadLocal<>();
	private static final ThreadLocal<Connection> TL_DB_CONNECTION = new ThreadLocal<>();
	private static final ThreadLocal<DSLContext> TL_CTX = new ThreadLocal<>();

	public DSLContext ctx() {
		var ctx = TL_CTX.get();
		if (ctx == null) {
			ctx = initializeDslCtx();
		}
		return ctx;
	}

	private DSLContext initializeDslCtx() {
		var pg = new PostgreSQLContainer<>("postgres:latest");
		TL_POSTGRES.set(pg);
		pg.start();

		var jdbcUrl = pg.getJdbcUrl();
		var username = pg.getUsername();
		var password = pg.getPassword();

		Flyway.configure()
				.dataSource(jdbcUrl, username, password)
				.load()
				.migrate();

		var dbConnection = catching(() -> DriverManager.getConnection(jdbcUrl, username, password));
		TL_DB_CONNECTION.set(dbConnection);

		var ctx = DSL.using(dbConnection, SQLDialect.POSTGRES);
		TL_CTX.set(ctx);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			closeSilently(dbConnection);
			pg.stop();
		}));
		return ctx;
	}

	@BeforeEach
	void before() {
		ctx().startTransaction().execute();
	}

	@AfterEach
	void after() {
		ctx().rollback().execute();
	}

	private <T> T catching(CatchingSupplier<T> supplier) {
		try {
			return supplier.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void closeSilently(AutoCloseable closeable) {
		try {
			if (closeable != null) closeable.close();
		} catch (Exception ignored) {
		}
	}

	private interface CatchingSupplier<T> {
		T get() throws Exception;
	}

}
