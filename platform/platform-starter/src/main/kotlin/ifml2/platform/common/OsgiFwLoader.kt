package ifml2.platform.common

import org.osgi.framework.launch.Framework
import org.apache.felix.framework.FrameworkFactory
import org.apache.felix.framework.util.FelixConstants
import org.osgi.framework.*
import org.osgi.framework.wiring.BundleRevision
import org.osgi.framework.wiring.FrameworkWiring
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.util.concurrent.CountDownLatch

class OsgiFwLoader() {
    private val logger = LoggerFactory.getLogger(OsgiFwLoader::class.java)
    private var fw: Framework? = null

    fun start() {
        logger.info("Starting framework...")
        val configMap = hashMapOf<String, Any>()
        prepareConfigMap(configMap)
        val fwf = FrameworkFactory()
        val nfw = fwf.newFramework(configMap)
        nfw.init()
        nfw.start()
        fw = nfw
    }

    private fun prepareConfigMap(configMap: MutableMap<String, Any>) {
        configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
                "javafx.application, " +
                "javafx.fxml, " +
                "javafx.event, " +
                "javafx.scene, " +
                "javafx.scene.control, " +
                "javafx.scene.layout, " +
                "javafx.scene.paint, " +
                "javafx.scene.text, " +
                "javafx.stage, " +
                "org.slf4j"
        )
        configMap.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT)
        configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, arrayListOf<Any>())
    }

    fun isFragmentBundle(bundle: Bundle): Boolean {
        val br = bundle.adapt(BundleRevision::class.java)
        return br != null && (br.types and BundleRevision.TYPE_FRAGMENT) != 0
    }

    fun startBundle(bundle: Bundle) {
        logger.info("Starting bundle...")
        if (isFragmentBundle(bundle)) {
            logger.warn("Bundle is a Fragment Bundle, won't starting bundle {}", bundle)
            return
        }
        try {
            bundle.start()
        } catch (ex: Exception) {
            logger.error("Error starting bundle", ex)
        }
    }

    fun startBundles(bundles: List<Bundle>) {
        bundles.forEach { startBundle(it) }
    }

    fun installBundle(file: File): Bundle? {
        try {
            val bc = fw?.getBundleContext()
            val url = URL("file", "", file.path).toString()
            return bc?.installBundle(url)
        } catch (ex: Exception) {
            logger.error("Error installing bundle", ex)
        }
        return null
    }

    fun installBundles(bundleFiles: List<File>): List<Bundle?> {
        return bundleFiles.map { installBundle(it) }
    }

    fun refreshAndWait(refreshList: List<Bundle>) {
        logger.info("Start refresh...")
        val fww = fw?.adapt(FrameworkWiring::class.java)
        val cdl = CountDownLatch(1)
        fww?.refreshBundles(refreshList, FrameworkListener {
            fun frameworkEvent(event: FrameworkEvent) {
                cdl.countDown()
            }
        })
        logger.debug("Waiting for refresh to finish...")
        try {
            cdl.await()
        } catch (ex: InterruptedException) {
            logger.error("Interrupted", ex)
        }
        logger.info("Finished refresh...")
    }

    fun waitForStop() {
        logger.info("Waiting for framework to stop...")
        try {
            fw?.waitForStop(0)
        } catch (ex: Exception) {
            logger.error("Error while waiting", ex)
        }
    }

    fun requestStop() {
        logger.info("Stopping framework...")
        try {
            fw?.stop(0)
        } catch (ex: BundleException) {
            logger.error("Error during framework stop, ignored.", ex)
        }
    }

    fun getBundleContext(): BundleContext? {
        return fw?.bundleContext
    }
}
