# Spring Boot Steganography Tool

This project is a powerful and secure steganography tool built with Spring Boot 3. It allows you to hide text messages or files within images using the Least Significant Bit (LSB) technique. The application provides a RESTful API for all its operations, supports robust AES-256 encryption for hidden data, and includes features like large file streaming to ensure memory efficiency.

## Table of Contents

- [Features](#features)
- [How It Works](#how-it-works)
  - [Encoding Process](#encoding-process)
  - [Decoding Process](#decoding-process)
- [Technology Stack](#technology-stack)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Cloning the Repository](#cloning-the-repository)
  - [Configuration](#configuration)
  - [Running the Application](#running-the-application)
- [Interacting with the API](#interacting-with-the-api)
  - [Example 1: Encode a Text Message](#example-1-encode-a-text-message)
  - [Example 2: Decode a Stego Image](#example-2-decode-a-stego-image)
- [Project Structure](#project-structure)

## Features

- **Text & File Steganography**: Embed both plain text and binary files within images.
- **Strong Encryption**: All hidden data is encrypted with **AES-256 (CBC mode)** using a key derived from your password via **PBKDF2**.
- **RESTful API**: Easy-to-use endpoints for encoding, decoding, capacity estimation, and managing encodings.
- **Capacity Estimation**: Before performing an expensive encoding operation, you can estimate if your data will fit in a given image.
- **Large File Streaming**: Efficiently encodes large files by streaming them, keeping memory usage low. A configurable threshold (`app.stream.threshold-bytes`) determines when to switch to streaming.
- **Database Integration**: Metadata about each encoding (e.g., file names, sizes) is saved to a MySQL database using Spring Data JPA.
- **Virtual Threads**: The application is configured to use Java 21+ Virtual Threads with an embedded Jetty server for high-concurrency request handling.
- **Scheduled Cleanup**: A background task runs periodically to clean up orphaned stego files from storage and expired extracted files.

## How It Works

The application uses the Least Significant Bit (LSB) steganography method. It modifies the least significant bits of the color channels (Red, Green, Blue) in an image's pixels to store data. The changes are so subtle that they are virtually invisible to the human eye.

### Encoding Process

1.  **Input**: The user provides a cover image, the data to hide (text or a file), and a password.
2.  **Encryption**: The data is encrypted using AES-256. The encryption key is derived from the user's password and a random salt using the PBKDF2 algorithm. This ensures that the same password always produces a different key for different encryptions, enhancing security.
3.  **Metadata Creation**: A JSON metadata block is created containing essential information for decoding:
    -   `lsbDepth`: The number of LSBs to use per color channel (1 or 2).
    -   `hasText` / `hasFile`: A flag indicating the type of hidden data.
    -   `encryptionKeyHash`: A **SHA-256 hash** of the encryption key. This is used during decoding to quickly verify the password without exposing the key itself.
    -   `nameOfFileToEmbed`: The original filename if a file is being embedded.
4.  **LSB Embedding**: The data is written into the cover image's pixels in a specific format:
    -   First, a 4-byte "magic number" (`STEG`) and a version byte are written at LSB depth 1. This helps identify images processed by this tool.
    -   Next, the length of the metadata, the metadata JSON itself, and the length of the encrypted payload are written.
    -   Finally, the encrypted payload is written using the user-specified LSB depth.
5.  **Output**: The modified image is saved as a new, lossless **PNG file** to the configured storage directory, and its metadata is persisted in the database.

### Decoding Process

1.  **Input**: The user provides the stego-image and the password used for encoding.
2.  **Metadata Extraction**: The application reads the image's LSBs to find the "magic number" and version. If valid, it extracts the full metadata block.
3.  **Password Verification**: It hashes the provided password and compares it to the `encryptionKeyHash` stored in the metadata. If the hashes do not match, the process is aborted.
4.  **Payload Extraction**: Using the payload length and LSB depth from the metadata, the application reads the raw encrypted data from the image's pixels.
5.  **Decryption**: The extracted payload is decrypted using the user's password.
6.  **Output**: The original data is returned. If it's text, the message is provided directly. If it's a file, it is saved to a temporary location, and its path is returned. The temporary file is automatically deleted after a configured TTL (`app.extraction.temp-ttl-ms`).

## Technology Stack

- **Java 25** & **Spring Boot 3**
- **Spring Web, Data JPA, Jetty**
- **MySQL**: Database for persisting encoding metadata.
- **Lombok**: To reduce boilerplate code.
- **MapStruct**: For high-performance DTO-entity mapping.
- **Maven**: For project and dependency management.
- **Docker & Docker Compose**: For easy database setup.

## API Endpoints

The following are the main endpoints provided by the application. The base path is `/api/v1/stego`.

| Method | Endpoint                    | Description                                                                                             |
| :----- | :-------------------------- | :------------------------------------------------------------------------------------------------------ |
| `GET`  | `/estimate`                 | Estimates the data capacity of an image based on its dimensions, LSB depth, and payload size.           |
| `POST` | `/encode/text`              | Encodes a text message into a cover image.                                                              |
| `POST` | `/encode/file`              | Encodes a file into a cover image. Handles both in-memory and streaming based on file size.             |
| `POST` | `/decode`                   | Decodes a message or file from a stego-image using a password.                                          |
| `POST` | `/metadata`                 | Extracts and returns steganography metadata from an image without decoding the payload.                 |
| `GET`  | `/encodings`                | Lists all steganography encodings stored in the database.                                               |
| `GET`  | `/encodings/{id}`           | Retrieves a specific encoding by its UUID.                                                              |
| `DELETE`| `/encodings/{id}`          | Deletes an encoding record from the database and the corresponding stego file from storage.              |

## Getting Started

Follow these steps to clone and run the project on your local machine.

### Prerequisites

- **JDK 21 or higher** (The project is configured for Java 25).
- **Maven 3.8+**.
- **Docker and Docker Compose** (for running the MySQL database).
- A REST client like [Postman](https://www.postman.com/) or `curl` to interact with the API.

### Cloning the Repository

```bash
git clone https://github.com/noobdevsam/spring-project-steganography-tool.git
cd spring-project-steganography-tool
```

### Configuration

The main configuration is in `src/main/resources/application.yml`. The default settings are configured to work with the provided Docker Compose setup.

Key properties:
- `spring.datasource.*`: Configures the connection to the MySQL database.
- `app.storage.base-path`: The local directory where stego-images and extracted files will be stored. Defaults to `storage/`.
- `app.cleanup.*`: Settings for the scheduled file cleanup task.
- `app.stream.threshold-bytes`: File size (in bytes) above which streaming encoding is used. Defaults to 1MB.
- `app.extraction.temp-ttl-ms`: Time-to-live for extracted files. Defaults to 5 minutes.

### Running the Application

1.  **Start the Database**:
    Use Docker Compose to start the MySQL database service.
    ```bash
    docker-compose up -d
    ```
    This will start a MySQL container, create a `stego` database, and configure a `user`.

2.  **Run the Spring Boot Application**:
    You can run the application using the Maven wrapper.
    ```bash
    ./mvnw spring-boot:run
    ```
    The application will start and connect to the database. By default, the server runs on port `8080`.

## Interacting with the API

Here are a few examples using `curl`.

### Example 1: Encode a Text Message

This command encodes the message "Hello, World!" into `cover.png` with the password "secret".

```bash
curl -X POST http://localhost:8080/api/v1/stego/encode/text \
  -F "coverImage=@/path/to/your/cover.png" \
  -F "message=Hello, World!" \
  -F "password=secret" \
  -F "lsbDepth=1"
```

The API will return a JSON response with details of the newly created stego-image, including its filename and UUID.

### Example 2: Decode a Stego Image

This command decodes the data from `stego-image.png` using the password "secret".

```bash
curl -X POST http://localhost:8080/api/v1/stego/decode \
  -F "stegoImage=@/path/to/your/stego-image.png" \
  -F "password=secret"
```

The API will return the hidden data. If it's text, the JSON will contain the message. If it's a file, it will contain the path to the temporarily extracted file.

## Project Structure

The project follows a standard Spring Boot structure, with a clear separation of concerns.

- `configs/`: Spring configuration classes (e.g., `VirtualThreadConfig`).
- `controllers/`: REST controllers that handle API requests (`StegoController`).
- `entities/`: JPA entity classes (`StegoData`).
- `exceptions/`: Custom exception classes and a global exception handler.
- `mappers/`: MapStruct interfaces for DTO-entity conversions.
- `models/`: DTOs (Data Transfer Objects) used for API requests and responses.
- `repos/`: Spring Data JPA repositories (`StegoDataRepository`).
- `services/`: Business logic, split into interfaces and implementations.
- `cleanup/`: Contains the `OrphanCleanupTask` for scheduled jobs.
- `filters/`: Servlet filters like `CorrelationIdFilter` for adding a trace ID to logs.

---
