const core = require("@actions/core");
const exec = require("@actions/exec");

async function run() {
  try {
    const delimiter = core.getInput('delimeter')
    const delimeted_arguments = core.getInput('delimeted_arguments')
    const generator = core.getInput('generator')
    const generated_shell = core.getInput('generated_shell')

    // Generate shell script with generate.sh (uses generator)
    await exec.exec(
        `${__dirname}/../generate.sh`,
        [generator, delimiter, delimeted_arguments, generated_shell]
    );
  } catch (error) {
    core.setFailed(error.message);
  }
}

// noinspection JSIgnoredPromiseFromCall
run();
