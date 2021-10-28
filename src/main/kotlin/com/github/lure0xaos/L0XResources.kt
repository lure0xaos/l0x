@file:Suppress("unused")

package com.github.lure0xaos

import com.github.lure0xaos.L0XResources.toMap
import java.io.InputStreamReader
import java.net.*
import java.nio.charset.Charset
import java.nio.file.*
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile

private const val PROTOCOL_FILE: String = "file"
private const val PROTOCOL_JAR: String = "jar"
private const val EXT_PROPERTIES: String = "properties"

private val context = L0X::class

object L0XResources {

    private fun findResource(name: String) = context.java.getResource(name)

    fun getRootPath(): Path {
        val root: URL = findResource("")!!
        when {
            root.isProtocol(PROTOCOL_FILE) -> {
                var path: Path = root.toPath()
                (0..context.java.packageName.count { it == '.' }).forEach { _ ->
                    path = path.parent
                }
                return path
            }

            root.isProtocol(PROTOCOL_JAR) -> {
                val rootUrl = URL(
                    root.toExternalForm()
                        .removePrefix(root.protocol + ':')
                        .substringBeforeLast('!')
                        .substringBeforeLast('/')
                )
                when {
                    rootUrl.isProtocol(PROTOCOL_FILE) -> return rootUrl.toPath()
                }
            }
        }
        error("Unknown protocol ${root.protocol}")
    }

    private fun URL.relativeTo(base: URL): String =
        toExternalForm().removePrefix(base.toExternalForm() + '/')

    fun resolveFile(root: String, name: String, locale: Locale): String {
        val path = name.substringBeforeLast('/')
        val baseName = name.substringAfterLast('/')
        val resource: URL = resolveContext(root + path, baseName, locale)
        return if (resource.exists()) resource.relativeTo(getContextURL(root, locale) ?: return "") else ""
    }

    fun resolveContext(baseName: String, resource: String, locale: Locale): URL =
        getContextURL(baseName, locale)!!.resolve(resource)

    fun readProperties(resource: String, charset: Charset, locale: Locale): Properties =
        getContextURL(resource, EXT_PROPERTIES, locale)!!.readResourceBundle(charset).toProperties()

    fun readMap(resource: String): Map<String, String> =
        readMap(resource, Charsets.UTF_8, Locale.getDefault())

    fun readMap(resource: String, charset: Charset, locale: Locale): Map<String, String> =
        getContextURL(resource, EXT_PROPERTIES, locale)!!.readResourceBundle(charset).toMap()

    private fun ResourceBundle.toProperties() = Properties().also { properties: Properties ->
        keySet().forEach { properties[it] = getString(it) }
    }

    fun ResourceBundle.toMap(): Map<String, String> =
        mutableMapOf<String, String>().also { properties: MutableMap<String, String> ->
            keySet().forEach { properties[it] = getString(it) }
        }

    fun readResourceBundle(resource: String, charset: Charset, locale: Locale): ResourceBundle =
        getContextURL(resource, EXT_PROPERTIES, locale)!!.readResourceBundle(charset)

    fun getContextURL(baseName: String, locale: Locale): URL? = getContextURL(baseName, "", locale)

    fun getContextURL(baseName: String, suffix: String, locale: Locale): URL? {
        val control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT)
        return control.getCandidateLocales(baseName, locale)
            .map { control.toBundleName(baseName, it) }
            .map { if (suffix.isEmpty()) it else control.toResourceName(it, suffix) }
            .map(::findResource)
            .firstOrNull { it != null }
    }
}

fun URL.resolve(resource: String): URL {
    val url: String = toExternalForm()
    return URL(URL(if (url.endsWith('/')) url else "$url/"), resource)
}

fun URL.hasExtension(suffix: String): Boolean = toExternalForm().endsWith(suffix)

fun URL.exists(): Boolean {
    when {
        isProtocol(PROTOCOL_FILE) -> {
            return Files.exists(toPath())
        }

        isProtocol(PROTOCOL_JAR) -> {
            val urlConnection: URLConnection = this.openConnection()
            require(urlConnection is JarURLConnection)
            try {
                urlConnection.jarFile.close()
            } catch (e: Exception) {
                return false
            }
            return true
        }

        else -> {
            val urlConnection: URLConnection = this.openConnection()
            if (urlConnection is HttpURLConnection) {
                val connection: HttpURLConnection = urlConnection
                val responseCode: Int = connection.responseCode
                connection.disconnect()
                return HttpURLConnection.HTTP_OK == responseCode
            }
            error("Unknown protocol $protocol")
        }
    }
}

fun URL.getBaseName(): String = toExternalForm().substringAfterLast('/').substringBeforeLast('.')

fun URL.readResourceBundle(charset: Charset): ResourceBundle =
    InputStreamReader(openStream(), charset).use { PropertyResourceBundle(it) }

