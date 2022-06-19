package org.eclipse.prettyconsole.utils;

import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_CONCEAL_OFF;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_CONCEAL_ON;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_CROSSOUT_OFF;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_CROSSOUT_ON;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_FRAMED_OFF;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_FRAMED_ON;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_INTENSITY_BRIGHT;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_INTENSITY_FAINT;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_INTENSITY_NORMAL;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_ITALIC;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_ITALIC_OFF;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_NEGATIVE_OFF;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_NEGATIVE_ON;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_RESET;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_UNDERLINE;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_UNDERLINE_DOUBLE;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_ATTR_UNDERLINE_OFF;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_COLOR_BACKGROUND_FIRST;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_COLOR_BACKGROUND_LAST;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_COLOR_BACKGROUND_RESET;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_COLOR_FOREGROUND_FIRST;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_COLOR_FOREGROUND_LAST;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_COLOR_FOREGROUND_RESET;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_COLOR_INTENSITY_DELTA;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_HICOLOR_BACKGROUND;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_HICOLOR_BACKGROUND_FIRST;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_HICOLOR_BACKGROUND_LAST;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_HICOLOR_FOREGROUND;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_HICOLOR_FOREGROUND_FIRST;
import static org.eclipse.prettyconsole.utils.Commands.COMMAND_HICOLOR_FOREGROUND_LAST;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.prettyconsole.PrettyConsoleUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class StyleAttribute {

	private static final int UNDERLINE_SIMPLE = 1 << 27;
	private static final int UNDERLINE_DOUBLE = 1 << 28;
	private static final int STRIKETHROUGH = 1 << 29;
	private static final int INVERT = 1 << 30;
	private static final int CONCEAL = 1 << 31;
	private static final int FONT_STYLE_MASK = SWT.ITALIC | SWT.BOLD | SWT.NORMAL;
	private static final int UNDERLINE_STYLE_MASK = UNDERLINE_SIMPLE | UNDERLINE_DOUBLE;

	public static final StyleAttribute DEFAULT = new StyleAttribute();

	// If you change any of these also update reset()
	private RGB background;
	private RGB foreground;
	private int style;

	private StyleAttribute() {
		reset();
	}

	private StyleAttribute(StyleAttribute other) {
		this.background = other.background;
		this.foreground = other.foreground;
		this.style = other.style;
	}

	private void reset() {
		background = null;
		foreground = null;
		style = SWT.NORMAL;
	}

	private boolean isDefault() {
		return background == null && foreground == null && style == SWT.NORMAL;

	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		if (background != null) {
			result.append("Bg" + background);
		}
		if (foreground != null) {
			result.append("Fg" + foreground);
		}

		if ((style & UNDERLINE_SIMPLE) != 0) {
			result.append("_");
		}
		if ((style & SWT.BOLD) != 0) {
			result.append("B");
		}
		if ((style & SWT.ITALIC) != 0) {
			result.append("I");
		}

		if ((style & INVERT) != 0) {
			result.append("!");
		}
		if ((style & CONCEAL) != 0) {
			result.append("H");
		}

		if ((style & STRIKETHROUGH) != 0) {
			result.append("-");
		}

		if ((style & SWT.BORDER) != 0) {
			result.append("[]");
		}

		return result.toString();
	}

	// This function maps from the current attributes as "described" by escape
	// sequences to real,
	// Eclipse console specific attributes (resolving color palette, default colors,
	// etc.)
	public static void updateRangeStyle(StyleRange range, final StyleAttribute attribute) {

		// update the foreground color
		if (attribute.foreground != null) {
			range.foreground = ColorPalette.getColor(attribute.foreground);
		}

		// update the background color
		if (attribute.background != null) {
			range.background = ColorPalette.getColor(attribute.background);
		}

		if ((attribute.style & INVERT) != 0) {
			// swap background/foreground
			final Color tmp = range.background;
			range.background = range.foreground;
			range.foreground = tmp;
		}

		if ((attribute.style & CONCEAL) != 0) {
			range.foreground = range.background;
		}

		range.font = null;
		range.fontStyle = attribute.style & FONT_STYLE_MASK;

		// Prepare the rest of the attributes
		if ((attribute.style & UNDERLINE_STYLE_MASK) != 0) {
			range.underline = true;
			range.underlineColor = range.foreground;
			range.underlineStyle = (attribute.style & UNDERLINE_DOUBLE) != 0 ? SWT.UNDERLINE_DOUBLE
					: SWT.UNDERLINE_SINGLE;
		}

		range.strikeout = (attribute.style & STRIKETHROUGH) != 0;
		if (range.strikeout) {
			range.strikeoutColor = range.foreground;
		}

		if ((attribute.style & SWT.BORDER) != 0) {
			range.borderStyle = SWT.BORDER_SOLID;
			range.borderColor = range.foreground;
		}
	}

	/**
	 * Apply an ansi escape code to the current attribute
	 *
	 * @param ansiCode the ansi code
	 * @return the resulting attributes
	 */
	public StyleAttribute apply(String ansiCode) {
		final char code = ansiCode.charAt(ansiCode.length() - 1);
		if (code == PrettyConsoleUtils.ESCAPE_SGR) {

			final StyleAttribute current = new StyleAttribute(this);

			// Select Graphic Rendition (SGR) escape sequence
			current.interpretCommand(parseSemicolonSeparatedIntList(ansiCode.substring(2, ansiCode.length() - 1)));
			if (current.isDefault()) {
				return StyleAttribute.DEFAULT;
			}

			return current;
		}
		return this;
	}

	// Takes a string that looks like this: int [ ';' int] and returns a list of the
	// integers
	private static List<Integer> parseSemicolonSeparatedIntList(String text) {
		final List<Integer> result = new ArrayList<>(10);
		int crtValue = 0;
		for (int i = 0; i < text.length(); i++) {
			final char ch = text.charAt(i);
			if (ch >= '0' && ch <= '9') {
				crtValue *= 10;
				crtValue += ch - '0';
			} else {
				result.add(crtValue);
				crtValue = 0;
			}
		}
		result.add(crtValue);
		return result;
	}

	private void interpretCommand(List<Integer> nCommands) {

		final Iterator<Integer> iter = nCommands.iterator();
		while (iter.hasNext()) {
			final int nCmd = iter.next();
			switch (nCmd) {
			case COMMAND_ATTR_RESET:
				reset();
				break;

			case COMMAND_ATTR_INTENSITY_BRIGHT:
				style |= SWT.BOLD;
				break;
			case COMMAND_ATTR_INTENSITY_FAINT: // Intentional fallthrough
			case COMMAND_ATTR_INTENSITY_NORMAL:
				style &= ~SWT.BOLD;
				break;

			case COMMAND_ATTR_ITALIC:
				style |= SWT.ITALIC;
				break;
			case COMMAND_ATTR_ITALIC_OFF:
				style &= ~SWT.ITALIC;
				break;

			case COMMAND_ATTR_UNDERLINE:
				style |= UNDERLINE_SIMPLE;
				break;
			case COMMAND_ATTR_UNDERLINE_DOUBLE:
				style |= UNDERLINE_DOUBLE;
				break;
			case COMMAND_ATTR_UNDERLINE_OFF:
				style &= ~UNDERLINE_DOUBLE;
				break;

			case COMMAND_ATTR_CROSSOUT_ON:
				style |= STRIKETHROUGH;
				break;
			case COMMAND_ATTR_CROSSOUT_OFF:
				style &= ~STRIKETHROUGH;
				break;

			case COMMAND_ATTR_NEGATIVE_ON:
				style |= INVERT;
				break;
			case COMMAND_ATTR_NEGATIVE_OFF:
				style &= ~INVERT;
				break;

			case COMMAND_ATTR_CONCEAL_ON:
				style |= CONCEAL;
				break;
			case COMMAND_ATTR_CONCEAL_OFF:
				style &= ~CONCEAL;
				break;

			case COMMAND_ATTR_FRAMED_ON:
				style |= SWT.BORDER;
				break;
			case COMMAND_ATTR_FRAMED_OFF:
				style &= ~SWT.BORDER;
				break;

			case COMMAND_COLOR_FOREGROUND_RESET:
				foreground = null;
				break;
			case COMMAND_COLOR_BACKGROUND_RESET:
				background = null;
				break;

			case COMMAND_HICOLOR_FOREGROUND:
			case COMMAND_HICOLOR_BACKGROUND: // {esc}[48;5;{color}m
				int color = -1;
				final int nMustBe2or5 = iter.hasNext() ? iter.next() : -1;
				if (nMustBe2or5 == 5) { // 256 colors
					color = iter.hasNext() ? iter.next() : -1;
					if (!ColorPalette.isValidIndex(color)) {
						color = -1;
					}
				} else if (nMustBe2or5 == 2) { // rgb colors
					final int r = iter.hasNext() ? iter.next() : -1;
					final int g = iter.hasNext() ? iter.next() : -1;
					final int b = iter.hasNext() ? iter.next() : -1;
					color = ColorPalette.hackRgb(r, g, b);
				}
				if (color != -1) {
					if (nCmd == COMMAND_HICOLOR_FOREGROUND) {
						foreground = ColorPalette.getColor(color);
					} else {
						background = ColorPalette.getColor(color);
					}
				}
				break;

			case -1:
				break; // do nothing

			default:
				if (nCmd >= COMMAND_COLOR_FOREGROUND_FIRST && nCmd <= COMMAND_COLOR_FOREGROUND_LAST) {
					// text color
					foreground = ColorPalette.getColor(nCmd - COMMAND_COLOR_FOREGROUND_FIRST);
				} else if (nCmd >= COMMAND_COLOR_BACKGROUND_FIRST && nCmd <= COMMAND_COLOR_BACKGROUND_LAST) {
					// background color
					background = ColorPalette.getColor(nCmd - COMMAND_COLOR_BACKGROUND_FIRST);
				} else if (nCmd >= COMMAND_HICOLOR_FOREGROUND_FIRST && nCmd <= COMMAND_HICOLOR_FOREGROUND_LAST) {
					// text color
					foreground = ColorPalette
							.getColor(nCmd - COMMAND_HICOLOR_FOREGROUND_FIRST + COMMAND_COLOR_INTENSITY_DELTA);
				} else if (nCmd >= COMMAND_HICOLOR_BACKGROUND_FIRST && nCmd <= COMMAND_HICOLOR_BACKGROUND_LAST) {
					// background color
					background = ColorPalette
							.getColor(nCmd - COMMAND_HICOLOR_BACKGROUND_FIRST + COMMAND_COLOR_INTENSITY_DELTA);
				}
			}
		}
	}
}
