# Copilot Instructions for V2StyxLib

V2StyxLib is a multi-language implementation of the Styx (9P2000) protocol with Java, C++, and C layers.

## Architecture

The codebase is organized in **protocol layers** across languages:

- **L4 (C)**: Core protocol wire format and low-level operations
- **L5 (Java/C++)**: Styx protocol messaging, serialization/deserialization
- **L6 (Java)**: File system abstraction and client/server interfaces
- **L7+ (Java)**: Application-level features (console client, server samples)

### Language Organization

```
java/
  v2styx-lib/           # Core protocol library (L4-L7)
    src/main/java/com/v2soft/styxlib/
      l4/               # C bindings
      l5/               # Protocol messages, serialization
      l6/               # File system abstraction
      server/           # Server implementation
      handlers/         # Message routing
      io/               # Channel drivers (TCP, Unix sockets)
  v2styx-server-sample/ # Example Styx server
  v2styx-console-client/ # Console test client

cpp/
  include/
    l4/                 # C++ bindings to C layer
    l5/                 # Protocol messages
    l6/                 # File system abstraction
    handlers/
    io/
  lib/                  # Implementation files
  testserver/           # Test server
  apps/                 # Example applications

c/l4/
  include/
    v2styx/            # Core protocol definitions
  lib/                 # Low-level implementation
```

### Key Design Patterns

1. **Serialization**: Messages use serializer/deserializer pattern (BufferWriter/BufferReader)
2. **Dependency Injection**: `StyxSessionDI` manages message processors and transmitters
3. **Channel Abstraction**: `IChannelDriver` interface allows TCP, Unix sockets, UART backends
4. **Exception Hierarchy**: `StyxException` root; `StyxErrorMessageException` for protocol errors
5. **Multi-Version Support**: C++ builds support C++17 and C++23; Java uses JDK 17+

## Build & Test Commands

### Java (Gradle)

**Full build & test suite:**
```bash
cd java && ./gradlew build test
```

**Run specific test:**
```bash
cd java && ./gradlew :v2styx-lib:test --tests 'com.v2soft.styxlib.l5.serialization.*'
```

**Build without tests:**
```bash
cd java && ./gradlew build -x test
```

**Single module:**
```bash
cd java && ./gradlew :v2styx-lib:build
```

### C++ (CMake)

**C++23 build & test:**
```bash
cd cpp
mkdir -p build23 && cd build23
cmake .. -DCPP_VERSION=23 -DCMAKE_BUILD_TYPE=Release
cmake --build . --config Release
ctest --output-on-failure
```

**C++17 build & test:**
```bash
cd cpp
mkdir -p build17 && cd build17
cmake .. -DCPP_VERSION=17 -DCMAKE_BUILD_TYPE=Release
cmake --build . --config Release
ctest --output-on-failure
```

**GCC 14 required** (see CI workflows for version constraints). Clang also supported.

### C (L4)

No standalone build; compiled as part of C++ build via `add_subdirectory(../c/l4 modules/l4_c)`.

## Key Conventions

### Java

- **Package structure mirrors architecture layers**: `l5.messages`, `l5.serialization`, `l6.vfs`, etc.
- **Message classes**: Base classes in `l5.messages.base`; protocol versions (v9p2000, etc.) in subpackages
- **Configuration objects**: Use static inner `Configuration` class for constructor parameters (see `Connection.Configuration`)
- **Resource cleanup**: Implement `Closeable` and use try-with-resources
- **Generated code**: Codegen system in build pipeline; output in `l5.messages.v9p2000` (auto-generated from definitions)

### C++

- **Header-only templates** for serialization (Catch2 for testing)
- **C++ standard**: Minimum C++17, with C++23 features gated behind version checks
- **Exception policy**: Minimal exceptions; use error codes in performance-critical paths
- **Memory model**: Use `USE_STD_MEMORY` option (default: ON) to control `std::unique_ptr` vs. manual management
- **CMake options**:
  - `CPP_VERSION`: 17 or 23 (default: 23)
  - `ENABLE_CATCH2`: Enable Catch2 tests (default: ON)
  - `SUPPORT_UNIX`: Unix socket support (default: ON)
  - `SUPPORT_RP2040`: Raspberry Pi Pico support (default: OFF)

### C (L4)

- **Minimal standard library** (runs on embedded platforms: STM8, CH32V)
- **Channel configuration**: Defined in `Channel_c.h` with customizable constants
  - `BufferSize_t`: Buffer size type (default: `uint8_t`)
  - `V2STYXLIB_CRC16_POLY`, `V2STYXLIB_CRC16_INITIAL_VALUE`: CRC checksums
  - `V2STYXLIB_SOF_MARKER_1/2`: Start of Frame markers (`0x55`, `0xAA`)
- **Target-specific macros**: `V2STYXLIB_TARGET_STM8`, `V2STYXLIB_TARGET_CH32V`
- **Software UART**: Enable with `V2STYXLIB_SOFTUART_TX` (TX only on STM8)

### Cross-Language Patterns

- **Types alignment**: Java uses `int`/`long`; C++/C use `uint32_t`/`uint64_t` (verify with wire format)
- **Error handling**: Java exceptions; C++ status codes; C error enums
- **Logging**: Minimal in library code; use injected Logger interface where needed
- **Testing**: Catch2 (C++), JUnit (Java via Gradle); no unit tests for pure C

## CI/CD

- **C++ tests**: Run on C++17 and C++23 (GCC 14) on PR ready-for-review → converts to draft on failure
- **Java tests**: Run on every push/PR to master with JDK 17
- Test reports uploaded as artifacts

## Common Tasks

**Add a new protocol message:** Implement in `l5.messages` hierarchy (Java), mirror in C++ headers, update serializers

**Add a new channel driver:** Implement `IChannelDriver` interface; add to channel factory

**Debug serialization issues:** Check `BufferWriter`/`BufferReader` for byte order and length calculations

**Port between languages:** Ensure type widths match (e.g., 4-byte int in Java → `uint32_t` in C++)

## MCP Servers

The GitHub MCP server is configured (`.copilot-mcp.json`) to enable Copilot to:
- Query issues and pull requests
- Search repository code
- Fetch commit information
- Reference GitHub discussions

Requires `GITHUB_TOKEN` environment variable with repo access.
