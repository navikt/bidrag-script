import java.util.stream.Collectors

if (args.size < 4) {
    println(
        """
        Usage: createIntegrationInput.kts </path/tp/azure/inputs> <environment> </path/to/nais/folder> <test user name> 
               1) path to where azure json files is stored
               2) environment to use (main or feature)
               3) path to where nais apps with nais folders are stored
               4) username of a test user (ie: z......, ex: z992903)
        """.trimMargin()
    )

    throw IllegalArgumentException("Invalid number of parameters provided to script!")
}

val jsonPath = args[0]
val environment = args[1]
val naisProjectFolder = java.io.File(args[2])
val userTest = args[3]

if (!java.io.File(jsonPath).exists()) {
    throw IllegalArgumentException("Path (arg 1: $jsonPath) do not exist!")
}

if (!naisProjectFolder.exists()) {
    throw IllegalArgumentException("Path (arg 3: $naisProjectFolder) do not exist!")
}

fun createAzureInputs(jsonPath: String): List<java.io.File> {
    val resourcesPath = java.nio.file.Paths.get(jsonPath)
    val jsonFiles = java.nio.file.Files.walk(resourcesPath)
        .filter { item -> java.nio.file.Files.isRegularFile(item) }
        .filter { item ->item.toString().endsWith(".json") }
        .map { item -> java.io.File(item.toString()) }

    return jsonFiles.collect(Collectors.toList())
}

val integrationInput = """
        {
            "azureInputs": [
                ${createAzureInputs(jsonPath).joinToString(separator = ",\n") { it.readText(Charsets.UTF_8) }.trim()}
            ],
            "environment": "$environment",
            "naisProjectFolder": "$naisProjectFolder",
            "userTest": "$userTest"
        }
        """.trimIndent()

val integrationInputJson = java.io.File(jsonPath, "integrationInput.json")
integrationInputJson.writeText("$integrationInput\n", Charsets.UTF_8)
println("created $integrationInputJson")
