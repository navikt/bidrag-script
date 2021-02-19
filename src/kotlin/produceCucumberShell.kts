import java.io.File

var skipMavenFailures = "not set"
var mavenGoal = "not set"
var mavenImage = "not set"
var userName = "not set"
var relativeJsonPath = "not set"
var relativeAppPath = "not set"
var cucumberShellName = "not set"
var cucumberTag = "not @ignored"

if (args.size < 5) {
    throw IllegalStateException(
        """
        Error!
        Usage: produceCucumberShell.kts true/false mvn:image json/integrationInput.json apps bidrag-app execute-cucumber.sh
          1) if failure in the integration tests should produce build error, ex [true] or [false]
          2) the maven command to run, ex [test]
          3) the docker maven image to run, ex [maven:3.6.3-openjdk-15]
          4) the nav user running the integration tests, ex [j104364]
          5) the relative path to input json file, ex [json/integrationInput.json]
          6) the name of the file to produce, ex [execute-cucumber.sh]
          7) the optional cucumber tag to run, will default to "not @ignored"
          ---------
        """.trimIndent()
    )
}

skipMavenFailures = if (args[0].toBoolean()) {
    " -Dmaven.test.failure.ignore=true"
} else {
    println("Will fail if integration tests have errors!!!")
    ""
}

mavenGoal = args[1]
mavenImage = args[2]
userName = args[3]
relativeJsonPath = args[4]
cucumberShellName = args[5]

if (args.size > 6) {
    cucumberTag = "@${args[6]} and not @ignored"
}

var dockerEnvironment = "-e CUCUMBER_FILTER_TAGS=\"$cucumberTag\""
var dockerArguments = "$dockerEnvironment --rm -v \$PWD:/usr/src/mymaven -v \$HOME/.m2:/root/.m2 -w /usr/src/mymaven $mavenImage mvn"
var mavenArguments = "-e -DUSERNAME=$userName -DINTEGRATION_INPUT=$relativeJsonPath$skipMavenFailures"
var authentication = "-DUSER_AUTH=\$USER_AUTHENTICATION -DTEST_AUTH=\$TEST_USER_AUTHENTICATION -DPIP_AUTH=\$PIP_USER_AUTHENTICATION"

println(
    """
      docker run: $dockerArguments $mavenGoal
      maven args: $mavenArguments $authentication
    """.trimIndent()
)

val workspace = System.getenv()["GITHUB_WORKSPACE"] ?: throw IllegalStateException("Unable to fetch GITHUB_WORKSPACE")
val executeCucumberShell = File(workspace, cucumberShellName)
executeCucumberShell.writeText("docker run $dockerArguments $mavenGoal $mavenArguments\n", Charsets.UTF_8)
println("created $executeCucumberShell")
