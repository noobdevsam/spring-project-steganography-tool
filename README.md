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
- **GraalVM Native Image Support**: Build a lightweight, fast-starting native executable container using Spring Boot's Maven plugin and Cloud Native Buildpacks.
- **Capacity Estimation**: Before performing an expensive encoding operation, you can estimate if your data will fit in a given image.
- **Large File Streaming**: Efficiently encodes large files by streaming them, keeping memory usage low.
- **Database Integration**: Metadata for each encoding is saved to a MySQL database using Spring Data JPA.
- **Virtual Threads**: Uses Java 25 Virtual Threads with an embedded Jetty server for high-concurrency request handling.
- **Scheduled Cleanup**: A background task cleans up orphaned stego files and expired extracted files.
- **Docker Compose Support**: Includes a `compose.yml` for easy multi-container setup.

## How It Works

The application embeds data by modifying the least significant bits of an image's pixel color channels (Red, Green, Blue).

### Encoding Process

1.  **Input**: A user provides a cover image, the data to hide (text or a file), and a password.
2.  **Encryption**: The data is encrypted using AES-256.
3.  **Metadata Creation**: A JSON metadata block is created containing details like LSB depth, data type, and a password verification hash.
4.  **LSB Embedding**: The metadata and encrypted data are written into the cover image's pixels.
5.  **Output**: The modified image is saved as a new, lossless **PNG file**, and its metadata is persisted in the database.

### Decoding Process

1.  **Input**: A user provides the stego-image and the password.
2.  **Metadata Extraction**: The application reads the image's LSBs to find and extract the metadata.
3.  **Password Verification**: It hashes the provided password and compares it to the hash stored in the metadata.
4.  **Payload Extraction & Decryption**: The application reads and decrypts the hidden data.
5.  **Output**: The original text or file is returned.

## Technology Stack

- **Java 25** & **Spring Boot 3**
- **GraalVM**: For building a native executable.
- **Spring Web, Data JPA, Jetty**
- **MySQL**: Database for persisting encoding metadata.
- **Maven**: For project and dependency management.
- **Docker & Docker Compose**: For container orchestration.

## API Endpoints

The base path for all endpoints is `/api/v1/stego`.

| Method   | Endpoint          | Description                                                                                 |
| :------- | :---------------- | :------------------------------------------------------------------------------------------ |
| `GET`    | `/estimate`       | Estimates the data capacity of an image.   |
| `POST`   | `/encode/text`    | Encodes a text message into a cover image.                                                  |
| `POST`   | `/encode/file`    | Encodes a file into a cover image. |
| `POST`   | `/decode`         | Decodes a message or file from a stego-image.                              |
| `POST`   | `/metadata`       | Extracts steganography metadata from an image.     |
| `GET`    | `/encodings`      | Lists all steganography encodings.                                   |
| `GET`    | `/encodings/{id}` | Retrieves a specific encoding by its UUID.                                                  |
| `DELETE` | `/encodings/{id}` | Deletes an encoding record and the corresponding stego file.                   |

## Getting Started

### Prerequisites

- **JDK 25 or higher** (The project is configured for Java 25).
- **Maven 3.8+**.
- **Docker and Docker Compose**.
- A REST client like [Postman](https://www.postman.com/) or `curl`.

### Cloning the Repository

```bash
git clone https://github.com/noobdevsam/spring-project-steganography-tool.git
cd spring-project-steganography-tool
```

### Running the Application (JVM)

For local development, you can run the application directly on your machine's JVM.

1.  **Start the Database**:
    ```bash
    docker compose up -d db-mysql
    ```

2.  **Run the Spring Boot Application**:
    ```bash
    ./mvnw spring-boot:run
    ```
    The application will start on port `8080`.

## Containerization with Docker (Native Image)

The recommended way to run the application in production is to build a native image container using the Spring Boot Maven plugin.

This process involves two simple steps:

1.  **Build the Native Docker Image:**
    Run the following Maven command. This will use Cloud Native Buildpacks to compile the application into a native executable and package it into a minimal Docker image named `noobdevsam/spring-project-steganography-tool:0.0.1-SNAPSHOT`.

    ```bash
    # This command can take several minutes to complete
    ./mvnw spring-boot:build-image -Pnative
    ```

2.  **Run with Docker Compose:**
    Once the image is built, you can start the application and the database using Docker Compose.

    ```bash
    docker compose up
    ```
    The application will be available on port `8080`. The `storage` directory is mounted as a volume, so your files will persist between container restarts.

To stop the services, press `Ctrl+C` or run:
```bash
docker compose down
```

## Project Structure

- `configs/`: Spring configuration classes.
- `controllers/`: REST controllers for the API.
- `entities/`: JPA entity classes.
- `exceptions/`: Custom exceptions and the global exception handler.
- `mappers/`: MapStruct DTO-entity converters.
- `models/`: API Data Transfer Objects (DTOs).
- `repos/`: Spring Data JPA repositories.
- `services/`: Business logic interfaces and implementations.
- `cleanup/`: Scheduled cleanup task.
- `filters/`: Servlet filters for request processing.

## Contributing

Contributions are welcome! Please fork the repository, create a feature branch, and open a pull request.

## License

This project is licensed under the **GNU Affero General Public License v3.0**. See the [LICENSE](LICENSE) file for details.
