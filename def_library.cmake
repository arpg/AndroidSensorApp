include(CMakeParseArguments)

function(def_library lib)

  string(TOUPPER ${lib} LIB)

  set(LIB_OPTIONS)
  set(LIB_SINGLE_ARGS)
  set(LIB_MULTI_ARGS SOURCES DEPENDS CONDITIONS LINK_LIBS)
  cmake_parse_arguments(lib
    "${LIB_OPTIONS}"
    "${LIB_SINGLE_ARGS}"
    "${LIB_MULTI_ARGS}"
    "${ARGN}"
    )

  if(NOT lib_SOURCES)
    message(FATAL_ERROR "def_library for ${LIB} has an empty source list.")
  endif()

  set(cache_var BUILD_${LIB})
  set(${cache_var} ON CACHE BOOL "Enable ${LIB} compilation.")

  set(build_type_cache_var LIB${LIB}_BUILD_TYPE)
  set(${build_type_cache_var} "" CACHE STRING
    "Target specific build configuration for lib${lib}")

  string(TOUPPER "${${build_type_cache_var}}" LIB_BUILD_TYPE)
  set(lib_flags "${CMAKE_CXX_FLAGS} ${CMAKE_C_FLAGS} ${CMAKE_CXX_FLAGS_${LIB_BUILD_TYPE}} ${CMAKE_C_FLAGS_${LIB_BUILD_TYPE}}")

  if(lib_CONDITIONS)
    foreach(cond ${lib_CONDITIONS})
      if(NOT ${cond})
	set(${cache_var} OFF)
	message("${cache_var} is false because ${cond} is false.")
	return()
      endif()
    endforeach()
  endif()

  if(lib_DEPENDS)
    foreach(dep ${lib_DEPENDS})
      string(TOUPPER ${dep} DEP)
      if(NOT TARGET ${dep})
	set(${cache_var} OFF)
	message("${cache_var} is false because ${dep} is not being built.")
	return()
      endif()
    endforeach()
  endif()

  if(${cache_var})
    add_library(${lib} ${lib_SOURCES})
    set_target_properties(${lib} PROPERTIES COMPILE_FLAGS ${lib_flags})

    if(lib_DEPENDS)
      target_link_libraries(${lib} ${lib_DEPENDS})
    endif()

    if(lib_LINK_LIBS)
      target_link_libraries(${lib} ${lib_LINK_LIBS})
    endif()
  endif()
endfunction()