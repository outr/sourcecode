#!/usr/bin/env bash

sbt +clean +sourcecodeJS/publishSigned +sourcecodeJVM/publishSigned +sourcecodeNative/publishSigned sonatypeRelease