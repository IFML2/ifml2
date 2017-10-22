package ifml2.platform.core.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.felix.framework.FrameworkFactory;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.FrameworkWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSGiFwLoader {

    Logger logger = LoggerFactory.getLogger(OSGiFwLoader.class);

    // This is the OSGi Framework instance, the main thing
    // which this class creates and manages:
    private Framework fw = null;

    public OSGiFwLoader() {
        super();
    }

    /**
     * Starts the OSGi Framework.
     */
    public void start() {
        logger.info("Starting framework...");
        try {
            Map<Object,Object> configMap = new HashMap<>();
            prepareConfigMap(configMap);
            FrameworkFactory fwf = new FrameworkFactory();
            fw = fwf.newFramework(configMap);
            fw.init();
            fw.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to set up the configuration map - called by the start 
     * method.
     * 
     * @param configMap    The configuration map.
     */
    private void prepareConfigMap(Map<Object, Object> configMap) {
        // List the packages which shall be exported by the System Bundle.
        // Notes:
        // - example.set2appapi.api is only relevant for set2 bundles
        // - example.set2cmdapi.api is only relevant for set2 bundles
        //   (Remove them, if you don't need it.)
        configMap.put(
                Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
//                "org.osgi.framework, " +
                "example.set2appapi.api, " +
                "example.set2cmdapi.api, " +
                "org.slf4j");
        // Each time the OSGi Framework starts, it shall clean up its
        // storage. I decided to do that, so one gets a clean environment
        // each time.
        configMap.put(
                Constants.FRAMEWORK_STORAGE_CLEAN, 
                Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);

        // This is for application provided BundleActivators:
        List<Object> list = new ArrayList<>();
//      fillSystemBundleActivators(list);
        configMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);
    }

    /**
     * Tests whether a bundle is a fragment bundle.
     * 
     * @param bundle
     * @return
     */
    public boolean isFragmentBundle(Bundle bundle) {
        // This is the recommended approach since OSGi R4 V4.3.
        // For earlier versions, one can use Bundle.getHeaders() and
        // check whether the key "FRAGMENT_HOST" exists.
        BundleRevision br = bundle.adapt(BundleRevision.class);
        if (br!=null && (br.getTypes() & BundleRevision.TYPE_FRAGMENT)!=0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Calls Bundle.start for the given bundle.
     * The method tests, whether the bundle is a fragment bundle and does not
     * start it in that case.
     * 
     * @param bundle  A Bundle to start.
     */
    public void startBundle(Bundle bundle) {
        logger.info("Starting bundle...");
        if (isFragmentBundle(bundle)) {
            logger.warn("Bundle is a Fragment-Bundle, won't start bundle {}", bundle);
            return;
        }
        try {
            bundle.start();
        } catch (BundleException e) {
            logger.error("Error starting bundle", e);
        } catch (Exception e) {
            logger.error("Error starting bundle", e);
        }
    }

    /**
     * Calls Bundle.start for the given list of bundles.
     * The method tests, whether the bundle is a fragment bundle and does not
     * start it in that case.
     * 
     * @param bundles  A List of Bundles to start.
     */
    public void startBundles(List<Bundle> bundles) {
        logger.info("Starting bundles...");
        for (Bundle bundle : bundles) {
            logger.debug("Start={}", bundle);
            if (isFragmentBundle(bundle)) {
                logger.warn("Bundle is a Fragment-Bundle, won't start bundle {}", bundle);
                continue;
            }
            try {
                bundle.start();
                logger.info("Started={}", bundle);
            } catch (BundleException e) {
                logger.error("Error starting bundle", e);
            } catch (Exception e) {
                logger.error("Error starting bundle", e);
            }
        }
        logger.info("Starting bundles... done");
    }

    /**
     * Calls BundleContext.installBundle for the given file.
     * (The file is turned into a URL by prepending "file:" 
     * to the filename.) The Bundle instance as returned from
     * BundleContext.installBundle is returned. In case of an error,
     * the error is logged and null is returned.
     * Note that this method does not refresh bundles which may be 
     * needed for installing fragment bundles.
     * 
     * @param file The File which points to the bundle file to be installed.
     * @return Returns the Bundle instance which has been installed, or null 
     *         in case of errors.
     */
    public Bundle installBundle(File file) {
        Bundle bundle;
        try {
            BundleContext bc = fw.getBundleContext();
            String url = new URL("file", "", file.getPath()).toString();
            bundle = bc.installBundle(url);
        } catch (BundleException | MalformedURLException e) {
            logger.error("Error installing bundle", e);
            bundle = null;
        }
        return bundle;
    }

    /**
     * Calls BundleContext.installBundle for each file in the given List of Files.
     * (A file is turned into a URL by prepending "file:" 
     * to the filename.) A List of Bundles is returned matching the given 
     * List of Files. If a bundle could not be installed, that position
     * in the List of Bundles will be null.
     * Note that this method does not perform a bundle refresh which is 
     * needed for installing fragment bundles.
     * 
     * @param file The File which points to the bundle file to be installed.
     * @return Returns the Bundle instance which has been installed, or null 
     *         in case of errors.
     */
    public List<Bundle> installBundles(List<File> bundleFiles) {
        List<Bundle> bundles = new ArrayList<>();

        for (File file : bundleFiles) {
            Bundle bundle = null;
            try {
                String url = new URL("file", "", file.getPath()).toString();
                bundle = fw.getBundleContext().installBundle(url);
                logger.debug("Installed: {}", bundle);
            } catch (BundleException | MalformedURLException e) {
                logger.error("Error installing bundle", e);
            }
            bundles.add(bundle);
        }

        return bundles;
    }

    public void refreshAndWait(List<Bundle> refreshList) {
        logger.info("Starting refresh...");
        FrameworkWiring fww = fw.adapt(FrameworkWiring.class);
        final CountDownLatch cdl = new CountDownLatch(1);
        fww.refreshBundles(refreshList, new FrameworkListener() {
            @Override
            public void frameworkEvent(FrameworkEvent arg0) {
                cdl.countDown();
            }
        });
        logger.debug("Waiting for refresh to finish...");
        try {
            cdl.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted", e);
        }
        logger.info("Finished refresh...");
    }

    /**
     * Calls Framework.waitForStop(). Use this method to wait for the
     * Framework to stop.
     */
    public void waitForStop() {
        logger.info("Waiting for framework to stop...");
        try {
            fw.waitForStop(0);
            logger.info("Framework stopped...");
        } catch (Exception e) {
            logger.error("Error while waiting", e);
        }
    }

    /**
     * Tells the Framework to stop. Note that this method returns immediately.
     * Use waitForStop to actually wait for the stop to complete.
     */
    public void requestStop() {
        logger.info("Stopping framework...");
        try {
            fw.stop(0);
        } catch (BundleException e) {
            logger.error("Error during framework stop, ignored.", e);
        }
    }

    /**
     * Returns the BundleContext of the Framework. (This is the BundleContext
     * of the System Bundle.)
     * 
     * @return Returns a BundleContext instance of the System Bundle.
     */
    public BundleContext getBundleContext() {
        return fw.getBundleContext();
    }

}
