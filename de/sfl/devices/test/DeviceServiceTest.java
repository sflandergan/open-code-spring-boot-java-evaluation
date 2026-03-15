package de.sfl.devices;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.*;

@SpringBootTest
class DeviceServiceTest {

	@MockBean
	private DeviceRepository deviceRepository;

	@Test
	void testSomeServiceMethod() {
		// Mock behavior
		when(deviceRepository.findById(1L)).thenReturn(new Device());

		// Call service method
		// Verify interactions
		verify(deviceRepository, times(1)).findById(1L);
	}
}
