package com.example.springprojectsteganographytool.mappers;

import com.example.springprojectsteganographytool.entities.StegoData;
import com.example.springprojectsteganographytool.models.StegoEncodeResponseDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StegoDataMapper {

    StegoEncodeResponseDTO stegoDataToEncodeResponseDTO(StegoData stegoData);

}
