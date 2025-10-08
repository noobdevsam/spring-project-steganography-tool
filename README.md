# Spring Boot Steganography Tool

![Java](https://img.shields.io/badge/Java-25-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen.svg)
![License](https://img.shields.io/badge/License-AGPL--3.0-blue.svg)

This project is a powerful and secure steganography tool built with Spring Boot 3. It allows you to hide text messages or files within images using the Least Significant Bit (LSB) technique. The application provides a RESTful API for all its operations, supports robust AES-256 encryption for hidden data, and is optimized for production with Java Virtual Threads.

## Table of Contents

- [What is Steganography?](#what-is-steganography)
- [Features](#features)
- [How It Works](#how-it-works)
  - [Encoding Process](#encoding-process)
  - [Decoding Process](#decoding-process)
  - [Security Model](#security-model)
- [Technology Stack](#technology-stack)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Cloning the Repository](#cloning-the-repository)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
  - [1. For Local Development (JVM)](#1-for-local-development-jvm)
  - [2. For Production (Docker Image)](#2-for-production-docker-image)
- [Interacting with the API](#interacting-with-the-api)
  - [Example 1: Estimate Capacity](#example-1-estimate-capacity)
  - [Example 2: Encode a Text Message](#example-2-encode-a-text-message)
  - [Example 3: Decode a Stego Image](#example-3-decode-a-stego-image)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

## What is Steganography?

Steganography is the practice of concealing a message or file within another file. Unlike cryptography, which obscures the *content* of a message, steganography conceals the very *existence* of the message. This tool uses the **Least Significant Bit (LSB)** method, which subtly alters the color data of image pixels to embed information, making the changes virtually invisible to the human eye.

## Features

- **Text & File Steganography**: Embed both plain text and binary files within images.
- **Strong Encryption**: All hidden data is encrypted with **AES-256 (CBC mode)**. The encryption key is derived from your password using **PBKDF2 with 65,536 iterations** and a unique salt for each encoding.
- **RESTful API**: A comprehensive API for encoding, decoding, capacity estimation, and managing encodings.
- **Docker Support**: Build and run the application as a container using the integrated Spring Boot Maven plugin and Docker Compose.
- **High-Concurrency Ready**: Utilizes **Java 25 Virtual Threads** with an embedded Jetty server to efficiently handle a large number of concurrent requests.
- **Large File Streaming**: Efficiently encodes and encrypts large files by streaming them, keeping memory usage low.
- **Capacity Estimation**: A dedicated endpoint to check if your data will fit in a given image before performing the encoding.
- **Database Integration**: Encoding metadata is saved to a MySQL database using Spring Data JPA.
- **Scheduled Cleanup**: A background task periodically cleans up orphaned stego files from storage and deletes expired extracted files.
- **Robust Error Handling**: A global exception handler provides detailed, structured error responses (`ProblemDetail`) for API clients.
- **Traceability**: A `CorrelationIdFilter` adds a unique trace ID to every request for improved logging and debugging.

## How It Works

### Encoding Process

1.  **Input**: A user provides a cover image, the data to hide (text or a file), and a password.
2.  **Encryption**: The data is encrypted using AES-256. A unique salt is generated for each operation.
3.  **Metadata Creation**: A JSON metadata block is created containing details like LSB depth, data type, and a **SHA-256 hash** of the encryption key for password verification on decode.
4.  **LSB Embedding**: The metadata and encrypted data are written into the least significant bits of the cover image's pixels.
5.  **Output**: The modified image is saved as a new, lossless **PNG file**, and its metadata is persisted in the database.

### Decoding Process

1.  **Input**: A user provides the stego-image and the password used for encoding.
2.  **Metadata Extraction**: The application reads the image's LSBs to find the "magic number" (`STEG`) and extracts the metadata.
3.  **Password Verification**: It derives a key from the provided password, hashes it, and compares it to the hash stored in the metadata. The process fails if they don't match.
4.  **Payload Extraction & Decryption**: The application reads the hidden data from the pixels and decrypts it using the password.
5.  **Output**: The original text or file is returned.

### Security Model

- **Password Derivation**: The AES key is derived from the user's password and a unique, random **16-byte salt** using **PBKDF2**. This ensures that even identical passwords produce different keys for different encodings.
- **Password Verification**: During decoding, the provided password is used to derive a key, which is then hashed (SHA-256) and compared against the hash stored in the image's metadata. This prevents incorrect password attempts without decrypting the entire payload.
- **Authenticated Encryption**: The use of **CBC mode with PKCS5Padding** and a unique **16-byte Initialization Vector (IV)** ensures that encrypted data is resistant to common cryptographic attacks.

## Technology Stack

- **Java 25** & **Spring Boot 3**
- **Spring Framework**: Web, Data JPA, Jetty (for Virtual Threads).
- **Database**: MySQL.
- **Build & Packaging**: Maven, Spring Boot Buildpacks.
- **Containerization**: Docker & Docker Compose.
- **Utilities**: Lombok, MapStruct.

## API Endpoints

The base path for all endpoints is `/api/v1/stego`.

| Method   | Endpoint                      | Description                                                               |
| :------- | :---------------------------- | :------------------------------------------------------------------------ |
| `GET`    | `/estimate`                   | Estimates the data capacity of an image.                                  |
| `POST`   | `/encode/text`                | Encodes a text message into a cover image.                                |
| `POST`   | `/encode/file`                | Encodes a file into a cover image.                                        |
| `POST`   | `/decode`                     | Decodes a message or file from a stego-image.                             |
| `POST`   | `/metadata`                   | Extracts steganography metadata from an image without full decoding.      |
| `GET`    | `/encodings`                  | Lists all steganography encodings stored in the database.                 |
| `GET`    | `/encodings/{id}`             | Retrieves a specific encoding by its UUID.                                |
| `DELETE` | `/encodings/{id}`             | Deletes an encoding record and its associated file.                       |
| `GET`    | `/encodings/{id}/download`    | Downloads the specified stego-image file.                                 |
| `GET`    | `/download/extracted/{name}`  | Downloads a temporarily stored file after decoding.                       |

## Getting Started

### Prerequisites

- **JDK 25 or higher**
- **Maven 3.8+**
- **Docker and Docker Compose**
- A REST client like [Postman](https://www.postman.com/) or `curl`.

### Cloning the Repository

```bash
git clone https://github.com/noobdevsam/spring-project-steganography-tool.git
cd spring-project-steganography-tool
```

## Configuration

The main configuration is in `src/main/resources/application.yml`. Key properties can be overridden with environment variables, which is the standard practice for containerized deployment.

| Property                     | Environment Variable  | Description                                                      | Default Value   |
| :--------------------------- | :-------------------- | :--------------------------------------------------------------- | :-------------- |
| `spring.datasource.url`      | `DATABASE_URL`        | JDBC URL for the MySQL database.                                 | `jdbc:mysql://...` |
| `spring.datasource.username` | `DATABASE_USER`       | Database username.                                               | `user`          |
| `spring.datasource.password` | `DATABASE_PASSWORD`   | Database password.                                               | `password`      |
| `app.storage.base-path`      | `STORAGE_BASE_PATH`   | Local directory where files will be stored.                      | `storage`       |
- `app.stream.threshold-bytes`: File size (in bytes) above which streaming is used for encoding. Default: `1MB`.
- `app.cleanup.*`: Settings for the scheduled file cleanup task.
- `app.extraction.temp-ttl-ms`: Time-to-live for extracted files. Default: `5 minutes`.

## Running the Application

There are two primary ways to run the application.

### 1. For Local Development (JVM)

This method is ideal for development and debugging, as it uses Spring Boot's hot-reload capabilities.

1.  **Start the Database**:
    Open a terminal and run the following command to start the MySQL database service in the background.
    ```bash
    docker compose up -d db-mysql
    ```

2.  **Run the Spring Boot Application**:
    In a separate terminal, run the application using the Maven wrapper.
    ```bash
    ./mvnw spring-boot:run
    ```
    The application will start on `http://localhost:8080`.

### 2. For Production (Docker Image)

This is the recommended approach for production. It packages the application into a standard JVM-based Docker container.

1.  **Build the Docker Image**:
    Run the following Maven command. This uses Cloud Native Buildpacks to create a Docker image named `noobdevsam/spring-project-steganography-tool:0.0.1-SNAPSHOT`.
    ```bash
    # This command requires Docker to be running.
    ./mvnw spring-boot:build-image -DskipTests
    ```

2.  **Run with Docker Compose**:
    Once the image is built, start the application and the database using Docker Compose.
    ```bash
    # This will use the image built in the previous step.
    docker compose up
    or
    docker compose up -d db-mysql && docker compose up app
    ```
    The application will be available on `http://localhost:8080`.

To stop all services, press `Ctrl+C` or run `docker compose down`.

## Interacting with the API

### Example 1: Estimate Capacity

Check if a 50KB message fits in a 1024x768 image.

**Request:**
```bash
curl "http://localhost:8080/api/v1/stego/estimate?width=1024&height=768&lsbDepth=1&plainLength=51200"
```

**Response:**
```json
{
    "capacityBytes": 294912,
    "overheadBytes": 137,
    "encryptedBytesEstimate": 51216,
    "requiredBytesEstimate": 51353,
    "fits": true,
    "streamThresholdBytes": 1000000
}
```

### Example 2: Encode a Text Message

Encode the message "secret data" into `cover.png` with password "securepass".

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/stego/encode/text \
  -F "coverImage=@/path/to/your/cover.png" \
  -F "message=secret data" \
  -F "password=securepass" \
  -F "lsbDepth=1"
```

### Example 3: Decode a Stego Image

Decode `stego-image.png` using the password "securepass".

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/stego/decode \
  -F "stegoImage=@/path/to/your/stego-image.png" \
  -F "password=securepass" \
  --output response.json
```

## Project Structure

- `configs/`: Spring configuration classes (e.g., `VirtualThreadConfig`).
- `controllers/`: REST controllers for the API.
- `entities/`: JPA entity classes (`StegoData`).
- `exceptions/`: Custom exceptions and the global exception handler.
- `filters/`: Servlet filters for request processing (`CorrelationIdFilter`).
- `mappers/`: MapStruct DTO-entity converters.
- `models/`: API Data Transfer Objects (DTOs).
- `repos/`: Spring Data JPA repositories.
- `services/`: Business logic interfaces and implementations.
- `cleanup/`: Scheduled cleanup task for orphaned files.

## Contributing

Contributions are welcome! Please fork the repository, create a feature branch, and open a pull request.

## License

This project is licensed under the **GNU Affero General Public License v3.0**. See the [LICENSE](LICENSE) file for details.
