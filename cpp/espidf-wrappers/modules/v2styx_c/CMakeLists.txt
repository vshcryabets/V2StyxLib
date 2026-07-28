# Get the absolute path to your original library
set(ORIGINAL_LIB_DIR "${CMAKE_CURRENT_LIST_DIR}/../../../../c/l4/")

idf_component_register(
    SRCS 
        "${ORIGINAL_LIB_DIR}/src/arch.c"
        "${ORIGINAL_LIB_DIR}/src/Channel_c.c"
    INCLUDE_DIRS "${ORIGINAL_LIB_DIR}/include"
)