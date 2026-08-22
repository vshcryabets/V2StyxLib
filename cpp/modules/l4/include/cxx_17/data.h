#pragma once

namespace styxlib
{
    class Unexpected
    {
    private:
        ErrorCode _error;
    public:
        explicit Unexpected(ErrorCode error) : _error(error) {}
        ErrorCode error() const { return _error; }
    };

    template <typename T>
    class ExpectedResult
    {
    private:
        T _value;
        ErrorCode _error;
    public:
        ExpectedResult(T &&value) : _value(std::move(value)), _error(ErrorCode::Success) {}
        ExpectedResult(const Unexpected& unexp) : _value(), _error(unexp.error()) {}
        T& value() { return _value; }
        const T& value() const { return _value; }
        ErrorCode error() const { return _error; }
        bool has_value() const { return _error == ErrorCode::Success; }
    };

    using SizeResult = ExpectedResult<Size>;
    
}