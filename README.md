# Huffman Compressor Backend

A **Spring Boot** REST API for Huffman compression and decompression. This backend provides high-performance file compression services with comprehensive error handling, progress tracking, and validation.

## 🚀 Features

- **🗜️ Huffman Compression** - Lossless compression using optimal prefix-free codes
- **📤 File Decompression** - Perfect restoration of original files
- **📊 Compression Analysis** - Analyze files before compressing
- **✅ File Validation** - Verify `.huff` file integrity
- **📈 Progress Tracking** - Real-time operation progress
- **🔄 Async Processing** - Non-blocking file operations
- **🛡️ Error Handling** - Comprehensive exception management
- **📝 Health Monitoring** - Spring Actuator integration

## 📋 Prerequisites

- **Java** 21 or higher
- **Maven** 3.8 or higher

## 🛠️ Installation

```bash
# Clone and navigate to project
cd file-compression

# Build the project
mvn clean install

# Run tests
mvn test
```

## 🏃 Running the Application

### Development Mode

```bash
mvn spring-boot:run
```

### Production Mode

```bash
# Build JAR
mvn clean package -DskipTests

# Run JAR
java -jar target/file-compression-1.0.0.jar
```

The server starts at `http://localhost:8081`

## 📁 Project Structure

```
src/
├── main/
│   ├── java/rickm/filecompression/
│   │   ├── FileCompressionApplication.java         # Entry point
│   │   ├── config/
│   │   │   └── AsyncConfig.java                    # App configuration for threads
│   │   │   └── CorsConfig.java                     # App configuration for cors policy
│   │   │   └── OpenApiConfig.java                  # App configuration for Open API Docs
│   │   ├── controller/
│   │   │   ├── HuffmanController.java              # REST endpoints
│   │   ├── model/
│   │   │   ├── CompressionResult.java              # Result DTO
│   │   │   ├── HuffmanNode.java                    # Tree node
│   │   │   └── ProgressInfo.java                   # Progress tracking
│   │   ├── properties/
│   │   │   └── AsyncProperties.java                # App properties for threads
│   │   │   └── CorsProperties.java                 # App properties for cors policy
│   │   ├── service/
│   │   │   └── CompressionService.java             # Business logic interface
│   │   │   └── impl/
│   │   │       └── HuffmanCompressionService.java  # Business logic implementation
│   │   └── util/
│   │       └── HuffmanCodec.java                   # Core algorithm
│   └── resources/
│       ├── application.yml                         # Configuration
│       └── application-prod.yml                    # Production Configuration
└── test/
    └── java/rickm/filecompression/
        ├── controller/
        │   └── HuffmanControllerTest.java
        └── service/
        │   ├── HuffmanCompressionServiceTest.java
        └── utils/
            ├── HuffmanCodecTest.java
```

## 🔌 API Reference

### Compress File

**Binary Response:**
```http
POST /huffman/compress
Content-Type: multipart/form-data

file: <binary data>
```

**Response:** Binary compressed data with headers:
- `X-Original-Size`: Original file size
- `X-Compressed-Size`: Compressed file size
- `X-Compression-Ratio`: Compression percentage
- `X-Processing-Time-Ms`: Processing time

**JSON Response:**
```http
POST /huffman/compress/json
Content-Type: multipart/form-data

file: <binary data>
```

**Response:**
```json
{
  "success": true,
  "data": "SFVGR...(base64)...",
  "fileName": "document.txt.huff",
  "originalSize": 1024,
  "compressedSize": 512,
  "formattedOriginalSize": "1 KB",
  "formattedCompressedSize": "512 B",
  "formattedCompressionRatio": "50.00%",
  "processingTimeMs": 23
}
```

### Decompress File

**Binary Response:**
```http
POST /huffman/decompress
Content-Type: multipart/form-data

file: <.huff binary data>
```

**JSON Response:**
```http
POST /huffman/decompress/json
Content-Type: multipart/form-data

file: <.huff binary data>
```

**Response:**
```json
{
  "success": true,
  "data": "SGVsbG8gV29ybGQ=",
  "fileName": "document.txt",
  "originalSize": 1024,
  "compressedSize": 512,
  "formattedOriginalSize": "1 KB",
  "formattedCompressedSize": "512 B",
  "processingTimeMs": 15
}
```

### Analyze File

```http
POST /huffman/analyze
Content-Type: multipart/form-data

file: <binary data>
```

