Import("env")
from os.path import join, realpath

is_stm8 = env.get("PIOPLATFORM") == "ststm8" or "stm8" in env.get("BOARD_MCU", "").lower()
is_ch32v = env.get("PIOPLATFORM") == "ch32v" or "ch32v" in env.get("BOARD_MCU", "").lower()

sources = ["+<arch.c>", "+<Channel_c.c>"]

if is_stm8:
    sources.append("+<ChannelUartStm8.c>")
if is_ch32v:
    sources.append("+<ChannelUartCh32v.c>")

env.Replace(SRC_FILTER=sources)
