# - Try to find gflags
# Once done, this will define
#
#  GFlags_FOUND - system has gflags
#  GFlags_INCLUDE_DIRS - the gflags include directories
#  GFlags_LIBRARIES - link these to use gflags

# Find header and lib
find_path(GFlags_INCLUDE_DIR NAMES gflags/gflags.h)
find_library(GFlags_LIBRARIES NAMES gflags)

include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(GFLAGS_FOUND GFlags_INCLUDE_DIR GFlags_LIBRARIES)
set(GFLAGS_INCLUDE_DIRS ${GFlags_INCLUDE_DIRS})
set(GFLAGS_LIBRARIES ${GFlags_LIBRARIES})
