Import("env")
from os.path import join, realpath

is_ch32v = env.get("PIOPLATFORM") == "ch32v" or "ch32v" in env.get("BOARD_MCU", "").lower()
is_rp2040 = env.get("PIOPLATFORM") == "rp2040" or "rp2040" in env.get("BOARD_MCU", "").lower()

sources = ["+<ChannelTX.cpp>", "+<ChannelRX.cpp>", "+<uart/ChannelUart.cpp>"]

if is_ch32v:
    sources.append("+<ch32v/ChannelCh32Uart.cpp>")

if is_rp2040:
    sources.append("+<styxpico/ChannelUsbUart.cpp>")

env.Replace(SRC_FILTER=sources)
