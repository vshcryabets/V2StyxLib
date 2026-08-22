#pragma once

namespace styxlib
{

    template <typename T>
    class ExpectedResult
    {
    private:
        T _value;
        ErrorCode _error;
    public:
        ExpectedResult(T value) : _value(value), _error(ErrorCode::Success) {}
        ExpectedResult(ErrorCode error) : _value(0), _error(error) {}
        T value() const { return _value; }
        ErrorCode error() const { return _error; }
        bool has_value() const { return _error == ErrorCode::Success; }
    };

    class Unexpected: public ExpectedResult<Size>
    {
    public:
        Unexpected(ErrorCode error) : ExpectedResult(error) {}
    };

    using SizeResult = ExpectedResult<Size>;
    
}