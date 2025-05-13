#pragma once

#include <exception>
#include <string>

class StyxException : public std::exception {
public:
    explicit StyxException(const char* message) : std::exception(message) {}
    explicit StyxException(const std::string& message) : std::exception(message.c_str()) {}
};

