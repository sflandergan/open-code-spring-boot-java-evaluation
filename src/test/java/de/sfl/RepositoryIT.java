package de.sfl;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class RepositoryIT {

	@ServiceConnection
	private static final PostgreSQLContainer postgres = new PostgreSQLContainer(
		DockerImageName.parse("postgres:17-alpine"))
		.withReuse(true);

	static {
		postgres.start();
	}
}
