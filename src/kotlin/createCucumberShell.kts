import java.io.File

if (args.size == 0) {
    throw IllegalStateException(
        """
            ERROR!
            Usage: createCucumberShell.kts [mapped args]
              - all the arguments to this script as key=value - ex: <arg1=value1 arg2=value2 arg3=value3 ... argX=valueX>
        """.trimIndent()
    )
}

val allArgs = args.joinToString(",")
val inputs = allArgs.replace(',', ' ').split(Regex(" "))
val commands: MutableMap<String, String> = HashMap()

inputs
    .filter { it.contains('=') }
    .filter { !it.endsWith('=') }
    .forEach {
        val key = it.split("=")[0]
        val value = it.split("=")[1]
        commands[key] = value
    }

val cucumber_github_project = "cucumber_github_project"
val cucumber_tag = "cucumber_tag"
val do_not_fail = "do_not_fail"
val final_shell_file = "final_shell_file"
val maven_goal = "maven_goal"
val optional_maven_goal = "optional_maven_goal"
var relative_json_path = "relative_json_path"
val user = "user"

println("Using arguments: $allArgs")

val cucumberGithubProcect = commands[cucumber_github_project] ?: throw exception("Missing required argument: $cucumber_github_project!")
val cucumberShellName = commands[final_shell_file] ?: throw exception("Missing required argument: $final_shell_file!")
val suppressFailures = commands[do_not_fail]?.toBoolean() ?: throw exception("Missing required argument: $do_not_fail, args $allArgs")
val jsonPath = commands[relative_json_path] ?: throw exception("Missing required argument: $relative_json_path, args: $allArgs")
val runMavenGoal = commands[maven_goal] ?: throw exception("Missing required argument: $maven_goal, args: $allArgs")
val userName = commands[user] ?: println("No user found among the arguments")

val cucumberTags = if (commands.containsKey(cucumber_tag)) {
    "@${commands[cucumber_tag]} and not @ignored"
} else {
    "not @ignored"
}

val skipMavenFailures = if (suppressFailures) {
    " -Dmaven.test.failure.ignore=true"
} else {
    println("Will fail if integration tests have errors!!!")
    ""
}

val envCucumberFilterTags = "export CUCUMBER_FILTER_TAGS=\"$cucumberTags\""
val mavenArguments = "-e -DUSERNAME=$userName -DINTEGRATION_INPUT=$jsonPath$skipMavenFailures"
val authentication = "-DUSER_AUTH=\$USER_AUTHENTICATION -DTEST_AUTH=\$TEST_USER_AUTHENTICATION -DPIP_AUTH=\$PIP_USER_AUTHENTICATION"
val optionalMvnGoal = if (commands.containsKey(optional_maven_goal)) {
    "mvn ${commands[optional_maven_goal]}"
} else {
    ""
}

val workspace = System.getenv()["RUNNER_WORKSPACE"] ?: throw IllegalStateException("Unable to fetch RUNNER_WORKSPACE")
val executeCucumberShell = File(workspace, cucumberShellName)
val shellContent = """
      cd $workspace/$cucumberGithubProcect || exit 1
      $envCucumberFilterTags
      mvn $runMavenGoal $mavenArguments $authentication
      $optionalMvnGoal
    """.trimIndent() + "\n"

println(shellContent.trim())
executeCucumberShell.writeText(shellContent, Charsets.UTF_8)
println("created $executeCucumberShell")

fun exception(detailedArgumentMessage: String) = IllegalArgumentException(fetchErrorMsg(detailedArgumentMessage))

fun fetchErrorMsg(detailedArgumentMsg: String) = """
           ERROR!
           $detailedArgumentMsg
              - $cucumber_github_project=<name of project>     : the name of the github project where the cucumber tests to run
              - $cucumber_tag=<cucumber tag> (optional)        : cucumber tag to run along with "not @ignored", (will default to "not @ignored")>,
              - $do_not_fail=true/false                        : if failure in the integration tests, produce build error, ex [true] or [false] if not
              - $final_shell_file=execute-cucumber.sh          : the name of the file to produce, ex [execute-cucumber.sh]
              - $maven_goal=test                               : the maven goal to run, ex [test]
              - $optional_maven_goal=<goal> (optional)         : an optional goal to perform after running the cucumber tests
              - $relative_json_path=json/integrationInput.json : the relative path to input json file, ex [json/integrationInput.json]
              - $user=<username> (optional)                    : the nav user running the integration tests, ex [j104364]
              ---------
              -  args: $allArgs
              ---------
        """.trimIndent()
