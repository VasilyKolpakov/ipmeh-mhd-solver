#!/bin/bash
rm solver-jar-with-dependencies.jar
cd ../
mvn clean install assembly:single
