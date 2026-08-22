#include "server.h"

#include <iostream>
#include <thread>
#include <chrono>
#include <spdlog/spdlog.h>
#include <spdlog/sinks/rotating_file_sink.h>

#include "SimpleServer.h"
#include "impl/ChannelUnixUdp.h"
#include "impl/ClientsRepoImpl.h"
#include "serialization/DeserializerL5StyxImpl.h"

ServerConsole::ServerConsole(const Config& config) : config(config) {
    initLogging();
}

ServerConsole::~ServerConsole() {
    // Destructor implementation
}

void ServerConsole::start() {
    if (running.load()) {
        spdlog::warn("Server is already running.");
        return;
    }
    running.store(true);
    spdlog::info("Server starting on port {}", config.port);

    auto deserializer = std::make_shared<styxlib::DeserializerL5StyxImpl>();
    auto clientsRepo = std::make_shared<styxlib::ClientsRepoImpl>();
    styxlib::ChannelUnixUdpServer::Configuration channelConfig(
        static_cast<uint16_t>(config.port),
        clientsRepo,
        styxlib::PacketHeaderSize::Size2Bytes,
        8192,
        deserializer,
        16);

    styxlib::SimpleServer::Configuration serverConfig;
    serverConfig.channel = std::make_shared<styxlib::ChannelUnixUdpServer>(channelConfig);
    serverConfig.deserializer = deserializer;
    styxlib::SimpleServer server(serverConfig);
    auto startFuture = server.start();
    auto startResult = startFuture.get();
    while (running.load()) {
        std::printf("command>");
        std::string command;
        std::getline(std::cin, command);
        if (command == "exit") {
            spdlog::info("Shutting down server...");
            running.store(false);
        } else {
            spdlog::info("Received command: {}", command);
        }
    }

    spdlog::info("Server stopped.");
}

void ServerConsole::initLogging() {
    if (config.enableLogging) {
        try {
            auto rotating_sink = std::make_shared<spdlog::sinks::rotating_file_sink_mt>(
                "logs/server.log", 1024 * 1024, 5
            );
            rotating_sink->set_pattern("[%Y-%m-%d %H:%M:%S.%e] [%l] [T%t] %v");
            auto logger = std::make_shared<spdlog::logger>("global_logger", rotating_sink);
            logger->set_level(spdlog::level::trace);
            spdlog::flush_every(std::chrono::seconds(3));
            spdlog::set_default_logger(logger);

        } catch (const spdlog::spdlog_ex& ex) {
            std::cerr << "Log initialization failed: " << ex.what() << std::endl;
        }
    }
}

int main(int argc, char* argv[]) {
    ServerConsole::Config config;
    config.port = 1234;
    config.interfaces = {"127.0.0.1"};
    config.exportPath = "./";
    ServerConsole server(config);
    server.start();
    return 0;
}