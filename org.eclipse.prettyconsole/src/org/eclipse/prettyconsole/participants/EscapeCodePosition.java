package org.eclipse.prettyconsole.participants;

import org.eclipse.prettyconsole.preferences.PreferenceUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GlyphMetrics;

public class EscapeCodePosition extends AbstractStyledPosition {
	private static final Font MONO_FONT = new Font(null, "Monospaced", 6, SWT.NORMAL);
	private static final GlyphMetrics HIDE_CODE = new GlyphMetrics(0, 0, 0);

	/**
	 * Build an escape code position
	 *
	 * @param offset the position offset
	 * @param length the position length
	 */
	protected EscapeCodePosition(int offset, int length) {
		super(offset, length);
	}

	/**
	 * update the style according to preferences
	 */
	@Override
	protected StyleRange getStyle(int offset, int length, Color foregroundColor, Color backgroundColor) {

		final StyleRange style = new StyleRange(offset, length, null, null);
		// update the the Style according to current preferences
		if (PreferenceUtils.showEscapeCodes()) {
			style.font = MONO_FONT; // Show the codes in small, monospaced font
			style.metrics = null;

		} else {
			style.metrics = HIDE_CODE; // Hide the codes
			style.font = null;
		}
		return style;
	}
}
