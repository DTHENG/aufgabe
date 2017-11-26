# Aufgabe

## Table of Contents

* [Requirements](#requirements)
* [Running the project](#running-the-project)
* [Configuration](#configuration)
* [Authors](#authors)

## Requirements

* 1 Raspberry Pi 3
* 1 Breadboard (half)
* 5 12mm Tactile Switch Buttons
* Male to Female Jumper Wires

## Running the project

* Clone the repo
* Run `mvn clean package` from the project directory
* Run `java -jar target/aufgabe.jar`
* Go to [http://localhost:8080/](http://localhost:8080/)

## Configuration

* By default the `.jar` will use `/src/main/resources/configuration-default.json`
* To add your own configuration, create a new `.json` file in the __resources__ directory with the naming pattern `configuration-{your-config}.json`
    > __Note:__ In the `.gitignore` of the repo, all files in the __resources__ directory starting with `configuration-` will be ignored from the git source, with the exception of the default.
    
    
## Authors

* Daniel Thengvall