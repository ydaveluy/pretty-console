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
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;

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

}
