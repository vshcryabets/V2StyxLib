Import("env")
from os.path import join, realpath

is_stm8 = env.get("PIOPLATFORM") == "ststm8" or "stm8" in env.get("BOARD_MCU", "").lower()

sources = ["+<arch.c>", "+<Channel_c.c>"]

if is_stm8:
    sources.append("+<ChannelUartStm8.c>")

env.Replace(SRC_FILTER=sources)
