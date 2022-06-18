package org.eclipse.prettyconsole;

import java.util.regex.Pattern;

public class PrettyConsoleUtils {
	public static final Pattern ESCAPE_SEQUENCE_REGEX_TXT = Pattern.compile("\u001b\\[[\\d;]*[A-HJKSTfimnsu]");
	public static final Pattern INCOMPLETE_ESCAPE_SEQUENCE_REGEX_TXT = Pattern.compile("\u001b(\\[[\\d;?]*)?\\z");
	public static final Pattern ESCAPE_SEQUENCE_REGEX_RTF = Pattern
			.compile("\\{\\\\cf\\d+[^}]* \u001b\\[[\\d;]*[A-HJKSTfimnsu][^}]*\\}");
	// These two are used to replace \chshdng#1\chcbpat#2 with
	// \chshdng#1\chcbpat#2\cb#2
	public static final Pattern ESCAPE_SEQUENCE_REGEX_RTF_FIX_SRC = Pattern.compile("\\\\chshdng\\d+\\\\chcbpat(\\d+)");
	public static final String ESCAPE_SEQUENCE_REGEX_RTF_FIX_TRG = "$0\\\\cb$1";

	public static final char ESCAPE_SGR = 'm';

	private PrettyConsoleUtils() {
		// Utility class, should not be instantiated
	}

}
