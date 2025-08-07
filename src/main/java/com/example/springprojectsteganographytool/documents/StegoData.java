package com.example.springprojectsteganographytool.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document
public class StegoData {

    @Id
    private UUID id;


}
