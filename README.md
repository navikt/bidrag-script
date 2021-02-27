# bidrag-script

![build and test](https://github.com/navikt/bidrag-scripts/workflows/build%20and%20test/badge.svg)

scripts used in development by team bidrag and a bash script generator written as kotlin script

## release endringer

versjon | endringstype | beskrivelse
---|---|---
v1.1.0 | opprettet action | `create-cucumber-shell`: specialized action for cucumber scripting with default values
v1.0.1 | endret | `generate-with-kotlin`: `generate.sh` vil sette scriptet kjørbart med `chmod +x`
v1.0.0 | opprettet action | `generate-with-kotlin`: laget et shell script med gitt kotlin script 
