package org.eclipse.prettyconsole.participants;

import org.eclipse.prettyconsole.utils.StyleAttribute;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

public class StyledPosition extends AbstractStyledPosition {

	// StyleRange for the current position
	protected final StyleAttribute attributes;

	/**
	 * Build a position with a specific style
	 *
	 * @param offset     the position offset
	 * @param length     the position length
	 * @param attributes the style
	 */
	public StyledPosition(int offset, int length, StyleAttribute attributes) {
		super(offset, length);
		this.attributes = attributes;

	}

	/**
	 * Get the Style of the position
	 *
	 * @return the Style of the position
	 */
	@Override
	public StyleRange getStyle(int offset, int length, Color foregroundColor, Color backgroundColor) {

		final StyleRange style = new StyleRange(offset, length, foregroundColor, backgroundColor);

		// update the style with the attributes
		StyleAttribute.updateRangeStyle(style, attributes);

		return style;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof StyledPosition) {
			final StyledPosition rp = (StyledPosition) other;
			return rp.attributes == attributes && super.equals(other);
		}
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ attributes.hashCode();
	}

}
