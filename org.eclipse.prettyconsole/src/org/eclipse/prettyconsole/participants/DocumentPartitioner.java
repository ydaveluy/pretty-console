package org.eclipse.prettyconsole.participants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.prettyconsole.PrettyConsoleActivator;
import org.eclipse.prettyconsole.PrettyConsoleUtils;
import org.eclipse.prettyconsole.preferences.PreferenceUtils;
import org.eclipse.prettyconsole.utils.StyleAttribute;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.osgi.framework.FrameworkUtil;

public class DocumentPartitioner implements IDocumentPartitioner {

	private static final ILog LOGGER = Platform.getLog(FrameworkUtil.getBundle(DocumentPartitioner.class));
	// store the last processed attributes
	private StyleAttribute attributes = StyleAttribute.DEFAULT;
	// the matcher used to find escape sequences
	private final Matcher matcher = PrettyConsoleUtils.ESCAPE_SEQUENCE_REGEX_TXT.matcher("");
	// the matcher used to find incomplete escape sequences
	private final Matcher endMatcher = PrettyConsoleUtils.INCOMPLETE_ESCAPE_SEQUENCE_REGEX_TXT.matcher("");
	// the last incomplete escape sequence
	private String incompleteEscapeSequence = "";

	public static final String PARTITION_NAME = "pretty_console";

	private final DefaultPositionUpdater positionUpdater = new DefaultPositionUpdater(PARTITION_NAME);

	private boolean enable;
	private IDocument document;

	// cache the document positions
	private Position[] positions = {};

	public void updateEventStyles(LineStyleEvent event, Color foregroundColor, Color backgroundColor) {

		// no position means nothing to do
		if (positions.length == 0 || event.lineText == null || event.lineText.isEmpty()) {
			return;
		}

		final List<StyleRange> styles = new ArrayList<>(4);

		// keep existing styles if any
		if (event.styles != null) {
			Collections.addAll(styles, event.styles);
		}

		// filters all the positions that overlap with the current event line
		final int offset = event.lineOffset;
		final int length = event.lineText.length();

		final int rangeEnd = offset + length;

		int index = findFirstOverlappigPosition(event);
		Position position = positions[index];
		boolean found = false;

		// process positions that overlap with the current line
		while (index < positions.length && position.getOffset() < rangeEnd) {

			((AbstractStyledPosition) position).overrideStyleRange(styles, offset, length, foregroundColor,
					backgroundColor);

			found = true;
			index++;
			if (index < positions.length) {
				position = positions[index];
			}
		}

		// update event styles if found an overlapping position
		if (found) {
			event.styles = styles.toArray(new StyleRange[styles.size()]);
		}
	}

	private int findFirstOverlappigPosition(LineStyleEvent event) {
		final int offset = event.lineOffset;
		final int length = event.lineText.length();

		final int rangeEnd = offset + length;
		int left = 0;
		int right = positions.length - 1;

		int mid;
		Position position;

		// find the first overlapping position
		while (left < right) {

			mid = (left + right) / 2;
			position = positions[mid];
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
			position = positions[index];
			while (index >= 0 && position.getOffset() + position.getLength() > offset) {
				index--;
				if (index > 0) {
					position = positions[index];
				}
			}
		}
		index++;
		return index;
	}

	private void updatePositionCache() {
		// update the cached positions
		try {
			positions = document.getPositions(PARTITION_NAME);
		} catch (final BadPositionCategoryException e) {
			LOGGER.log(new Status(IStatus.ERROR, PrettyConsoleActivator.PLUGIN_ID, e.getMessage()));
		}
	}

	protected void update(int offset, String text) {

		if (text == null || text.isEmpty()) {
			return;
		}

		// sometime an incomplete escape sequence is at the end of text.
		// In such case, store the incomplete sequence and remove it from the
		// text.
		// Reuse the incomplete sequence to process the next text
		text = incompleteEscapeSequence + text;
		offset -= incompleteEscapeSequence.length();

		endMatcher.reset(text);
		if (endMatcher.find()) {
			incompleteEscapeSequence = endMatcher.group();
			text = text.substring(0, text.length() - incompleteEscapeSequence.length());
		} else {
			incompleteEscapeSequence = "";
		}

		matcher.reset(text);
		try {

			int escapeOffet = 0;
			// find all escapes codes in the appended text and compute the new positions
			while (matcher.find()) {
				final int start = matcher.start();
				final String group = matcher.group();
				// add a position between two escape codes (or from the beginning to an escape
				// code)
				// add this position only if the attributes is of interest (different from
				// default)
				if (attributes != StyleAttribute.DEFAULT && start > escapeOffet) {
					document.addPosition(PARTITION_NAME,
							new StyledPosition(escapeOffet + offset, start - escapeOffet, attributes));
				}

				// add a position to hide the escape code
				document.addPosition(PARTITION_NAME, new EscapeCodePosition(start + offset, group.length()));

				// update the attributes
				attributes = attributes.apply(group);

				// update the offset
				escapeOffet = matcher.end();

			}

			// add a position between the last escape code (or from the beginning) and the
			// end of the appended text
			// add this position only if the attribute is of interest
			if (attributes != StyleAttribute.DEFAULT && text.length() > escapeOffet) {
				document.addPosition(PARTITION_NAME,
						new StyledPosition(escapeOffet + offset, text.length() - escapeOffet, attributes));
			}

		} catch (BadPositionCategoryException | BadLocationException e) {
			LOGGER.log(new Status(IStatus.ERROR, PrettyConsoleActivator.PLUGIN_ID, e.getMessage()));

		}

	}

	private void doConnect() {

		document.addPositionCategory(PARTITION_NAME);

		attributes = StyleAttribute.DEFAULT;
		incompleteEscapeSequence = "";
		enable = true;
		// initialize the positions
		update(0, document.get());
		updatePositionCache();
	}

	@Override
	public void connect(IDocument document) {
		this.document = document;
		doConnect();
	}

	@Override
	public void disconnect() {
		enable = false;
		try {
			document.removePositionCategory(PARTITION_NAME);
		} catch (final BadPositionCategoryException e) {
			LOGGER.log(new Status(IStatus.ERROR, PrettyConsoleActivator.PLUGIN_ID, e.getMessage()));
		}
		positions = new Position[0];
	}

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		// ignore
	}

	@Override
	public boolean documentChanged(DocumentEvent event) {

		if (!PreferenceUtils.isPrettyConsoleEnabled()) {

			if (enable) {
				disconnect();
			}
		}

		else if (!enable) {
			doConnect();
		} else {
			// adapt existing positions (we are interested only by remove events)
			if (event.getOffset() == 0) {
				// when the document is empty, remove all positions together
				// to improve performances during console clear
				if (document.getLength() == 0) {
					try {
						document.removePositionCategory(PARTITION_NAME);
						document.addPositionCategory(PARTITION_NAME);
					} catch (final BadPositionCategoryException e) {
						LOGGER.log(new Status(IStatus.ERROR, PrettyConsoleActivator.PLUGIN_ID, e.getMessage()));
					}
				} else {
					positionUpdater.update(event);
				}
			}

			update(event.getOffset(), event.getText());

			updatePositionCache();
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
