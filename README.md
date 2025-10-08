# Spring Boot Steganography Tool

![Java](https://img.shields.io/badge/Java-25-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen.svg)
![License](https://img.shields.io/badge/License-AGPL--3.0-blue.svg)

This project is a powerful and secure steganography tool built with Spring Boot 3. It allows you to hide text messages or files within images using the Least Significant Bit (LSB) technique. The application provides a RESTful API for all its operations, supports robust AES-256 encryption for hidden data, and includes features like large file streaming to ensure memory efficiency.

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
  - [Running the Application (JVM)](#running-the-application-jvm)
- [Interacting with the API](#interacting-with-the-api)
  - [Example 1: Estimate Capacity](#example-1-estimate-capacity)
  - [Example 2: Encode a Text Message](#example-2-encode-a-text-message)
  - [Example 3: Encode a File](#example-3-encode-a-file)
  - [Example 4: Decode a Stego Image](#example-4-decode-a-stego-image)
- [Building for Production](#building-for-production)
  - [Building a JAR](#building-a-jar)
  - [Building a Native Executable](#building-a-native-executable)
- [Containerization with Docker (Native Image)](#containerization-with-docker-native-image)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

## What is Steganography?

Steganography is the practice of concealing a message, image, or file within another message, image, or file. Unlike cryptography, which obscures the content of a message, steganography conceals the very existence of the message. This tool uses the **Least Significant Bit (LSB)** method, which subtly alters the color data of image pixels to embed information, making the changes virtually invisible to the human eye.

## Features

- **Text & File Steganography**: Embed both plain text and binary files within images.
- **Strong Encryption**: All hidden data is encrypted with **AES-256 (CBC mode)** using a key derived from your password via **PBKDF2**.
- **RESTful API**: Easy-to-use endpoints for encoding, decoding, capacity estimation, and managing encodings.
- **GraalVM Native Image Support**: Includes a multi-stage `Dockerfile` to build a lightweight, fast-starting native executable.
- **Capacity Estimation**: Before performing an expensive encoding operation, you can estimate if your data will fit in a given image.
- **Large File Streaming**: Efficiently encodes large files by streaming them, keeping memory usage low. A configurable threshold (`app.stream.threshold-bytes`) determines when to switch to streaming.
- **Database Integration**: Metadata about each encoding (e.g., file names, sizes) is saved to a MySQL database using Spring Data JPA.
- **Virtual Threads**: The application is configured to use Java 21+ Virtual Threads with an embedded Jetty server for high-concurrency request handling.
- **Scheduled Cleanup**: A background task runs periodically to clean up orphaned stego files from storage and expired extracted files.
- **Docker Support**: Comes with a `Dockerfile` and `compose.yml` for easy setup and deployment.

## How It Works

The application embeds data by modifying the least significant bits of an image's pixel color channels (Red, Green, Blue).

### Encoding Process

1.  **Input**: A user provides a cover image, the data to hide (text or a file), and a password.
2.  **Encryption**: The data is encrypted using AES-256.
3.  **Metadata Creation**: A JSON metadata block is created containing:
    -   `lsbDepth`: The number of LSBs to use per color channel (1 or 2).
    -   `hasText` / `hasFile`: A flag indicating the type of hidden data.
    -   `encryptionKeyHash`: A **SHA-256 hash** of the encryption key for password verification.
    -   `nameOfFileToEmbed`: The original filename if embedding a file.
4.  **LSB Embedding**: The data is written into the cover image's pixels in a specific format:
    -   A 4-byte "magic number" (`STEG`) and a version byte identify images processed by this tool.
    -   The metadata, its length, and the encrypted payload's length are written.
    -   Finally, the encrypted payload is written using the user-specified LSB depth.
5.  **Output**: The modified image is saved as a new, lossless **PNG file**, and its metadata is persisted in the database.

### Decoding Process

1.  **Input**: A user provides the stego-image and the password.
2.  **Metadata Extraction**: The application reads the image's LSBs to find the "magic number" and extracts the metadata.
3.  **Password Verification**: It hashes the provided password and compares it to the `encryptionKeyHash` from the metadata. If they don't match, the process fails.
4.  **Payload Extraction**: The application reads the raw encrypted data from the image's pixels.
5.  **Decryption**: The payload is decrypted using the user's password.
6.  **Output**: If text, the message is returned. If a file, it's saved to a temporary location, and its path is returned. Temporary files are deleted automatically after a configured TTL.

### Security Model

- **Password Derivation**: The AES key is derived from the user's password and a unique, random **16-byte salt** using **PBKDF2 with 65,536 iterations**. This salt is stored with the encrypted data, ensuring that even identical passwords produce different keys for different encodings.
- **Password Verification**: During decoding, the provided password is used to derive a key, which is then hashed (SHA-256) and compared against the hash stored in the image's metadata. This prevents incorrect password attempts without decrypting the entire payload and avoids storing the password or key directly.
- **Authenticated Encryption**: The use of **CBC mode with PKCS5Padding** and a unique **16-byte Initialization Vector (IV)** ensures that encrypted data is resistant to common cryptographic attacks.

## Technology Stack

- **Java 25** & **Spring Boot 3**
- **GraalVM**: For building a native executable.
- **Spring Web, Data JPA, Jetty**
- **MySQL**: Database for persisting encoding metadata.
- **Lombok**: To reduce boilerplate code.
- **MapStruct**: For high-performance DTO-entity mapping.
- **Maven**: For project and dependency management.
- **Docker & Docker Compose**: For easy database setup and application containerization.

## API Endpoints

The base path for all endpoints is `/api/v1/stego`.

| Method   | Endpoint          | Description                                                                                 |
| :------- | :---------------- | :------------------------------------------------------------------------------------------ |
| `GET`    | `/estimate`       | Estimates the data capacity of an image based on dimensions, LSB depth, and payload size.   |
| `POST`   | `/encode/text`    | Encodes a text message into a cover image.                                                  |
| `POST`   | `/encode/file`    | Encodes a file into a cover image. Handles both in-memory and streaming based on file size. |
| `POST`   | `/decode`         | Decodes a message or file from a stego-image using a password.                              |
| `POST`   | `/metadata`       | Extracts and returns steganography metadata from an image without decoding the payload.     |
| `GET`    | `/encodings`      | Lists all steganography encodings stored in the database.                                   |
| `GET`    | `/encodings/{id}` | Retrieves a specific encoding by its UUID.                                                  |
| `DELETE` | `/encodings/{id}` | Deletes an encoding record and the corresponding stego file from storage.                   |

## Getting Started

### Prerequisites

- **JDK 21 or higher** (The project is configured for Java 25).
- **Maven 3.8+**.
- **Docker and Docker Compose** (for running the MySQL database and the application).
- A REST client like [Postman](https://www.postman.com/) or `curl`.

### Cloning the Repository

```bash
git clone https://github.com/noobdevsam/spring-project-steganography-tool.git
cd spring-project-steganography-tool
```

### Configuration

The main configuration is in `src/main/resources/application.yml`. The default settings are designed to work with the provided Docker Compose setup.

| Property                        | Description                                                              | Default Value |
| :------------------------------ | :----------------------------------------------------------------------- | :------------ |
| `spring.datasource.*`           | Configures the connection to the MySQL database.                         | (See file)    |
| `app.storage.base-path`         | Local directory where stego-images and extracted files will be stored.   | `storage/`    |
| `app.cleanup.enabled`           | Enables the scheduled file cleanup task.                                 | `true`        |
| `app.cleanup.interval-ms`       | Interval for the cleanup task.                                           | `120000` (2m) |
| `app.stream.threshold-bytes`    | File size (in bytes) above which streaming encoding is used.             | `1000000` (1MB) |
| `app.extraction.temp-ttl-ms`    | Time-to-live for extracted files.                                        | `300000` (5m) |

### Running the Application (JVM)

If you wish to run the application directly on your machine's JVM without containerization:

1.  **Start the Database**:
    Use Docker Compose to start only the MySQL database service.
    ```bash
    docker compose up -d db-mysql
    ```
    This will start a MySQL container, create a `stego` database, and configure a `user`.

2.  **Run the Spring Boot Application**:
    You can run the application using the Maven wrapper.
    ```bash
    ./mvnw spring-boot:run
    ```
    The application will start on port `8080`.

## Interacting with the API

### Example 1: Estimate Capacity

Check if a 10KB message fits in a 1920x1080 image using LSB depth 1.

**Request:**
```bash
curl "http://localhost:8080/api/v1/stego/estimate?width=1920&height=1080&lsbDepth=1&plainLength=10240"
```

**Response:**
```json
{
    "capacityBytes": 777600,
    "overheadBytes": 137,
    "encryptedBytesEstimate": 10256,
    "requiredBytesEstimate": 10393,
    "fits": true,
    "streamThresholdBytes": 1000000
}
```

### Example 2: Encode a Text Message

Encode the message "Secret message" into `cover.png` with password "secure_password".

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/stego/encode/text \
  -F "coverImage=@/path/to/your/cover.png" \
  -F "message=Secret message" \
  -F "password=secure_password" \
  -F "lsbDepth=1"
```

### Example 3: Encode a File

Encode `document.pdf` into `image.png` with the same password.

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/stego/encode/file \
  -F "coverImage=@/path/to/your/image.png" \
  -F "fileToEmbed=@/path/to/your/document.pdf" \
  -F "password=secure_password" \
  -F "lsbDepth=2"
```

### Example 4: Decode a Stego Image

Decode `stego-image.png` using the password "secure_password".

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/stego/decode \
  -F "stegoImage=@/path/to/your/stego-image.png" \
  -F "password=secure_password"
```

## Building for Production

### Building a JAR

You can build the project into an executable JAR file using Maven.

```bash
./mvnw clean package
```
The JAR file will be created in the `target/` directory.

### Building a Native Executable

You can compile the application into a self-contained native executable using the GraalVM native image plugin.

```bash
# This requires a GraalVM-enabled JDK and can take several minutes.
./mvnw native:compile -Pnative
```
The executable will be created in the `target/` directory.

## Containerization with Docker (Native Image)

The recommended way to run the application in production is to build a minimal Docker container with a GraalVM native executable.

The provided `compose.yml` is configured to build and run both the application and its database.

1.  **Build and Run with Docker Compose:**
    This single command will:
    -   Build the native executable inside a Docker container.
    -   Create a minimal, production-ready Docker image.
    -   Start the application container and the MySQL database container.

    ```bash
    docker-compose up --build
    ```
    The application will be available on port `8080`. The `storage` directory is mounted as a volume, so your files will persist between container restarts.

2.  **Stopping the services:**
    To stop the containers, press `Ctrl+C` or run:
    ```bash
    docker-compose down
    ```

## Project Structure

- `configs/`: Spring configuration classes (e.g., `VirtualThreadConfig`).
- `controllers/`: REST controllers that handle API requests (`StegoController`).
- `entities/`: JPA entity classes (`StegoData`).
- `exceptions/`: Custom exception classes and a global exception handler.
- `mappers/`: MapStruct interfaces for DTO-entity conversions.
- `models/`: DTOs (Data Transfer Objects) for API requests and responses.
- `repos/`: Spring Data JPA repositories (`StegoDataRepository`).
- `services/`: Business logic, split into interfaces and implementations.
- `cleanup/`: Contains the `OrphanCleanupTask` for scheduled jobs.
- `filters/`: Servlet filters like `CorrelationIdFilter` for adding a trace ID to logs.

## Contributing

Contributions are welcome! Please fork the repository, create a feature branch, and open a pull request.

## License

This project is licensed under the **GNU Affero General Public License v3.0**. See the [LICENSE](LICENSE) file for details.
