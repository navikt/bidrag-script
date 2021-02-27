const core = require("@actions/core");
const exec = require("@actions/exec");

async function run() {
  try {
    const cucumberTag = 'cucumber_tag=' + core.getInput('cucumber_tag');
    const doNotFail = 'do_not_fail=' + core.getInput('do_not_fail');
    const githubProjectName = core.getInput('github_project_name');
    const finalShellFile = core.getInput('final_shell_file');
    const finalShellFileAsMap = 'final_shell_file==' + finalShellFile;
    const mavenGoal = 'maven_goal=' + core.getInput('maven_goal');
    const user = 'user=' + core.getInput('user');

    const optionalMavenGoal = 'optional_maven_goal=' + core.getInput(
        'optional_maven_goal'
    );

    const relativeJsonPath = 'relative_json_path=' + core.getInput(
        'relative_json_path'
    );

    const mappedAndSpecificArguments = [
      cucumberTag + ',' + doNotFail + ',' + finalShellFileAsMap + ','
      + mavenGoal + ',' + optionalMavenGoal + ',' + relativeJsonPath + ','
      + user,
      githubProjectName,
      finalShellFile
    ];

    console.log("Arguments to shell script: " + mappedAndSpecificArguments);

    // Run createCucumberShell.sh with mapped arguments as well as specific
    await exec.exec(
        `${__dirname}/../createCucumberShell.sh`, mappedAndSpecificArguments
    );
  } catch (error) {
    core.setFailed(error.message);
  }
}

// noinspection JSIgnoredPromiseFromCall
run();
