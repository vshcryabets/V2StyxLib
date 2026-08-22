#pragma once
#include <string>
#include <vector>
#include <atomic>

#include "SimpleServer.h"

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
        
public:
    ServerConsole(const Config& config);
    ~ServerConsole();
    void start();

    const Config& getConfig() const { return config; }

private:
    Config config;
    std::atomic<bool> running;
    void initLogging();
};