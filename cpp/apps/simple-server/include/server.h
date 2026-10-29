#pragma once
#include <string>
#include <vector>
#include <atomic>

#include "SimpleServer.h"
#include "serialization/DeserializerL5StyxImpl.h"
#include "impl/ClientsRepoImpl.h"

class ServerConsole {
public:
    struct Config {
        int port;
        std::vector<std::string> interfaces;
        std::string exportPath;
        bool enableLogging;
        bool enableCli;
    };
private:
    styxlib::serialization::DeserializerL5StyxImpl deserializer;
    styxlib::ClientsRepoImpl clientsRepo;
        
public:
    ServerConsole(const Config& config);
    ~ServerConsole();
    void start();

    const Config& getConfig() const { return config; }

private:
    Config config;
    std::atomic<bool> running{false};
    void initLogging();
};