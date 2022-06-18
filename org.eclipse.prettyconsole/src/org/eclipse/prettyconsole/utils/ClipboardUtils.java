package org.eclipse.prettyconsole.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.prettyconsole.PrettyConsoleUtils;
import org.eclipse.prettyconsole.preferences.PreferenceUtils;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class ClipboardUtils {

	private ClipboardUtils() {
		// hide constructor
	}

	public static void textToClipboard(StyledText styledText, boolean removeEscapeSeq) {
		final Clipboard clipboard = new Clipboard(Display.getDefault());

		clipboard.clearContents();
		styledText.copy(); // copy to clipboard using the default Eclipse behavior

		final List<Object> clipboardData = new ArrayList<>(2);
		final List<Transfer> clipboardTransfers = new ArrayList<>(2);

		final TextTransfer textTransfer = TextTransfer.getInstance();
		final Object textData = clipboard.getContents(textTransfer);
		if (textData instanceof String) {
			String plainText = (String) textData;
			if (removeEscapeSeq) {
				plainText = PrettyConsoleUtils.ESCAPE_SEQUENCE_REGEX_TXT.matcher(plainText).replaceAll("");
			}
			clipboardData.add(plainText);
			clipboardTransfers.add(textTransfer);
		}

		if (PreferenceUtils.putRtfInClipboard()) {
			final RTFTransfer rtfTransfer = RTFTransfer.getInstance();
			final Object rtfData = clipboard.getContents(rtfTransfer);
			if (rtfData instanceof String) {
				String rtfText = (String) rtfData;
				if (removeEscapeSeq) {
					rtfText = PrettyConsoleUtils.ESCAPE_SEQUENCE_REGEX_RTF.matcher(rtfText).replaceAll("");
				}
				// The Win version of MS Word, and Write, understand \chshdng and \chcbpat, but
				// not \cb
				// The MacOS tools seem to understand \cb, but not \chshdng and \chcbpat
				// But using both seems to work fine, both systems just ignore the tags they
				// don't understand.
				rtfText = PrettyConsoleUtils.ESCAPE_SEQUENCE_REGEX_RTF_FIX_SRC.matcher(rtfText)
						.replaceAll(PrettyConsoleUtils.ESCAPE_SEQUENCE_REGEX_RTF_FIX_TRG);
				clipboardData.add(rtfText);
				clipboardTransfers.add(rtfTransfer);
			}
		}

		clipboard.setContents(clipboardData.toArray(), clipboardTransfers.toArray(new Transfer[0]));

		clipboard.dispose();
	}
}
