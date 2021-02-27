const core = require("@actions/core");
const exec = require("@actions/exec");

async function run() {
  try {
    const cucumberTag = 'cucumber_tag=' + core.getInput('cucumber_tag');
    const doNotFail = 'do_not_fail=' + core.getInput('do_not_fail');
    const mavenGoal = 'maven_goal=' + core.getInput('maven_goal');
    const user = 'user=' + core.getInput('user');

    const finalShellName = 'final_shell_name=' + core.getInput(
        'final_shell_name'
    );

    const optionalMavenGoal = 'optional_maven_goal=' + core.getInput(
        'optional_maven_goal'
    );

    const relaticeJsonPath = 'relative_json_path=' + core.getInput(
        'relative_json_path'
    );

    // Run createCucumberShell.sh with all arguments (runs createCucumberShell.kts)
    await exec.exec(
        `${__dirname}/../createCucumberShell.sh`,
        [
          cucumberTag, doNotFail, finalShellName, mavenGoal, optionalMavenGoal,
          relaticeJsonPath, user
        ]
    );
  } catch (error) {
    core.setFailed(error.message);
  }
}

// noinspection JSIgnoredPromiseFromCall
run();
