const core = require("@actions/core");
const exec = require("@actions/exec");

async function run() {
  try {
    const delimiter = core.getInput('delimiter')
    const delimited_arguments = core.getInput('delimited_arguments')
    const generator = core.getInput('generator')
    const generated_shell = core.getInput('generated_shell')

    core.info(
        "Arguments to index.js: " + [
          delimiter, delimited_arguments, generator, generated_shell
        ]
    )

    // Generate shell script with generate.sh (uses generator)
    await exec.exec(
        `${__dirname}/../generate.sh`,
        [generator, delimiter, delimited_arguments, generated_shell]
    );
  } catch (error) {
    core.setFailed(error.message);
  }
}

// noinspection JSIgnoredPromiseFromCall
run();
