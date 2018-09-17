SCRIPTS_DIR := $(dir $(lastword $(MAKEFILE_LIST)))
DIR := $(SCRIPTS_DIR)../resources
OBJ_SOURCES := $(shell echo $(DIR)/*.obj)
TARGET := $(DIR)/db.json

OBJ2JSON_SCRIPT := $(SCRIPTS_DIR)obj2json.scm

all: $(TARGET)

$(TARGET):
	$(OBJ2JSON_SCRIPT) $(OBJ_SOURCES) > $(TARGET)

.PHONY: all
