package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceService deviceService;

    @Test
    void createDevice_shouldReturnCreatedDevice() throws Exception {
        var createDto = new CreateDeviceDto("Test Device", "Test Description");
        var deviceId = UUID.randomUUID();
        var device = createTestDevice(deviceId, "Test Device", "Test Description");

        when(deviceService.createDevice(any(CreateDeviceDto.class))).thenReturn(device);

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(deviceId.toString())))
                .andExpect(jsonPath("$.name", is("Test Device")))
                .andExpect(jsonPath("$.description", is("Test Description")));

        verify(deviceService).createDevice(any(CreateDeviceDto.class));
    }

    @Test
    void createDevice_shouldReturn400WhenNameIsBlank() throws Exception {
        var invalidDto = new CreateDeviceDto("", "Test Description");

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDevice_shouldReturn400WhenDescriptionIsBlank() throws Exception {
        var invalidDto = new CreateDeviceDto("Test Device", "");

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDevice_shouldReturnDevice() throws Exception {
        var deviceId = UUID.randomUUID();
        var device = createTestDevice(deviceId, "Test Device", "Test Description");

        when(deviceService.getDevice(deviceId)).thenReturn(device);

        mockMvc.perform(get("/api/devices/{id}", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deviceId.toString())))
                .andExpect(jsonPath("$.name", is("Test Device")));

        verify(deviceService).getDevice(deviceId);
    }

    @Test
    void getDevice_shouldReturn404WhenNotFound() throws Exception {
        var deviceId = UUID.randomUUID();
        when(deviceService.getDevice(deviceId)).thenThrow(new DeviceNotFoundException(deviceId));

        mockMvc.perform(get("/api/devices/{id}", deviceId))
                .andExpect(status().isNotFound());

        verify(deviceService).getDevice(deviceId);
    }

    @Test
    void updateDevice_shouldReturnUpdatedDevice() throws Exception {
        var deviceId = UUID.randomUUID();
        var updateDto = new UpdateDeviceDto("Updated Name", "Updated Description");
        var device = createTestDevice(deviceId, "Updated Name", "Updated Description");

        when(deviceService.updateDevice(any(UUID.class), any(UpdateDeviceDto.class))).thenReturn(device);

        mockMvc.perform(put("/api/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deviceId.toString())))
                .andExpect(jsonPath("$.name", is("Updated Name")));

        verify(deviceService).updateDevice(any(UUID.class), any(UpdateDeviceDto.class));
    }

    @Test
    void updateDevice_shouldReturn404WhenNotFound() throws Exception {
        var deviceId = UUID.randomUUID();
        var updateDto = new UpdateDeviceDto("Updated Name", "Updated Description");
        when(deviceService.updateDevice(any(UUID.class), any(UpdateDeviceDto.class)))
                .thenThrow(new DeviceNotFoundException(deviceId));

        mockMvc.perform(put("/api/devices/{id}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDevice_shouldReturn204() throws Exception {
        var deviceId = UUID.randomUUID();

        mockMvc.perform(delete("/api/devices/{id}", deviceId))
                .andExpect(status().isNoContent());

        verify(deviceService).deleteDevice(deviceId);
    }

    @Test
    void deleteDevice_shouldReturn404WhenNotFound() throws Exception {
        var deviceId = UUID.randomUUID();
        doThrow(new DeviceNotFoundException(deviceId)).when(deviceService).deleteDevice(deviceId);

        mockMvc.perform(delete("/api/devices/{id}", deviceId))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignSensor_shouldReturnDevice() throws Exception {
        var deviceId = UUID.randomUUID();
        var sensorId = 1L;
        var device = createTestDevice(deviceId, "Test Device", "Test Description");

        when(deviceService.assignSensor(deviceId, sensorId)).thenReturn(device);

        mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deviceId.toString())));

        verify(deviceService).assignSensor(deviceId, sensorId);
    }

    @Test
    void assignSensor_shouldReturn404WhenDeviceNotFound() throws Exception {
        var deviceId = UUID.randomUUID();
        var sensorId = 1L;
        when(deviceService.assignSensor(deviceId, sensorId)).thenThrow(new DeviceNotFoundException(deviceId));

        mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
                .andExpect(status().isNotFound());
    }

    private Device createTestDevice(UUID id, String name, String description) {
        return new TestDevice(id, name, description);
    }

    static class TestDevice extends Device {
        TestDevice(UUID id, String name, String description) {
            super(name, description);
            try {
                var idField = Device.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(this, id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
