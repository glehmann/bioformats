FIND_PACKAGE(JAVA)
WRAP_ITK_INSTALL("/Python/itkExtras/bioformats" "${CMAKE_CURRENT_SOURCE_DIR}/bio-formats.jar")
WRAP_ITK_INSTALL("/Python/itkExtras/bioformats" "${CMAKE_CURRENT_SOURCE_DIR}/loci_tools.jar")
WRAP_ITK_INSTALL("/Python/itkExtras/bioformats" "${CMAKE_CURRENT_BINARY_DIR}/SimpleImageConverter.class")

ADD_CUSTOM_COMMAND(
  OUTPUT "${CMAKE_CURRENT_BINARY_DIR}/SimpleImageConverter.class"
  DEPENDS "${CMAKE_CURRENT_SOURCE_DIR}/loci_tools.jar"
    "${CMAKE_CURRENT_SOURCE_DIR}/bio-formats.jar"
    "${CMAKE_CURRENT_SOURCE_DIR}/SimpleImageConverter.java"
  COMMAND ${JAVA_COMPILE}
  ARGS -classpath "${CMAKE_CURRENT_SOURCE_DIR}/loci_tools.jar:${CMAKE_CURRENT_SOURCE_DIR}/bio-formats.jar" -d "${CMAKE_CURRENT_BINARY_DIR}" "${CMAKE_CURRENT_SOURCE_DIR}/SimpleImageConverter.java"
)

ADD_CUSTOM_TARGET(SimpleImageConverter ALL DEPENDS "${CMAKE_CURRENT_BINARY_DIR}/SimpleImageConverter.class")
