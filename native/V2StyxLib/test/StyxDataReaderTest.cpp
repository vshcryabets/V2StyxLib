#include <ctime>

#include "gtest/gtest.h"
#include "io/StyxByteBufferReadable.h"
#include "io/StyxDataReader.h"

TEST(cpp_styx_data_reader_uint64_test, rw_test) {
	StyxByteBufferReadable buffer(16);
	uint8_t data[] = {8, 7, 6, 5, 4, 3, 2, 1};
	buffer.write(data, 8);
	buffer.write(data, 8);
	StyxDataReader reader(&buffer);
	ASSERT_EQ(0x0102030405060708L, reader.readUInt64()) << "Wrong read uint64";
	ASSERT_NE(0x1102030405060708L, reader.readUInt64()) << "Wrong read uint64";
}
