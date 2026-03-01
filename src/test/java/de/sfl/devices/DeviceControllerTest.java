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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
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
        // Given
        var createDto = new CreateDeviceDto("Device-001", "Test device");
        var deviceId = UUID.randomUUID();
        var createdDevice = new TestDevice(deviceId, "Device-001", "Test device");

        when(deviceService.createDevice(any(CreateDeviceDto.class))).thenReturn(createdDevice);

        // When/Then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(deviceId.toString())))
                .andExpect(jsonPath("$.name", is("Device-001")))
                .andExpect(jsonPath("$.description", is("Test device")));

        verify(deviceService).createDevice(any(CreateDeviceDto.class));
    }

    @Test
    void createDevice_shouldReturn400WhenNameIsBlank() throws Exception {
        // Given
        var invalidDto = new CreateDeviceDto("", "Test device");

        // When/Then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDevice_shouldReturn400WhenDescriptionIsBlank() throws Exception {
        // Given
        var invalidDto = new CreateDeviceDto("Device-001", "");

        // When/Then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDevice_shouldReturnUpdatedDevice() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var updateDto = new UpdateDeviceDto("Updated", "Updated description");
        var updatedDevice = new TestDevice(deviceId, "Updated", "Updated description");

        when(deviceService.updateDevice(deviceId, updateDto)).thenReturn(updatedDevice);

        // When/Then
        mockMvc.perform(put("/api/devices/" + deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deviceId.toString())))
                .andExpect(jsonPath("$.name", is("Updated")))
                .andExpect(jsonPath("$.description", is("Updated description")));

        verify(deviceService).updateDevice(deviceId, updateDto);
    }

    @Test
    void updateDevice_shouldReturn404WhenDeviceNotFound() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var updateDto = new UpdateDeviceDto("Updated", "Updated description");

        when(deviceService.updateDevice(deviceId, updateDto))
                .thenThrow(new DeviceNotFoundException(deviceId));

        // When/Then
        mockMvc.perform(put("/api/devices/" + deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(deviceService).updateDevice(deviceId, updateDto);
    }

    @Test
    void deleteDevice_shouldReturn204() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();

        // When/Then
        mockMvc.perform(delete("/api/devices/" + deviceId))
                .andExpect(status().isNoContent());

        verify(deviceService).deleteDevice(deviceId);
    }

    @Test
    void deleteDevice_shouldReturn404WhenDeviceNotFound() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();

        doThrow(new DeviceNotFoundException(deviceId))
                .when(deviceService).deleteDevice(deviceId);

        // When/Then
        mockMvc.perform(delete("/api/devices/" + deviceId))
                .andExpect(status().isNotFound());

        verify(deviceService).deleteDevice(deviceId);
    }

    @Test
    void assignSensor_shouldReturnDeviceWithSensorAssigned() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorId = 1L;
        var device = new TestDevice(deviceId, "Device-001", "Test device");

        when(deviceService.assignSensor(deviceId, sensorId)).thenReturn(device);

        // When/Then
        mockMvc.perform(put("/api/devices/" + deviceId + "/sensors/" + sensorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deviceId.toString())))
                .andExpect(jsonPath("$.name", is("Device-001")));

        verify(deviceService).assignSensor(deviceId, sensorId);
    }

    @Test
    void assignSensor_shouldReturn404WhenDeviceNotFound() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorId = 1L;

        when(deviceService.assignSensor(deviceId, sensorId))
                .thenThrow(new DeviceNotFoundException(deviceId));

        // When/Then
        mockMvc.perform(put("/api/devices/" + deviceId + "/sensors/" + sensorId))
                .andExpect(status().isNotFound());

        verify(deviceService).assignSensor(deviceId, sensorId);
    }

    @Test
    void getDevice_shouldReturnDevice() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var device = new TestDevice(deviceId, "Device-001", "Test device");

        when(deviceService.getDevice(deviceId)).thenReturn(device);

        // When/Then
        mockMvc.perform(get("/api/devices/" + deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deviceId.toString())))
                .andExpect(jsonPath("$.name", is("Device-001")))
                .andExpect(jsonPath("$.description", is("Test device")));

        verify(deviceService).getDevice(deviceId);
    }

    @Test
    void getDevice_shouldReturn404WhenDeviceNotFound() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();

        when(deviceService.getDevice(deviceId))
                .thenThrow(new DeviceNotFoundException(deviceId));

        // When/Then
        mockMvc.perform(get("/api/devices/" + deviceId))
                .andExpect(status().isNotFound());

        verify(deviceService).getDevice(deviceId);
    }

    static class TestDevice extends Device {
        TestDevice(UUID id, String name, String description) {
            super(name, description);
            this.id = id;
        }
    }
}
