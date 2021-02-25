import java.io.File

if (args.size < 2) {
    throw IllegalStateException(
        """
            ERROR!
            Usage: produceCucumberShell.kts [delimiter] [args]
              1) the delimiter which separates each argument  - ex: ,
              2) all the arguments separated by the delimiter - ex: arg1,arg2,arg3,arg4,arg5
              ---------
              -  args: ${args.joinToString(" - ")}
              ---------
        """.trimIndent()
    )
}

val delimiter = args[0]
val inputs = args[1].split(delimiter)

if (inputs.size < 5) {
    throw java.lang.IllegalStateException(
        """
            ERROR by arguments separated by delimter (${args[0]}):
            expected args: true/false test json/integrationInput.json apps bidrag-app execute-cucumber.sh           
              1) if failure in the integration tests should produce build error, ex [true] or [false]
              2) the maven command to run, ex [test]
              3) the nav user running the integration tests, ex [j104364]
              4) the relative path to input json file, ex [json/integrationInput.json]
              5) the name of the file to produce, ex [execute-cucumber.sh]
              6) the optional cucumber tag to run, will default to "not @ignored"
              ---------
              -  args: ${args.joinToString(" - ")}
              ---------
        """.trimIndent()
    )
}

println("Using arguments: ${inputs.joinToString(" - ")}")

val skipMavenFailures = if (inputs[0].toBoolean()) {
    " -Dmaven.test.failure.ignore=true"
} else {
    println("Will fail if integration tests have errors!!!")
    ""
}

val mavenGoal = inputs[1]
val userName = inputs[2]
val relativeJsonPath = inputs[3]
val cucumberShellName = inputs[4]

val cucumberTag = if (inputs.size > 5) {
    "@${inputs[5]} and not @ignored"
} else {
    "not @ignored"
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

val workspace = System.getenv()["RUNNER_WORKSPACE"] ?: throw IllegalStateException("Unable to fetch RUNNER_WORKSPACE")
val executeCucumberShell = File(workspace, cucumberShellName)
executeCucumberShell.writeText("$envCucumberFilterTags\nmvn $mavenGoal $mavenArguments $authentication\n", Charsets.UTF_8)

println("created $executeCucumberShell")
