package org.eclipse.prettyconsole.preferences;

import static org.eclipse.prettyconsole.preferences.PreferenceConstants.PREF_COLOR;
import static org.eclipse.prettyconsole.preferences.PreferenceConstants.PREF_COLOR_PALETTE;
import static org.eclipse.prettyconsole.preferences.PreferenceConstants.PREF_PRETTY_CONSOLE_ENABLED;
import static org.eclipse.prettyconsole.preferences.PreferenceConstants.PREF_PUT_RTF_IN_CLIPBOARD;
import static org.eclipse.prettyconsole.preferences.PreferenceConstants.PREF_SHOW_ESCAPES;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.prettyconsole.PrettyConsoleActivator;
import org.eclipse.prettyconsole.utils.ColorPalette;
import org.eclipse.swt.graphics.RGB;

public class PreferenceUtils {

	private static final IPreferenceStore PREF_STORE = PrettyConsoleActivator.getDefault().getPreferenceStore();

	// Caching, for better performance.
	private static String getPreferredPalette = PREF_STORE.getString(PREF_COLOR_PALETTE);
	private static boolean showEscapeCodes = PREF_STORE.getBoolean(PREF_SHOW_ESCAPES);
	private static boolean isPrettyConsoleEnabled = PREF_STORE.getBoolean(PREF_PRETTY_CONSOLE_ENABLED);
	private static boolean clipboardPutRtf = PREF_STORE.getBoolean(PREF_PUT_RTF_IN_CLIPBOARD);

	static {
		PREF_STORE.addPropertyChangeListener(PreferenceUtils::refresh);
	}

	private PreferenceUtils() {
		// Utility class, should not be instantiated
	}

	public static boolean putRtfInClipboard() {
		return clipboardPutRtf;
	}

	// This is not cached, because it can change from both Preferences and the icon
	// on console
	public static boolean isPrettyConsoleEnabled() {
		return isPrettyConsoleEnabled;
	}

	public static void setPrettyConsoleEnabled(boolean enabled) {
		PREF_STORE.setValue(PREF_PRETTY_CONSOLE_ENABLED, enabled);
	}

	public static String getPreferredPalette() {
		return getPreferredPalette;
	}

	public static boolean showEscapeCodes() {
		return showEscapeCodes;
	}

	public static void refresh(PropertyChangeEvent evt) {

		switch (evt.getProperty()) {
		case PREF_COLOR_PALETTE:
			getPreferredPalette = (String) evt.getNewValue();
			break;
		case PREF_SHOW_ESCAPES:
			showEscapeCodes = (boolean) evt.getNewValue();
			break;
		case PREF_PRETTY_CONSOLE_ENABLED:
			isPrettyConsoleEnabled = (boolean) evt.getNewValue();
			break;
		case PREF_PUT_RTF_IN_CLIPBOARD:
			clipboardPutRtf = (boolean) evt.getNewValue();
			break;
		default:

			for (int i = 0; i < PREF_COLOR.length; ++i) {
				if (PREF_COLOR[i].equals(evt.getProperty())) {
					// update the current palette (do not replace)

					final RGB[] palette = ColorPalette.getCurrentPalette();
					final RGB rgb = PreferenceConverter.getColor(PREF_STORE, evt.getProperty());
					palette[i].blue = rgb.blue;
					palette[i].green = rgb.green;
					palette[i].red = rgb.red;
					break;
				}
			}

			break;
		}

		PrettyConsoleActivator.getDefault().redrawViewers();
	}
}
