#set( $symbol_pound = '#' )

package ${package}.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import org.apache.felix.dm.Component;
import ${package}.ISimple;
import org.opendaylight.controller.sal.core.ComponentActivatorAbstractBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator extends ComponentActivatorAbstractBase {
    protected static final Logger log = LoggerFactory.getLogger(Activator.class);

    /**
     * Function called when the activator starts just after some initializations
     * are done by the ComponentActivatorAbstractBase.
     *
     */
    @Override
    public void init() {
    }

    /**
     * Function called when the activator stops just before the cleanup done by
     * ComponentActivatorAbstractBase
     *
     */
    @Override
    public void destroy() {
    }

    /**
     * Function that is used to communicate to dependency manager the list of
     * known implementations for services inside a container
     *
     *
     * @return An array containing all the CLASS objects that will be
     *         instantiated in order to get an fully working implementation
     *         Object
     */
    @Override
    public Object[] getGlobalImplementations() {
        Object[] res = { Simple.class };
        return res;
    }

    /**
     * Function that is called when configuration of the dependencies is
     * required.
     *
     * @param c
     *            dependency manager Component object, used for configuring the
     *            dependencies exported and imported
     * @param imp
     *            Implementation class that is being configured, needed as long
     *            as the same routine can configure multiple implementations
     * @param containerName
     *            The containerName being configured, this allow also optional
     *            per-container different behavior if needed, usually should not
     *            be the case though.
     */
    @Override
    public void configureGlobalInstance(Component c, Object imp) {
        if (imp.equals(Simple.class)) {
            Dictionary<String, Set<String>> props = new Hashtable<String, Set<String>>();
            String interfaces[] = null;
            interfaces = new String[] { ISimple.class.getName() };
            c.setInterface(interfaces, props);
        }
    }
}
