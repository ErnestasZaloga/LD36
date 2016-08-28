#!/bin/sh

# RUN THIS SCRIPT IN CASE PACKER HANGS UP ON CERTAIN PACK
cd "`dirname "$0"`"
java -Xmx1024m -jar app.jar