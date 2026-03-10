package tech.ula.model.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.ula.model.entities.App
import tech.ula.utils.* // ktlint-disable no-wildcard-imports
import java.io.File
import java.io.IOException
import java.util.Locale

class GithubAppsFetcher(
    private val filesDirPath: String,
    private val httpStream: HttpStream = HttpStream(),
    private val logger: Logger = SentryLogger()
) {

    // 本地assets目录路径
    private val localAssetsPath = "UserLAnd-Assets-Support/apps"

    // Allows destructing of the list of application elements
    private operator fun <T> List<T>.component6() = get(5)
    private operator fun <T> List<T>.component7() = get(6)

    @Throws(IOException::class)
    suspend fun fetchAppsList(): List<App> = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File("$localAssetsPath/apps.txt")
            val contents: List<String> = file.readLines()
            val numLinesToSkip = 1 // Skip first line which defines schema
            contents.drop(numLinesToSkip).map { line ->
                // Destructure app fields
                val (
                        name,
                        category,
                        filesystemRequired,
                        supportsCli,
                        supportsGui,
                        isPaidApp,
                        version
                ) = line.toLowerCase(Locale.ENGLISH).split(", ")
                // Construct app
                App(
                        name,
                        category,
                        filesystemRequired,
                        supportsCli.toBoolean(),
                        supportsGui.toBoolean(),
                        isPaidApp.toBoolean(),
                        version.toLong()
                )
            }
        } catch (err: Exception) {
            val exception = IOException("Error getting apps list")
            logger.addExceptionBreadcrumb(exception)
            throw exception
        }
    }

    suspend fun fetchAppIcon(app: App) = withContext(Dispatchers.IO) {
        val directoryAndFilename = "${app.name}/${app.name}.png"
        val sourceFile = File("$localAssetsPath/$directoryAndFilename")
        val targetFile = File("$filesDirPath/apps/$directoryAndFilename")
        sourceFile.copyTo(targetFile, overwrite = true)
    }

    suspend fun fetchAppDescription(app: App) = withContext(Dispatchers.IO) {
        val directoryAndFilename = "${app.name}/${app.name}.txt"
        val sourceFile = File("$localAssetsPath/$directoryAndFilename")
        val targetFile = File("$filesDirPath/apps/$directoryAndFilename")
        sourceFile.copyTo(targetFile, overwrite = true)
    }

    suspend fun fetchAppScript(app: App) = withContext(Dispatchers.IO) {
        val directoryAndFilename = "${app.name}/${app.name}.sh"
        val sourceFile = File("$localAssetsPath/$directoryAndFilename")
        val targetFile = File("$filesDirPath/apps/$directoryAndFilename")
        sourceFile.copyTo(targetFile, overwrite = true)
    }
}