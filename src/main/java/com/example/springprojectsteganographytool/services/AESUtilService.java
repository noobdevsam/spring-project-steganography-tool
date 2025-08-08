package com.example.springprojectsteganographytool.services;

public interface AESUtilService {

    byte[] encrypt(
            byte[] plainText,
            String password
    ) throws Exception;

    byte[] decrypt(
            byte[] input,
            String password
    ) throws Exception;

}
