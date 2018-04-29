#include "platformtools.h"

#include <sys/time.h>

uint64_t getTimestampInMilliseconds() {
	struct timeval tp;
	gettimeofday(&tp, NULL);
	uint64_t timestamp = (uint64_t) tp.tv_sec * 1000L + tp.tv_usec / 1000;
    return timestamp;
}
