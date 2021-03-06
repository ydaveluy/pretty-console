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
package org.eclipse.prettyconsole;

import java.util.regex.Pattern;

public class PrettyConsoleUtils {

	// match full escape sequence and incomplete escape sequence at the end
	public static final Pattern ESCAPE_SEQUENCE_REGEX_TXT = Pattern
			.compile("\u001b(?:\\[[\\d;]*[A-HJKSTfimnsu]|(?:(?:\\[[\\d;]*)?\\z))");

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
