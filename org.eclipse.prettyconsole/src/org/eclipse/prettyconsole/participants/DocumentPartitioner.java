/*
 * Copyright (c) 2012-2022 Mihai Nita and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.eclipse.prettyconsole.participants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.prettyconsole.PrettyConsoleUtils;
import org.eclipse.prettyconsole.preferences.PreferenceUtils;
import org.eclipse.prettyconsole.utils.StyleAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GlyphMetrics;

public class DocumentPartitioner implements IDocumentPartitioner {

	// store the last processed attributes
	private StyleAttribute attributes = StyleAttribute.DEFAULT;
	// the matcher used to find escape sequences
	private final Matcher matcher = PrettyConsoleUtils.ESCAPE_SEQUENCE_REGEX_TXT.matcher("");
	// the last incomplete escape sequence
	private String incompleteEscapeSequence = "";

	public static final String PARTITION_NAME = "pretty_console";

	private boolean enable;
	private IDocument document;

	// the styled positions
	private final List<AbstractStyledPosition> positions = new ArrayList<>();

	// Style list
	final List<StyleRange> styles = new ArrayList<>();

	public void updateEventStyles(LineStyleEvent event, Color foregroundColor, Color backgroundColor) {

		// no position means nothing to do
		if (positions.isEmpty() || event.lineText == null || event.lineText.isEmpty()) {
			return;
		}

		// keep existing styles if any
		if (event.styles != null) {
			Collections.addAll(styles, event.styles);
		}

		// filters all the positions that overlap with the current event line

		final int offset = event.lineOffset;
		final int length = event.lineText.length();

		final int rangeEnd = offset + length;
		int left = 0;
		int right = positions.size() - 1;

		int mid;
		AbstractStyledPosition position;

		// find the first overlapping position
		while (left < right) {
			mid = (left + right) / 2;
			position = positions.get(mid);
			if (rangeEnd < position.getOffset()) {
				if (left == mid) {
					right = left;
				} else {
					right = mid - 1;
				}
			} else if (offset > position.getOffset() + position.getLength() - 1) {
				if (right == mid) {
					left = right;
				} else {
					left = mid + 1;
				}
			} else {
				left = right = mid;
			}
		}

		int index = left - 1;
		if (index >= 0) {
			position = positions.get(index);
			while (index >= 0 && position.getOffset() + position.getLength() > offset) {
				index--;
				if (index > 0) {
					position = positions.get(index);
				}
			}
		}
		index++;
		position = positions.get(index);
		boolean found = false;

		// process positions that overlap with the current line
		while (index < positions.size() && position.getOffset() < rangeEnd) {

			position.overrideStyleRange(styles, offset, length, foregroundColor, backgroundColor);

			found = true;
			index++;
			if (index < positions.size()) {
				position = positions.get(index);
			}
		}

		// update event styles if found an overlapping position
		if (found) {
			event.styles = styles.toArray(new StyleRange[0]);
		}

		styles.clear();
	}

	protected void update(int offset, String text) {

		if (text == null || text.isEmpty()) {
			return;
		}

		// Reuse the incomplete escape sequence if any
		if (!incompleteEscapeSequence.isEmpty()) {
			text = incompleteEscapeSequence + text;
			offset -= incompleteEscapeSequence.length();
			incompleteEscapeSequence = "";
		}

		matcher.reset(text);

		int start = 0;

		// find all escapes codes in the appended text and compute the new positions
		while (matcher.find()) {
			final int mstart = matcher.start();

			// add a position between two escape codes (or from the beginning to an escape
			// code)
			// add this position only if the attributes is of interest (different from
			// default)
			if (attributes != StyleAttribute.DEFAULT && mstart > start) {
				positions.add(new StyledPosition(start + offset, mstart - start, attributes));
			}
			final String group = matcher.group();

			// save the incomplete escape sequence if any
			if (matcher.hitEnd()) {
				incompleteEscapeSequence = group;
				return;
			}

			// add a position to hide the escape code
			positions.add(new EscapeCodePosition(mstart + offset, group.length()));

			// update the attributes
			attributes = attributes.apply(group);

			// update the start offset
			start = matcher.end();
		}

		// add a position between the last escape code (or from the beginning) and the
		// end of the appended text
		// add this position only if the attribute is of interest
		if (attributes != StyleAttribute.DEFAULT && text.length() > start) {
			positions.add(new StyledPosition(start + offset, text.length() - start, attributes));
		}

	}

	private void doConnect() {

		attributes = StyleAttribute.DEFAULT;
		incompleteEscapeSequence = "";
		enable = true;

		positions.clear();
		// initialize the positions
		update(0, document.get());
	}

	@Override
	public void connect(IDocument document) {
		this.document = document;
		doConnect();
	}

	@Override
	public void disconnect() {
		enable = false;
		positions.clear();
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		// ignore
	}

	@Override
	public boolean documentChanged(DocumentEvent event) {

		if (!PreferenceUtils.isPrettyConsoleEnabled()) {
			// disable this partitioner
			if (enable) {
				disconnect();
			}
		}
		// re-enable this partitioner
		else if (!enable) {
			doConnect();
		}

		else {
			// adapt existing positions (we are interested only by remove events)
			if (event.getOffset() == 0) {

				final int length = event.getLength();

				// remove all the starting positions
				positions.removeIf(p -> p.offset + p.length < length);

				if (length > 0) {

					// update remaining positions
					positions.parallelStream().forEach(p -> {

						if (p.offset > length) {
							// position after removed region
							// position: _________PPPPP
							// remove : xxxx
							// result : _____PPPPP
							p.offset -= length;
						} else {
							// position overlap with removed region
							// position: __PPPPP__
							// remove : xxxx
							// result : PPP__
							p.length -= length - p.offset;
							p.offset = 0;
						}
					});
				}
			}
			// handle new text
			update(event.getOffset(), event.getText());
		}
		return false;
	}

	@Override
	public String[] getLegalContentTypes() {
		return new String[0];
	}

	@Override
	public String getContentType(int offset) {
		return null;
	}

	@Override
	public ITypedRegion[] computePartitioning(int offset, int length) {
		return new ITypedRegion[0];
	}

	@Override
	public ITypedRegion getPartition(int offset) {
		return null;
	}

	private static abstract class AbstractStyledPosition extends org.eclipse.jface.text.Position {

		protected AbstractStyledPosition(int offset, int length) {
			super(offset, length);
		}

		protected abstract StyleRange getStyle(int offset, int length, Color foregroundColor, Color backgroundColor);

		public void overrideStyleRange(List<StyleRange> ranges, int offset, int length, Color foregroundColor,
				Color backgroundColor) {

			final int overrideStart = Math.max(offset, this.offset);
			final int overrideEnd = Math.min(offset + length, this.offset + this.length);
			int insertIndex = ranges.size();
			for (int i = ranges.size() - 1; i >= 0; i--) {
				final StyleRange existingRange = ranges.get(i);
				final int existingStart = existingRange.start;
				final int existingEnd = existingStart + existingRange.length;

				// Find first position to insert where offset of new style is smaller then all
				// offsets before. This way the list is still sorted by offset after insert if
				// it was sorted before and it will not fail if list was not sorted.
				if (overrideStart <= existingStart) {
					insertIndex = i;
				}

				// adjust the existing style if required
				if (overrideStart <= existingStart) { // new style starts before or with existing
					if (overrideEnd < existingStart) {
						// new style lies before existing style. No overlapping.
						// new style: ++++_________
						// existing : ________=====
						// . result : ++++____=====
						// nothing to do
					} else if (overrideEnd < existingEnd) {
						// new style overlaps start of existing.
						// new style: ++++++++_____
						// existing : _____========
						// . result : ++++++++=====
						final int overlap = overrideEnd - existingStart;
						existingRange.start += overlap;
						existingRange.length -= overlap;
						// TODO combine overlapping part
					} else {
						// new style completely overlaps existing.
						// new style: ___++++++++++
						// existing : ___======____
						// . result : ___++++++++++
						ranges.remove(i);

						if (existingRange.foreground != null) {
							foregroundColor = existingRange.foreground;
						}
						if (existingRange.background != null) {
							backgroundColor = existingRange.background;
						}

					}
				} else if (existingEnd < overrideStart) {
					// new style lies after existing style. No overlapping.
					// new style: _________++++
					// existing : =====________
					// . result : =====____++++
					// nothing to do
				} else if (overrideEnd >= existingEnd) {
					// new style overlaps end of existing.
					// new style: _____++++++++
					// existing : ========_____
					// . result : =====++++++++
					existingRange.length -= existingEnd - overrideStart;
				} else {
					// new style lies inside existing style but not overrides all of it
					// (and does not touch first or last offset of existing)
					// new style: ____+++++____
					// existing : =============
					// . result : ====+++++====
					final StyleRange clonedRange = (StyleRange) existingRange.clone();
					existingRange.length = overrideStart - existingStart;
					clonedRange.start = overrideEnd;
					clonedRange.length = existingEnd - overrideEnd;
					ranges.add(i + 1, clonedRange);
				}

				if (existingRange.foreground != null) {
					foregroundColor = existingRange.foreground;
				}
				if (existingRange.background != null) {
					backgroundColor = existingRange.background;
				}

			}
			ranges.add(insertIndex,
					getStyle(overrideStart, overrideEnd - overrideStart, foregroundColor, backgroundColor));
		}
	}

	private static class StyledPosition extends AbstractStyledPosition {

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
				return attributes.equals(rp.attributes) && super.equals(other);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return super.hashCode() ^ attributes.hashCode();
		}

	}

	private static class EscapeCodePosition extends AbstractStyledPosition {
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
			} else {
				style.metrics = HIDE_CODE; // Hide the codes
			}
			return style;
		}
	}
}
