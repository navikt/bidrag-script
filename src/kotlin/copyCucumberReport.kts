import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime


val copyFrom = "copyFrom"
val project = "project"
val cucumberJson = "cucumberJson"
val errorMsg = """
            Usage: moveCucumberReports.kts [$copyFrom=<target>] [$project=<project destination>] [$cucumberJson=<from test>]
              - $copyFrom=target folder where to find the generated report, ex: bidrag-cucumber-backend/target/generated-report
              - $project=destination project where the report is comitted: ex: bidrag-dev
              - $cucumberJson=project path to the latest cucumber report, ex: bidrag-cucumber-backend/target/cucumber-report/cucumber.json
              ---------
              -  args: ${args.joinToString(" - ")}
              ---------
             """.trimIndent()

if (args.size < 3) {
    throw IllegalStateException("ERROR!\n$errorMsg")
}

val argumentMap = args.filter { it.contains('=') }.map { it.split("=")[0] to it.split("=")[1] }.toMap()

val moveFromTargetFolder = fetchArgument(copyFrom)
val projectWhereToMove = fetchArgument(project)
val latestCucumberJson = fetchArgument(cucumberJson)

println()

val fullPathToRunnerWorkspace = fetchEnvironment("RUNNER_WORKSPACE")
val fullPathToDocsLatest = "$fullPathToRunnerWorkspace/$projectWhereToMove/docs/latest"
val docsLatestFolder = File(fullPathToDocsLatest)

if (!docsLatestFolder.exists()) {
    throw IllegalStateException("$fullPathToDocsLatest er ikke del av filsystemet!")
}

val fullPathToGeneratedFolder = "$fullPathToRunnerWorkspace/$projectWhereToMove/docs/generated"

if (!File(fullPathToGeneratedFolder).exists()) {
    throw IllegalStateException("$fullPathToGeneratedFolder er ikke del av filsystemet!")
}

val fullPathToTargetFolder = "$fullPathToRunnerWorkspace/$moveFromTargetFolder"

if (!File(fullPathToTargetFolder).exists()) {
    throw IllegalStateException("$fullPathToTargetFolder er ikke del av filsystemet!")
}

val fullPathToCucumberJson = "$fullPathToRunnerWorkspace/$latestCucumberJson"
val cucumberJsonFile = File(fullPathToCucumberJson)

if (!cucumberJsonFile.exists()) {
    throw java.lang.IllegalStateException("$fullPathToCucumberJson er ikke del av filsystemet!")
}

println("Sletter eksisterende filer: $fullPathToDocsLatest")
deleteContentWithFolder(docsLatestFolder)
println("Kopierer $fullPathToTargetFolder/* til $fullPathToDocsLatest/.")
copyFiles(fullPathToTargetFolder, fullPathToDocsLatest)
println("Kopierer $fullPathToCucumberJson til $fullPathToDocsLatest/.")
Files.copy(cucumberJsonFile.toPath(), Path.of("$fullPathToDocsLatest/${cucumberJsonFile.name}"))

var fullPathToGeneratedDestinationFolder = "$fullPathToGeneratedFolder/${LocalDate.now()}"
val now = LocalDateTime.now()

if (File(fullPathToGeneratedDestinationFolder).exists()) {
    fullPathToGeneratedDestinationFolder = "$fullPathToGeneratedDestinationFolder.${now.hour}:${now.minute}:${now.second}"
}

println("Kopierer $fullPathToDocsLatest/* til $fullPathToGeneratedDestinationFolder/.")
copyFiles(fullPathToDocsLatest, fullPathToGeneratedDestinationFolder)

val fullPathToBidragDevJson = "$fullPathToRunnerWorkspace/bidrag-dev.json"
val bidragDevJson = """{"timestamp":"$now","foldername":"${File(fullPathToGeneratedDestinationFolder).name}"}\n"""

println("Lagrer resultatet fra dette skriptet i $fullPathToBidragDevJson")
val printWriter = PrintWriter(FileWriter(fullPathToBidragDevJson))
printWriter.write(bidragDevJson)
printWriter.close()

fun fetchArgument(argumentName: String) = argumentMap[argumentName] ?: throw IllegalStateException(
    "${errorMsg}\nArgument ikke oppgitt: '$argumentName=<verdi>'"
)

fun fetchEnvironment(variableName: String) = System.getenv()[variableName] ?: throw IllegalStateException("Ukjent miljøvariabel: $variableName")

fun deleteContentWithFolder(folder: File) {
    deleteContent(folder)
    folder.delete()
    println(" > Slettet $folder med innhold")
}

fun deleteContent(folder: File) {
    folder.listFiles().forEach {
        if (it.isFile) {
            it.delete()
        } else {
            deleteContent(it)
            it.delete()
        }

        print('.')
    }
}

fun copyFiles(targetPath: String, destinationPath: String) {
    val sourceDir = Paths.get(targetPath)
    val destinationDir = Paths.get(destinationPath)

    // copy files: target -> destination
    Files.walk(sourceDir).forEach {
        val destinationFilePath = destinationDir.resolve(sourceDir.relativize(it))
        Files.copy(it, destinationFilePath)
        print('.')
    }

    println(" > Kopiert $targetPath -> $destinationPath")
}
