package com.example.springprojectsteganographytool.services;

import com.example.springprojectsteganographytool.models.capacity.EstimationResult;

public interface CapacityUtilService {

    long computeTotalCapacityBytes(
            int width,
            int height,
            int lsbDepth
    );

    EstimationResult estimate(
            int width,
            int height,
            int lsbDepth,
            int metadataJsonLength,
            long plainLength
    );

}
