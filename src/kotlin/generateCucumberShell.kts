import java.io.File

if (args.size < 2) {
    throw IllegalStateException(
        """
            ERROR!
            Usage: generateCucumberShell.kts [delimiter] [args]
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
              2) the maven goal to run, ex [test]
              3) the nav user running the integration tests, ex [j104364]
              4) the relative path to input json file, ex [json/integrationInput.json]
              5) the name of the file to produce, ex [execute-cucumber.sh]
              6) optional commands: 
                 - tag=<cucumber tag to run (will default to "not @ignored")>,
                 - opt.goal=<an optional goal to perform after running the cucumber tests>
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
val optionalArguments: MutableMap<String, String> = HashMap()

inputs.filter { it.contains( '=') }.forEach {
    println("splitting $it by '='")

    val key = it.split("=")[0]
    val value = it.split("=")[1]
    optionalArguments[key] = value
}

println("optional arguments - $optionalArguments")

val cucumberTag = if (optionalArguments.containsKey("tag")) {
    "@${optionalArguments["tag"]} and not @ignored"
} else {
    "not @ignored"
}

val envCucumberFilterTags = "export CUCUMBER_FILTER_TAGS=\"$cucumberTag\""
val mavenArguments = "-e -DUSERNAME=$userName -DINTEGRATION_INPUT=$relativeJsonPath$skipMavenFailures"
val authentication = "-DUSER_AUTH=\$USER_AUTHENTICATION -DTEST_AUTH=\$TEST_USER_AUTHENTICATION -DPIP_AUTH=\$PIP_USER_AUTHENTICATION"
val optionalMvnGoal = if (optionalArguments.containsKey("opt.goal")) {
    "mvn ${optionalArguments["opt.goal"]}"
} else {
    ""
}

println(
    """
      environment  : $envCucumberFilterTags
      maven args   : $mavenGoal $mavenArguments $authentication
      optional goal: $optionalMvnGoal 
    """.trimIndent()
)

val workspace = System.getenv()["RUNNER_WORKSPACE"] ?: throw IllegalStateException("Unable to fetch RUNNER_WORKSPACE")
val executeCucumberShell = File(workspace, cucumberShellName)
val shellContent =
    """
      $envCucumberFilterTags
      mvn $mavenGoal $mavenArguments $authentication
      $optionalMvnGoal
    """.trimIndent() + "\n"

executeCucumberShell.writeText(shellContent, Charsets.UTF_8)

println("created $executeCucumberShell")