**Response:**
```json
{
  "success": true,
  "originalSize": 1024,
  "estimatedCompressedSize": 620,
  "estimatedRatio": "39.45%",
  "uniqueSymbols": 42
}
```

### Validate File

```http
POST /huffman/validate
Content-Type: multipart/form-data

file: <binary data>
```

**Response:**
```json
{
  "valid": true,
  "message": "Valid Huffman compressed file"
}
```

### Health Check

```http
GET /huffman/health
```

**Response:**
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## ⚙️ Configuration

### application.yml

```properties
# Server Configuration
server.port=8081

# File Upload Limits
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Logging
logging.level.com.huffman=DEBUG

# Actuator Endpoints
management.endpoints.web.exposure.include=health,info,metrics
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8081`  | Server port |
| `MAX_FILE_SIZE` | `100MB` | Maximum upload size |
| `LOG_LEVEL` | `INFO`  | Logging level |

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=HuffmanCodecTest
```

### Generate Coverage Report

```bash
mvn jacoco:report
# Report at target/site/jacoco/index.html
```

### Test Categories

- **Unit Tests** - `HuffmanCodecTest.java`
  - Frequency table building
  - Huffman tree construction
  - Code generation
  - Compression/decompression
  - Edge cases (empty, single byte, binary)
  
- **Service Tests** - `HuffmanCompressionServiceTest.java`
  - Service layer operations
  - Progress tracking
  - Result caching
  - Error handling
  
- **Integration Tests** - `HuffmanControllerTest.java`
  - REST endpoint testing
  - File upload handling
  - Response validation
  - CORS headers

## 🏛️ Architecture

### Core Algorithm (`HuffmanCodec`)

The codec implements the complete Huffman compression pipeline:

1. **Frequency Analysis** - Count byte occurrences
2. **Tree Building** - Create optimal binary tree
3. **Code Generation** - Assign variable-length codes
4. **Encoding** - Replace bytes with bit codes
5. **File Format** - Write header and data

### Service Layer (`HuffmanCompressionService`)

- Thread-safe operation management
- Progress tracking with `ConcurrentHashMap`
- Result caching for completed operations
- Cleanup utilities for memory management

### Controller Layer (`HuffmanController`)

- RESTful API design
- Multipart file handling
- Response format flexibility (binary/JSON)
- CORS configuration

## 📦 HUFF File Format

```
┌──────────────────┬────────────────────────────────────────┐
│ Offset │ Size    │ Content                                │
├──────────────────┼────────────────────────────────────────┤
│ 0      │ 4 bytes │ Magic number: "HUFF" (0x48554646)      │
│ 4      │ 1 byte  │ Version: 1                             │
│ 5      │ 8 bytes │ Original size (big-endian long)        │
│ 13     │ 4 bytes │ Symbol count (big-endian int)          │
│ 17     │ Variable│ Frequency table [(byte, freq), ...]    │
│ Var    │ Variable│ Encoded data (bit stream)              │
└──────────────────┴────────────────────────────────────────┘
```

## 🔒 Security Considerations

- **File Size Limits** - Configurable maximum upload size
- **Content Validation** - Magic number verification
- **CORS Configuration** - Whitelist allowed origins
- **No Execution** - Files are processed, never executed

## 📊 Performance

### Benchmarks (on standard hardware)

| File Size | Compression Time | Decompression Time |
|-----------|-----------------|-------------------|
| 1 KB      | < 5ms           | < 3ms             |
| 100 KB    | ~20ms           | ~15ms             |
| 1 MB      | ~200ms          | ~150ms            |
| 10 MB     | ~2s             | ~1.5s             |

### Optimization Tips

1. **Thread Pool** - Configure for your workload
2. **Memory** - Increase heap for large files
3. **Caching** - Results are cached per operation

## 🔧 Troubleshooting

### Common Issues

**"File too large" error:**
```properties
spring.servlet.multipart.max-file-size=200MB
```

**OutOfMemoryError:**
```bash
java -Xmx2g -jar file-compression-1.0.0.jar
```

**CORS errors:**
Add your frontend origin to `CorsConfig.java`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests for new functionality
4. Ensure all tests pass: `mvn test`
5. Submit a Pull Request

## 📝 License

MIT License - see [LICENSE](LICENSE) for details.

## 📚 References

- [Huffman Coding - Wikipedia](https://en.wikipedia.org/wiki/Huffman_coding)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [David A. Huffman's Original Paper (1952)](https://www.ic.tu-berlin.de/fileadmin/fg121/Source-Coding_WS12/selected-readings/10_04051119.pdf)
