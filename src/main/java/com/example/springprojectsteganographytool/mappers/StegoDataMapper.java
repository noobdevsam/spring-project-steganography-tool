package com.example.springprojectsteganographytool.mappers;

import com.example.springprojectsteganographytool.entities.StegoData;
import com.example.springprojectsteganographytool.models.StegoEncodeResponseDTO;
import org.mapstruct.Mapper;

/**
 * Mapper interface for converting between StegoData entities and StegoEncodeResponseDTO objects.
 * This interface uses MapStruct to generate the implementation at compile time.
 * The generated implementation is a Spring component.
 */
@Mapper(componentModel = "spring")
public interface StegoDataMapper {

    /**
     * Converts a StegoData entity to a StegoEncodeResponseDTO.
     *
     * @param stegoData the StegoData entity to be converted
     * @return the corresponding StegoEncodeResponseDTO
     */
    StegoEncodeResponseDTO stegoDataToEncodeResponseDTO(StegoData stegoData);

}