fun URL.readMap(charset: Charset): Map<String, String> =
    this.readResourceBundle(charset).toMap()

fun URL.getContextURL(locale: Locale): URL? {
    val base = toExternalForm()
    val baseName: String = if (base.contains('.')) base.substringBeforeLast('.') else base
    val suffix: String = if (base.contains('.')) base.substringAfterLast('.') else ""
    val control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT)
    return control.getCandidateLocales(baseName, locale)
        .map { control.toBundleName(baseName, it) }
        .map { if (suffix.isEmpty()) it else control.toResourceName(it, suffix) }
        .map(::URL)
        .firstOrNull(URL::exists)
}

fun URL.readFile(charset: Charset): String = readText(charset)

fun Path.writeFile(text: String, charset: Charset): Path = apply {
    Files.writeString(
        this, text, charset,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.DSYNC
    )
}

fun Path.recreateFolder() {
    require(!Files.exists(this) || Files.isDirectory(this)) { this }
    if (Files.exists(this))
        Files.walk(this).sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
    Files.createDirectories(this)
}

fun URL.listDirectory(filter: (URL, Boolean) -> Boolean): List<URL> =
    when {
        isProtocol(PROTOCOL_FILE) -> listFiles(filter)
        else -> {
            when {
                isProtocol(PROTOCOL_JAR) -> listJar(filter)
                else -> error("Unknown protocol $protocol")
            }
        }
    }

fun URL.copyDirectory(destination: Path) {
    when {
        isProtocol(PROTOCOL_FILE) -> copyFiles(destination)
        isProtocol(PROTOCOL_JAR) -> copyJar(destination)
        else -> error("Unknown protocol $protocol")
    }
}

fun ResourceBundle.toProperties(): Properties = Properties().also { properties ->
    keySet().forEach { properties[it] = getString(it) }
}

private fun URL.listFiles(filter: (URL, Boolean) -> Boolean): List<URL> =
    Files.find(toPath(), 1, { it, _ ->
        filter(it.toUrl(), Files.isDirectory(it))
    }).toList().map(Path::toUrl)

private fun Path.toUrl() = toUri().toURL()

private fun URL.listJar(filter: (URL, Boolean) -> Boolean): MutableList<URL> {
    val resString: String = toExternalForm()
    val jarPath: String = resString.removePrefix("$protocol:").substringBeforeLast('!')
    val jarUrl = URL(jarPath)
    val list: MutableList<URL> = mutableListOf()
    when {
        jarUrl.isProtocol(PROTOCOL_FILE) -> {
            val resPath = resString.substringAfterLast("!/")
            val jarFile = JarFile(jarUrl.toPath().toFile())
            jarFile.entries().forEach { jarEntry: JarEntry ->
                val entryName: String = jarEntry.name
                if (resPath != entryName && entryName.startsWith(resPath)
                    && entryName.count { it == '/' } == 1
                ) {
                    val fullUrl = URL("$PROTOCOL_JAR:$jarPath!/$entryName")
                    if (filter(fullUrl, jarEntry.isDirectory))
                        list += fullUrl
                }
            }
            jarFile.close()
            return list
        }

        else -> error("Unknown protocol $protocol")
    }
}

private inline fun <E> Enumeration<E>.forEach(function: (E) -> Unit) {
    for (e in this) function(e)
}

private fun URL.copyFiles(destination: Path) = toPath().also { path ->
    Files.walk(path).forEach {
        Files.copy(it, destination.resolve(path.relativize(it)), StandardCopyOption.REPLACE_EXISTING)
    }
}

private fun URL.copyJar(destination: Path) {
    val resString: String = toExternalForm()
    val jarUrl = URL(resString.removePrefix("$protocol:").substringBeforeLast('!'))
    val resPath = "${resString.substringAfterLast("!/")}/"
    when {
        jarUrl.isProtocol(PROTOCOL_FILE) -> {
            val jarFile = JarFile(jarUrl.toPath().toFile())
            for (jarEntry: JarEntry in jarFile.entries()) {
                val entryName: String = jarEntry.name
                if (entryName.startsWith(resPath)) {
                    val path: Path = destination.resolve(entryName.substring(resPath.length))
                    if (jarEntry.isDirectory) {
                        Files.createDirectory(path)
                    } else {
                        jarFile.getInputStream(jarEntry)
                            .use { Files.copy(it, path, StandardCopyOption.REPLACE_EXISTING) }
                    }
                }
            }
            jarFile.close()
        }

        else -> error("Unknown protocol $protocol")
    }
}

private fun URL.toPath(): Path =
    Paths.get(URLDecoder.decode(toExternalForm().removePrefix("$protocol:/"), Charsets.UTF_8))

private fun URL.isProtocol(protocol: String) = this.protocol == protocol
