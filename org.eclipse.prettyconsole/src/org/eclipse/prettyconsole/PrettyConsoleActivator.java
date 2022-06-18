package org.eclipse.prettyconsole;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class PrettyConsoleActivator extends AbstractUIPlugin {

	private static PrettyConsoleActivator plugin;
	public static final String PLUGIN_ID = "org.eclipse.prettyconsole"; //$NON-NLS-1$
	private final Map<StyledText, IConsolePageParticipant> viewers = new HashMap<>();

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static PrettyConsoleActivator getDefault() {
		return plugin;
	}

	public void addViewer(StyledText viewer, IConsolePageParticipant participant) {
		viewers.put(viewer, participant);
	}

	public void removeViewerWithPageParticipant(IConsolePageParticipant participant) {
		final Set<StyledText> toRemove = new HashSet<>();

		for (final Entry<StyledText, IConsolePageParticipant> entry : viewers.entrySet()) {
			if (entry.getValue() == participant) {
				toRemove.add(entry.getKey());
			}
		}

		for (final StyledText viewer : toRemove) {
			viewers.remove(viewer);
		}
	}

	/**
	 * Redraw all viewers
	 */
	public void redrawViewers() {
		for (final StyledText viewer : viewers.keySet()) {
			if (!viewer.isDisposed() && viewer.isVisible()) {
				viewer.redraw();
			}
		}
	}
}
