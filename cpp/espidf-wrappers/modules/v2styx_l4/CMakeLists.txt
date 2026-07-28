# Get the absolute path to your original library
set(ORIGINAL_LIB_DIR "${CMAKE_CURRENT_LIST_DIR}/../../../modules/l4/")

idf_component_register(
    SRCS 
        "${ORIGINAL_LIB_DIR}/src/ChannelRx.cpp"
        "${ORIGINAL_LIB_DIR}/src/ChannelTx.cpp"
        "${ORIGINAL_LIB_DIR}/src/uart/ChannelUart.cpp"
        "${ORIGINAL_LIB_DIR}/src/esp32/ChannelEsp32Uart.cpp"
    INCLUDE_DIRS "${ORIGINAL_LIB_DIR}/include"
    REQUIRES esp_driver_uart esp_driver_gpio v2styx_c
)