package de.sfl.devices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @BeforeEach
    void setUp() {
        // Default mock behavior for validation tests
        when(deviceService.createDevice(any(CreateDeviceDto.class))).thenReturn(null);
    }

    @Test
    void createDevice_shouldReturn201Created() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var createDeviceDto = new CreateDeviceDto("New Device", "Device Description");
        var device = new Device("New Device", "Device Description");
        device.id = deviceId;

        when(deviceService.createDevice(createDeviceDto)).thenReturn(device);

        // When/Then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDeviceDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(deviceId.toString())))
                .andExpect(jsonPath("$.name", is("New Device")))
                .andExpect(jsonPath("$.description", is("Device Description")));

        verify(deviceService).createDevice(createDeviceDto);
    }

    @Test
    void createDevice_shouldReturn400BadRequest_whenNameIsBlank() throws Exception {
        // Given
        var createDeviceDto = new CreateDeviceDto("  ", "Device Description");

        // When/Then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDeviceDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDevice_shouldReturn400BadRequest_whenDescriptionIsBlank() throws Exception {
        // Given
        var createDeviceDto = new CreateDeviceDto("New Device", "  ");

        // When/Then
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDeviceDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateDevice_shouldReturn200Ok() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var updateDeviceDto = new UpdateDeviceDto("Updated Device", "Updated Description");
        var device = new Device("Original Device", "Original Description");
        device.id = deviceId;
        var deviceDto = DeviceDto.fromEntityWithoutSensors(device);

        when(deviceService.updateDevice(eq(deviceId), any(UpdateDeviceDto.class))).thenReturn(device);

        // When/Then
        mockMvc.perform(put("/api/devices/{deviceId}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDeviceDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deviceId.toString())))
                .andExpect(jsonPath("$.name", is("Original Device")))
                .andExpect(jsonPath("$.description", is("Original Description")));

        verify(deviceService).updateDevice(eq(deviceId), any(UpdateDeviceDto.class));
    }

    @Test
    void updateDevice_shouldReturn404NotFound_whenDeviceDoesNotExist() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var updateDeviceDto = new UpdateDeviceDto("Updated Device", "Updated Description");

        when(deviceService.updateDevice(eq(deviceId), any(UpdateDeviceDto.class)))
                .thenThrow(new DeviceNotFoundException(deviceId));

        // When/Then
        mockMvc.perform(put("/api/devices/{deviceId}", deviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDeviceDto)))
                .andExpect(status().isNotFound());

        verify(deviceService).updateDevice(eq(deviceId), any(UpdateDeviceDto.class));
    }

    @Test
    void deleteDevice_shouldReturn204NoContent() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();

        // When/Then
        mockMvc.perform(delete("/api/devices/{deviceId}", deviceId))
                .andExpect(status().isNoContent());

        verify(deviceService).deleteDevice(deviceId);
    }

    @Test
    void deleteDevice_shouldReturn404NotFound_whenDeviceDoesNotExist() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();

        doThrow(new DeviceNotFoundException(deviceId))
                .when(deviceService).deleteDevice(deviceId);

        // When/Then
        mockMvc.perform(delete("/api/devices/{deviceId}", deviceId))
                .andExpect(status().isNotFound());

        verify(deviceService).deleteDevice(deviceId);
    }

    @Test
    void assignSensor_shouldReturn200Ok() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorId = 123L;
        var device = new Device("Test Device", "Test Description");
        device.id = deviceId;
        var deviceDto = DeviceDto.fromEntityWithoutSensors(device);

        when(deviceService.assignSensor(deviceId, sensorId)).thenReturn(device);

        // When/Then
        mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deviceId.toString())))
                .andExpect(jsonPath("$.name", is("Test Device")));

        verify(deviceService).assignSensor(deviceId, sensorId);
    }

    @Test
    void assignSensor_shouldReturn404NotFound_whenDeviceDoesNotExist() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var sensorId = 123L;

        when(deviceService.assignSensor(deviceId, sensorId))
                .thenThrow(new DeviceNotFoundException(deviceId));

        // When/Then
        mockMvc.perform(put("/api/devices/{deviceId}/sensors/{sensorId}", deviceId, sensorId))
                .andExpect(status().isNotFound());

        verify(deviceService).assignSensor(deviceId, sensorId);
    }

    @Test
    void getDevice_shouldReturn200Ok() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();
        var device = new Device("Test Device", "Test Description");
        device.id = deviceId;
        var deviceDto = DeviceDto.fromEntityWithoutSensors(device);

        when(deviceService.getDevice(deviceId)).thenReturn(device);

        // When/Then
        mockMvc.perform(get("/api/devices/{deviceId}", deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(deviceId.toString())))
                .andExpect(jsonPath("$.name", is("Test Device")))
                .andExpect(jsonPath("$.description", is("Test Description")));

        verify(deviceService).getDevice(deviceId);
    }

    @Test
    void getDevice_shouldReturn404NotFound_whenDeviceDoesNotExist() throws Exception {
        // Given
        var deviceId = UUID.randomUUID();

        when(deviceService.getDevice(deviceId))
                .thenThrow(new DeviceNotFoundException(deviceId));

        // When/Then
        mockMvc.perform(get("/api/devices/{deviceId}", deviceId))
                .andExpect(status().isNotFound());

        verify(deviceService).getDevice(deviceId);
    }
}