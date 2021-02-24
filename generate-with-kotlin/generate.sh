#!/bin/bash
set -e

############################################
#
# Følgende skjer i dette skriptet
# - kloner ut bidrag-script (enten branch som tilsvarer branch som bygges, eller main branch)
# - kaller scriptet med kotlin (ie: kotlinc -script src/kotlin/<arg1: script navn> <arg2: delimeter> <arg3:argliste separert med delimeter):
# - lager output av av fila som er generert (RUNNER_WORKSPACE/<arg4: generert script>)
#
############################################

if [[ $# > 2 ]]; then
  echo ::error:: "At least two arguments from the action is expected, name of the generator (kotlin script) and name of the generated shell script"
  exit 1
fi

INPUT_KOTLIN_SCRIPT=$1
INPUT_DELIMITER=$2
INPUT_DELIMITED_ARGS=$3
INPUT_GENERATED_SHELL=$4

cd "$RUNNER_WORKSPACE" || exit 1
sudo rm -rf "bidrag-scripts"
BRANCH="${GITHUB_REF#refs/heads/}"

if [[ "$BRANCH" != "main" ]]; then
  FEATURE_BRANCH=$BRANCH
  IS_SCRIPT_CHANGE=$(git ls-remote --heads $(echo "https://github.com/navikt/bidrag-scripts $FEATURE_BRANCH" | sed "s/'//g") | wc -l)

  if [[ $IS_SCRIPT_CHANGE -eq 1 ]]; then
    echo "Using feature branch: $FEATURE_BRANCH, cloning to $PWD"
    git clone --depth 1 --branch=$FEATURE_BRANCH https://github.com/navikt/bidrag-scripts
  else
    echo "Using /refs/heads/main, cloning to $PWD"
    git clone --depth 1 https://github.com/navikt/bidrag-scripts
  fi
else
  echo "Using /refs/heads/main, cloning to $PWD"
  git clone --depth 1 https://github.com/navikt/bidrag-scripts
fi

if [ -z $INPUT_DELIMTER ]; then
  kotlinc -script src/kotlin/$INPUT_KOTLIN_SCRIPT
else
  kotlinc -script src/kotlin/$INPUT_KOTLIN_SCRIPT $INPUT_DELIMITER $INPUT_DELIMITED_ARGS
fi

GENERATED_SHELL_FILE="$RUNNER_WORKSPACE/$INPUT_GENERATED_SHELL"

if [ ! -f $GENERATED_SHELL_FILE ]; then
  echo ::error:: "unable to find generated shell: $GENERATED_SHELL_FILE"
  exit 1
fi

echo ::set-output name=path_to_generated_shell::"$GENERATED_SHELL_FILE"
