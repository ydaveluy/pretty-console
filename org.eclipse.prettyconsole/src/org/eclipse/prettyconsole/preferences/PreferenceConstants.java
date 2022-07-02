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
package org.eclipse.prettyconsole.preferences;

public class PreferenceConstants {
	public static final String PREF_PRETTY_CONSOLE_ENABLED = "booleanEnabled";
	public static final String PREF_SHOW_ESCAPES = "booleanShowEscapes";
	public static final String PREF_COLOR_PALETTE = "choiceColorPalette";
	public static final String PREF_PUT_RTF_IN_CLIPBOARD = "booleanPutRtfInClipboard";
	protected static final String[] PREF_COLOR = { "color_black", "color_red", "color_green", "color_brown_yellow",
			"color_blue", "color_magenta", "color_cyan", "color_gray", "color_dark_gray", "color_bright_red",
			"color_bright_green", "color_yellow", "color_bright_blue", "color_bright_magenta", "color_bright_cyan",
			"color_white" };

	private PreferenceConstants() {
		// Utility class, should not be instantiated
	}

}
