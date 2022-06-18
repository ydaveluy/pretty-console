package org.eclipse.prettyconsole.participants;

import java.util.List;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

public abstract class AbstractStyledPosition extends org.eclipse.jface.text.Position {

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
		ranges.add(insertIndex, getStyle(overrideStart, overrideEnd - overrideStart, foregroundColor, backgroundColor));
	}
}
