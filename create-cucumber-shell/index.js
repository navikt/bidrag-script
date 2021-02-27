const core = require("@actions/core");
const exec = require("@actions/exec");

async function run() {
  try {
    // Generate shell script with createCucumberShell.sh
    await exec.exec(
        `${__dirname}/../createCucumberShell.sh`,
        [
          'cucumber_tag=' + core.getInput('cucumber_tag'),
          'do_not_fail=' + core.getInput('do_not_fail'),
          'final_shell_name=' + core.getInput('final_shell_name'),
          'maven_goal=' + core.getInput('maven_goal'),
          'optional_maven_goal=' + core.getInput('optional_maven_goal'),
          'relative_json_path=' + core.getInput('relative_json_path'),
          'user=' + core.getInput('user')
        ]
    );
  } catch (error) {
    core.setFailed(error.message);
  }
}

// noinspection JSIgnoredPromiseFromCall
run();
