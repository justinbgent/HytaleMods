import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.net.URI
import java.security.MessageDigest

/**
 * Custom Gradle plugin for automated Hytale server testing.
 * 
 * Usage:
 *   runHytale {
 *       jarUrl = "libs/HytaleServer.jar"
 *   }
 *   
 *   ./gradlew runServer
 */
open class RunHytalePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Create extension for configuration
        val extension = project.extensions.create("runHytale", RunHytaleExtension::class.java)

        // Register the runServer task
        val runTask: TaskProvider<RunServerTask> = project.tasks.register(
            "runServer", 
            RunServerTask::class.java
        ) {
            jarUrl.set(extension.jarUrl)
            group = "hytale"
            description = "Downloads and runs the Hytale server with your plugin"
        }

        // Make runServer depend on shadowJar (build plugin first)
        project.tasks.findByName("shadowJar")?.let {
            runTask.configure {
                dependsOn(it)
            }
        }
    }
}

/**
 * Extension for configuring the RunHytale plugin.
 */
open class RunHytaleExtension {
    var jarUrl: String = "libs/HytaleServer.jar"
}

/**
 * Task that downloads, sets up, and runs a Hytale server with the plugin.
 */
open class RunServerTask : DefaultTask() {

    @Input
    val jarUrl = project.objects.property(String::class.java)

    @TaskAction
    fun run() {
        // Create directories
        val runDir = File(project.projectDir, "run").apply { mkdirs() }
        val pluginsDir = File(runDir, "plugins").apply { mkdirs() }
        val jarFile = File(runDir, "server.jar")

        val serverJarPath = jarUrl.get()
        val sourceJar: File
        
        // Check if it's a URL or local file path
        if (serverJarPath.startsWith("http://") || serverJarPath.startsWith("https://")) {
            // It's a URL - download and cache it
            val cacheDir = File(
                project.layout.buildDirectory.asFile.get(), 
                "hytale-cache"
            ).apply { mkdirs() }

            // Compute hash of URL for caching
            val urlHash = MessageDigest.getInstance("SHA-256")
                .digest(serverJarPath.toByteArray())
                .joinToString("") { "%02x".format(it) }
            val cachedJar = File(cacheDir, "$urlHash.jar")

            // Download server JAR if not cached
            if (!cachedJar.exists()) {
                println("Downloading Hytale server from $serverJarPath")
                try {
                    URI.create(serverJarPath).toURL().openStream().use { input ->
                        cachedJar.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    println("Server JAR downloaded and cached")
                } catch (e: Exception) {
                    println("ERROR: Failed to download server JAR")
                    println("Make sure the jarUrl in build.gradle.kts is correct")
                    println("Error: ${e.message}")
                    return
                }
            } else {
                println("Using cached server JAR")
            }
            
            sourceJar = cachedJar
        } else {
            // It's a local file path - resolve it relative to project directory
            sourceJar = File(project.projectDir, serverJarPath)
            
            if (!sourceJar.exists()) {
                println("ERROR: Server JAR not found at: ${sourceJar.absolutePath}")
                println("Make sure the jarUrl in build.gradle.kts points to a valid file")
                return
            }
            
            println("Using local server JAR: ${sourceJar.absolutePath}")
        }

        // Copy server JAR to run directory
        sourceJar.copyTo(jarFile, overwrite = true)

        // Copy plugin JAR to plugins folder
        project.tasks.findByName("shadowJar")?.outputs?.files?.firstOrNull()?.let { shadowJar ->
            val targetFile = File(pluginsDir, shadowJar.name)
            shadowJar.copyTo(targetFile, overwrite = true)
            println("Plugin copied to: ${targetFile.absolutePath}")
        } ?: run {
            println("WARNING: Could not find shadowJar output")
        }

        println("Starting Hytale server...")
        println("Press Ctrl+C to stop the server")

        // Check if debug mode is enabled
        val debugMode = project.hasProperty("debug")
        val javaArgs = mutableListOf<String>()
        
        if (debugMode) {
            javaArgs.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
            println("Debug mode enabled. Connect debugger to port 5005")
        }
        
        javaArgs.addAll(listOf("-jar", jarFile.name))

        // Start the server process
        val process = ProcessBuilder("java", *javaArgs.toTypedArray())
            .directory(runDir)
            .start()

        // Handle graceful shutdown
        project.gradle.buildFinished {
            if (process.isAlive) {
                println("\nStopping server...")
                process.destroy()
            }
        }

        // Forward stdout to console
        Thread {
            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { println(it) }
            }
        }.start()

        // Forward stderr to console
        Thread {
            process.errorStream.bufferedReader().useLines { lines ->
                lines.forEach { System.err.println(it) }
            }
        }.start()

        // Forward stdin to server (for commands)
        Thread {
            System.`in`.bufferedReader().useLines { lines ->
                lines.forEach {
                    process.outputStream.write((it + "\n").toByteArray())
                    process.outputStream.flush()
                }
            }
        }.start()

        // Wait for server to exit
        val exitCode = process.waitFor()
        println("Server exited with code $exitCode")
    }
}
