package ifml2.platform.tools

import java.io.File
import java.util.jar.JarFile
import java.util.Collections
import java.util.jar.Manifest

class Osgifier {
    var jarFileName = ""
    var osgiVersion = ""
    val exportPackages = sortedSetOf<String>()
    val ignorePackages = hashSetOf<String>()
    var longestCommonPackage: String? = null

    fun parseArgs(args: Array<String>) {
        jarFileName = args[0]
        osgiVersion = args[1]
    }

    fun run() {
        val jarFile = File(jarFileName)
        val jarf = JarFile(jarFile).use {
            val manifest = it.getManifest()
            val attrs = manifest.getMainAttributes()
            val jarEntries = it.entries()
            while (jarEntries.hasMoreElements()) {
                val je = jarEntries.nextElement()
                val jeName = je.name
                if (jeName.toLowerCase().endsWith(".class")) {
                    val pkgName = extractPkgName(jeName)
                    if (pkgName != null) {
                        exportPackages.add(pkgName)
                        updateLongestCommonPackage(pkgName)
                    }
                }
            }

            val m2 = Manifest()
            val a2 = m2.getMainAttributes()
            a2.putValue("Manifest-Version", "1.0")
            a2.putValue("Export-Package", exportPackages.joinToString(", "))
            a2.putValue("Bundle-Version", osgiVersion)
            a2.putValue("Bundle-Description", attrs.getValue("Implementation-Title"))
            a2.putValue("Bundle-Name", attrs.getValue("Implementation-Title"))
            a2.putValue("Bundle-SymbolicName", longestCommonPackage)
            a2.putValue("Bundle-Vendor", attrs.getValue("Implementation-Vendor"))
            m2.write(System.out)
        }
    }

    private fun updateLongestCommonPackage(pkgName: String) {
        if (longestCommonPackage == null) {
            longestCommonPackage = pkgName
            return;
        }
        val lcp = longestCommonPackage!!.split(".")
        val pkg = pkgName.split(".")

        val sb = StringBuilder()
        var index = 0
        while (index < lcp.size && index < pkg.size) {
            if (lcp[index].equals(pkg[index])) {
                if (index != 0) {
                    sb.append(".")
                }
                sb.append(pkg[index])
            } else {
                break
            }
            index++
        }
        longestCommonPackage = sb.toString()
    }

    private fun extractPkgName(className: String): String? {
        val lastSlashPos = className.lastIndexOf('/')
        return if (lastSlashPos < 0) {
            null
        } else {
            className.substring(0, lastSlashPos).replace('/', '.')
        }
    }
}

fun main(args: Array<String>) {
    val osgifier = Osgifier()
    osgifier.parseArgs(args)
    osgifier.run()
}
