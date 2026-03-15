package de.sfl.devices.service;

import de.sfl.devices.model.Device;
import de.sfl.devices.dto.DeviceDto;
import de.sfl.devices.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;

    public List<DeviceDto> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public DeviceDto getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    public DeviceDto createDevice(DeviceDto deviceDto) {
        Device device = convertToEntity(deviceDto);
        Device savedDevice = deviceRepository.save(device);
        return convertToDto(savedDevice);
    }

    public DeviceDto updateDevice(Long id, DeviceDto deviceDto) {
        if (deviceRepository.existsById(id)) {
            deviceDto.setId(id);
            Device device = convertToEntity(deviceDto);
            Device updatedDevice = deviceRepository.save(device);
            return convertToDto(updatedDevice);
        }
        return null;
    }

    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }

    private DeviceDto convertToDto(Device device) {
        DeviceDto dto = new DeviceDto();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setType(device.getType());
        dto.setCapabilities(device.getCapabilities());
        return dto;
    }

    private Device convertToEntity(DeviceDto dto) {
        Device entity = new Device();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setCapabilities(dto.getCapabilities());
        return entity;
    }
}