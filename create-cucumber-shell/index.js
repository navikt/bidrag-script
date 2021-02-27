const core = require("@actions/core");
const exec = require("@actions/exec");

async function run() {
  try {
    const cucumberTag = 'cucumber_tag=' + core.getInput('cucumber_tag');
    const doNotFail = 'do_not_fail=' + core.getInput('do_not_fail');
    const githubProjectName = core.getInput('github_project_name');
    const finalShellName = core.getInput('final_shell_name');
    const finalShellNameAsMap = 'final_shell_name=' + finalShellName;
    const mavenGoal = 'maven_goal=' + core.getInput('maven_goal');
    const user = 'user=' + core.getInput('user');

    const optionalMavenGoal = 'optional_maven_goal=' + core.getInput(
        'optional_maven_goal'
    );

    const relativeJsonPath = 'relative_json_path=' + core.getInput(
        'relative_json_path'
    );

    const argumentsAsMapForKotlinAndForExtraArgsForShell = [
      cucumberTag + ',' + doNotFail + ',' + finalShellNameAsMap + ','
      + mavenGoal + ',' + optionalMavenGoal + ',' + relativeJsonPath + ','
      + user,
      githubProjectName,
      finalShellName
    ];

    console.log(
        "Arguments to shell script: "
        + argumentsAsMapForKotlinAndForExtraArgsForShell
    );

    // Run createCucumberShell.sh with all arguments (runs createCucumberShell.kts)
    await exec.exec(`${__dirname}/../createCucumberShell.sh`,
        argumentsAsMapForKotlinAndForExtraArgsForShell
    );
  } catch (error) {
    core.setFailed(error.message);
  }
}

// noinspection JSIgnoredPromiseFromCall
run();
