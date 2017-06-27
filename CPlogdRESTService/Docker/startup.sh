#!/bin/bash
docker run -it -p 80:8080 -e JAVA_OPTIONS='-Xmx1g' cplogd