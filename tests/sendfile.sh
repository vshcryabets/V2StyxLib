#!/bin/bash

exec 3<>/dev/tcp/127.0.1.1/8080
cat t1 >&3

