#pragma once

namespace styxlib
{
    class ExpectedSizeResult
    {
    private:
        Size _value;
        ErrorCode _error;
    public:
        ExpectedSizeResult(Size value) : _value(value), _error(ErrorCode::Success) {}
        ExpectedSizeResult(ErrorCode error) : _value(0), _error(error) {}
        Size value() const { return _value; }
        ErrorCode error() const { return _error; }
        bool has_value() const { return _error == ErrorCode::Success; }
    };

    using SizeResult = ExpectedSizeResult;
}