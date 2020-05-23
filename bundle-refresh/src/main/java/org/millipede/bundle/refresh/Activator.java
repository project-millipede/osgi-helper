package org.millipede.bundle.refresh;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.FrameworkWiring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        // Determine if any fragments are unresolved
        final List<Bundle> unresolvedFragments = getUnresolvedFragments(context);
        if (!unresolvedFragments.isEmpty()) {

            final List<Bundle> bundlesToRefresh = new ArrayList<>();

            // Get host bundle for each fragment to initialize refresh
            unresolvedFragments.forEach(bundle -> {
                Bundle hostBundle = getHostbundle(context, bundle.getHeaders().get("Fragment-Host"));
                bundlesToRefresh.add(hostBundle);
            });

            // Refresh host bundle
            final Bundle systemBundle = context.getBundle(0);
            FrameworkWiring frameworkWiring = systemBundle.adapt(FrameworkWiring.class);
            frameworkWiring.refreshBundles(bundlesToRefresh);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {

    }

    private List<Bundle> getUnresolvedFragments(BundleContext context) {
        return Arrays.stream(context.getBundles())
                .filter(bundle -> {
                    String fragmentHost = bundle.getHeaders().get("Fragment-Host");
                    return fragmentHost != null && bundle.getState() == Bundle.INSTALLED;
                })
                .collect(Collectors.toList());
    }

    private Bundle getHostbundle(BundleContext context, String symbolicName) {
        return Arrays.stream(context.getBundles())
                .filter(b -> symbolicName.equalsIgnoreCase(b.getSymbolicName()))
                .findFirst()
                .orElse(null);
    }

}
