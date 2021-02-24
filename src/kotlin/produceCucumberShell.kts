import java.io.File

var skipMavenFailures = "not set"
var mavenGoal = "not set"
var userName = "not set"
var relativeJsonPath = "not set"
var relativeAppPath = "not set"
var cucumberShellName = "not set"
var cucumberTag = "not @ignored"

if (args.size < 4) {
    throw IllegalStateException(
        """
        Error!
        Usage: produceCucumberShell.kts true/false mvn:image json/integrationInput.json apps bidrag-app execute-cucumber.sh
          1) if failure in the integration tests should produce build error, ex [true] or [false]
          2) the maven command to run, ex [test]
          3) the nav user running the integration tests, ex [j104364]
          4) the relative path to input json file, ex [json/integrationInput.json]
          5) the name of the file to produce, ex [execute-cucumber.sh]
          6) the optional cucumber tag to run, will default to "not @ignored"
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
userName = args[2]
relativeJsonPath = args[3]
cucumberShellName = args[4]

if (args.size > 5) {
    cucumberTag = "@${args[5]} and not @ignored"
}

var envCucumberFilterTags = "CUCUMBER_FILTER_TAGS=\"$cucumberTag\""
var mavenArguments = "-e -DUSERNAME=$userName -DINTEGRATION_INPUT=$relativeJsonPath$skipMavenFailures"
var authentication = "-DUSER_AUTH=\$USER_AUTHENTICATION -DTEST_AUTH=\$TEST_USER_AUTHENTICATION -DPIP_AUTH=\$PIP_USER_AUTHENTICATION"

println(
    """
      environment: $envCucumberFilterTags
      maven args : $mavenGoal $mavenArguments $authentication
    """.trimIndent()
)

val workspace = System.getenv()["GITHUB_WORKSPACE"] ?: throw IllegalStateException("Unable to fetch GITHUB_WORKSPACE")
val executeCucumberShell = File(workspace, cucumberShellName)
executeCucumberShell.writeText("$envCucumberFilterTags\nmvn $mavenGoal $mavenArguments\n", Charsets.UTF_8)

println("created $executeCucumberShell")
