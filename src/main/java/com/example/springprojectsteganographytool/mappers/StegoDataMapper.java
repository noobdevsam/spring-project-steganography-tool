package com.example.springprojectsteganographytool.mappers;

import com.example.springprojectsteganographytool.entities.StegoData;
import com.example.springprojectsteganographytool.models.StegoDecodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoEncodeResponseDTO;
import com.example.springprojectsteganographytool.models.StegoMetadataDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StegoDataMapper {

    StegoEncodeResponseDTO StegoDataToEncodeResponseDTO(StegoData stegoData);

    StegoMetadataDTO StegoDataToMetadataDTO(StegoData stegoData);

    StegoDecodeResponseDTO StegoDataToDecodeResponseDTO(StegoData stegoData);

}
