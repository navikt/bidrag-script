#!/bin/bash
set -x

############################################
#
# Følgende skjer i dette skriptet
# - kloner ut bidrag-script (enten branch som tilsvarer branch som bygges, eller main branch)
# - kaller scriptet med kotlin (ie: kotlinc -script src/kotlin/<arg1: script navn> <arg2: delimiter> <arg3:argliste separert med delimiter):
# - lager output av av fila som er generert (RUNNER_WORKSPACE/<arg4: generert script>)
#
############################################

if [[ $# < 2 ]]; then
  echo ::error:: "Usage: generate.sh <kotlin script> <delimiter for kotlin script> <delimited argument string for kotlin script> <generated shell>"
  echo ::error:: "At least two arguments from the action is expected, name of the generator (kotlin script) and name of the generated shell script"
  echo ::error:: "Args: $@"
  exit 1
fi

INPUT_KOTLIN_SCRIPT=$1
INPUT_DELIMITER=$2
INPUT_DELIMITED_ARGS=$3
INPUT_GENERATED_SHELL=$4

cd "$RUNNER_WORKSPACE" || exit 1

if [ ! -d bidrag-scripts/.git ]; then
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
else
  cd bidrag-scripts || exit 1
  git pull
fi

if [ -z $RUNNER_WORKSPACE ]; then
  echo ::error:: "No defined workspace for the github runner"
fi

KOTLIN_SCRIPT_SRC=$RUNNER_WORKSPACE/bidrag-scripts/src/kotlin/$INPUT_KOTLIN_SCRIPT

if [ -z $INPUT_DELIMITER ]; then
  kotlinc -script $KOTLIN_SCRIPT_SRC
else
  kotlinc -script $KOTLIN_SCRIPT_SRC $INPUT_DELIMITER $INPUT_DELIMITED_ARGS
fi

GENERATED_SHELL_FILE="$RUNNER_WORKSPACE/$INPUT_GENERATED_SHELL"

if [ ! -f $GENERATED_SHELL_FILE ]; then
  echo ::error:: "unable to find generated shell: $GENERATED_SHELL_FILE"
  exit 1
fi

chmod +x $GENERATED_SHELL_FILE
echo ::set-output name=path_to_generated_shell::"$GENERATED_SHELL_FILE"
