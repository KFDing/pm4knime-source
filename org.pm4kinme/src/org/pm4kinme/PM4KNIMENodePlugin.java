/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 *
 */
package org.pm4kinme;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.pm4kinme.external.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4kinme.external.connectors.prom.PM4KNIMEPackageDescriptor;
import org.pm4kinme.external.connectors.prom.PM4KNIMEPluginContext;
import org.pm4kinme.external.connectors.prom.PM4KNIMEPluginManager;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.plugin.PluginManager;

/**
 * This is the eclipse bundle activator.
 * Note: KNIME node developers probably won't have to do anything in here, 
 * as this class is only needed by the eclipse platform/plugin mechanism.
 * If you want to move/rename this file, make sure to change the plugin.xml
 * file in the project root directory accordingly.
 *
 * @author KNIME.com
 */
public class PM4KNIMENodePlugin extends Plugin {
    // The shared instance.
    private static PM4KNIMENodePlugin plugin;

    /**
     * The constructor.
     */
    public PM4KNIMENodePlugin() {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation.
     * 
     * @param context The OSGI bundle context
     * @throws Exception If this plugin could not be started
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        
        // to add similar stuff like RapidMiner 
        
        
    }

    
    /**
     * This method is called when the plug-in is stopped.
     * 
     * @param context The OSGI bundle context
     * @throws Exception If this plugin could not be stopped
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     * 
     * @return Singleton instance of the Plugin
     */
    public static PM4KNIMENodePlugin getDefault() {
        return plugin;
    }

}

