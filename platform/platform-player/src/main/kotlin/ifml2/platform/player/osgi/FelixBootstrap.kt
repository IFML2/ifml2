package ifml2.platform.player.osgi

import org.slf4j.LoggerFactory
import org.osgi.framework.launch.Framework
import org.osgi.framework.BundleContext
import org.apache.felix.framework.FrameworkFactory
import org.osgi.framework.Constants
import org.apache.felix.framework.util.FelixConstants
import org.osgi.framework.Bundle
import org.osgi.framework.wiring.FrameworkWiring
import java.util.concurrent.CountDownLatch
import org.osgi.framework.wiring.BundleRevision
import org.osgi.framework.BundleException
import java.io.File
import java.net.URL
import java.net.MalformedURLException
import org.osgi.framework.FrameworkListener

class FelixBootstrap {
    private val logger = LoggerFactory.getLogger(FelixBootstrap::class.java)

    private val extraPackages = "org.slf4j"

        lateinit var framework: Framework

    val bundleContext: BundleContext
        get() = framework.bundleContext

    fun start() {
        logger.info("Starting framework...")
        try {
            val configMap = hashMapOf<Any, Any>()
            prepareConfig(configMap)
            val frameworkFactory = FrameworkFactory()
            framework = frameworkFactory.newFramework(configMap)
            framework.init()
            framework.start()
            logger.info("Framework started...")
        } catch (ex: Exception) {
            logger.error("Error while starting", ex)
        }
    }

    fun isFragmentBundle(bundle: Bundle): Boolean {
        val bundleRevision = bundle.adapt(BundleRevision::class.java)
        return bundleRevision != null && bundleRevision.types and BundleRevision.TYPE_FRAGMENT != 0
    }

    fun startBundle(bundle: Bundle) {
        logger.info("Starting bundle...")
        if (isFragmentBundle(bundle)) {
            logger.warn("Bundle is Fragment Bundle, won't start bundle {}", bundle)
            return;
        }
        try {
            bundle.start()
        } catch (ex: BundleException) {
            logger.error("Unsupported bundle", ex)
        } catch (ex: Exception) {
            logger.error("Unknown error", ex)
        }
    }

    fun startBundles(bundles: List<Bundle>) {
        logger.info("Starting bundles...")
        bundles.forEach(this::startBundle)
        logger.info("Starting bundles... done")
    }

    fun installBundle(file: File): Bundle? {
        try {
            val url = URL("file", "", file.path).toString()
            return bundleContext.installBundle(url)
        } catch (ex: BundleException) {
            logger.error("Can't install bundle")
        } catch (ex: MalformedURLException) {
            logger.error("Unknown file location {}", file.path)
        }
        return null;
    }

    fun installBundles(files: List<File>): List<Bundle> {
        val list = arrayListOf<Bundle>()
        files.forEach {
            file ->
            val bundle = installBundle(file)
            if (bundle != null) {
                list.add(bundle)
            }
        }
        return list
    }

    fun refreshAndWait(refreshList: List<Bundle>) {
        logger.info("Starting refresh...")
        val frameworkWiring = framework.adapt(FrameworkWiring::class.java)
        val countDownLatch = CountDownLatch(1)
        frameworkWiring.refreshBundles(refreshList, FrameworkListener {
            countDownLatch.countDown()
        })
        logger.debug("Waiting for refresh to finish...")
        try {
            countDownLatch.await()
        } catch (ex: InterruptedException) {
            logger.error("Interrupted", ex)
        }
        logger.info("Finished refresh...")
    }

    fun waitForStop() {
        logger.info("Waiting for framework to stop...")
        try {
            framework.waitForStop(0)
            logger.info("Framework stopped...")
        } catch (ex: Exception) {
            logger.error("Error while waiting", ex)
        }
    }

    fun requestStop() {
        logger.info("Stopping framework...")
        try {
            framework.stop(0)
            logger.info("Framework stopped...")
        } catch (ex: Exception) {
            logger.error("Error during framework stop, ignored", ex)
        }
    }

    private fun prepareConfig(configMap: MutableMap<Any, Any>) {
        configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, extraPackages)
        configMap.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT)
        configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, arrayListOf<Any>())
    }

}
