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
package org.eclipse.prettyconsole.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class ColorPalette {
	public static final String PALETTE_VGA = "paletteVGA";
	public static final String PALETTE_WINXP = "paletteXP";
	public static final String PALETTE_WIN10 = "paletteWin10";
	public static final String PALETTE_MAC = "paletteMac";
	public static final String PALETTE_PUTTY = "palettePuTTY";
	public static final String PALETTE_XTERM = "paletteXTerm";
	public static final String PALETTE_MIRC = "paletteMirc";
	public static final String PALETTE_UBUNTU = "paletteUbuntu";
	public static final String PALETTE_TANGO = "paletteTango";
	public static final String PALETTE_RXVT = "paletteRxvt";
	public static final String PALETTE_CUSTOM = "paletteCustom";

	private static final int PALETTE_SIZE = 256;
	private static final int TRUE_RGB_FLAG = 0x10000000; // Representing true RGB colors as 0x10RRGGBB

	private ColorPalette() {
		// Utility class, should not be instantiated
	}

	// From Wikipedia, https://en.wikipedia.org/wiki/ANSI_escape_code
	private static final RGB[] paletteVGA = { new RGB(0, 0, 0), // black
			new RGB(170, 0, 0), // red
			new RGB(0, 170, 0), // green
			new RGB(170, 85, 0), // brown/yellow
			new RGB(0, 0, 170), // blue
			new RGB(170, 0, 170), // magenta
			new RGB(0, 170, 170), // cyan
			new RGB(170, 170, 170), // gray
			new RGB(85, 85, 85), // dark gray
			new RGB(255, 85, 85), // bright red
			new RGB(85, 255, 85), // bright green
			new RGB(255, 255, 85), // yellow
			new RGB(85, 85, 255), // bright blue
			new RGB(255, 85, 255), // bright magenta
			new RGB(85, 255, 255), // bright cyan
			new RGB(255, 255, 255) // white
	};
	private static final RGB[] paletteXP = { new RGB(0, 0, 0), // black
			new RGB(128, 0, 0), // red
			new RGB(0, 128, 0), // green
			new RGB(128, 128, 0), // brown/yellow
			new RGB(0, 0, 128), // blue
			new RGB(128, 0, 128), // magenta
			new RGB(0, 128, 128), // cyan
			new RGB(192, 192, 192), // gray
			new RGB(128, 128, 128), // dark gray
			new RGB(255, 0, 0), // bright red
			new RGB(0, 255, 0), // bright green
			new RGB(255, 255, 0), // yellow
			new RGB(0, 0, 255), // bright blue
			new RGB(255, 0, 255), // bright magenta
			new RGB(0, 255, 255), // bright cyan
			new RGB(255, 255, 255) // white
	};
	private static final RGB[] paletteWin10 = { new RGB(12, 12, 12), // black
			new RGB(197, 15, 31), // red
			new RGB(19, 161, 14), // green
			new RGB(193, 156, 0), // brown/yellow
			new RGB(0, 55, 218), // blue
			new RGB(136, 23, 152), // magenta
			new RGB(58, 150, 221), // cyan
			new RGB(204, 204, 204), // gray
			new RGB(118, 118, 118), // dark gray
			new RGB(231, 72, 86), // bright red
			new RGB(22, 198, 12), // bright green
			new RGB(249, 241, 165), // yellow
			new RGB(59, 120, 255), // bright blue
			new RGB(180, 0, 158), // bright magenta
			new RGB(97, 214, 214), // bright cyan
			new RGB(242, 242, 242) // white
	};
	private static final RGB[] paletteMac = { new RGB(0, 0, 0), // black
			new RGB(194, 54, 33), // red
			new RGB(37, 188, 36), // green
			new RGB(173, 173, 39), // brown/yellow
			new RGB(73, 46, 225), // blue
			new RGB(211, 56, 211), // magenta
			new RGB(51, 187, 200), // cyan
			new RGB(203, 204, 205), // gray
			new RGB(129, 131, 131), // dark gray
			new RGB(252, 57, 31), // bright red
			new RGB(49, 231, 34), // bright green
			new RGB(234, 236, 35), // yellow
			new RGB(88, 51, 255), // bright blue
			new RGB(249, 53, 248), // bright magenta
			new RGB(20, 240, 240), // bright cyan
			new RGB(233, 235, 235) // white
	};
	private static final RGB[] palettePuTTY = { new RGB(0, 0, 0), // black
			new RGB(187, 0, 0), // red
			new RGB(0, 187, 0), // green
			new RGB(187, 187, 0), // brown/yellow
			new RGB(0, 0, 187), // blue
			new RGB(187, 0, 187), // magenta
			new RGB(0, 187, 187), // cyan
			new RGB(187, 187, 187), // gray
			new RGB(85, 85, 85), // dark gray
			new RGB(255, 85, 85), // bright red
			new RGB(85, 255, 85), // bright green
			new RGB(255, 255, 85), // yellow
			new RGB(85, 85, 255), // bright blue
			new RGB(255, 85, 255), // bright magenta
			new RGB(85, 255, 255), // bright cyan
			new RGB(255, 255, 255) // white
	};
	private static final RGB[] paletteXTerm = { new RGB(0, 0, 0), // black
			new RGB(205, 0, 0), // red
			new RGB(0, 205, 0), // green
			new RGB(205, 205, 0), // brown/yellow
			new RGB(0, 0, 238), // blue
			new RGB(205, 0, 205), // magenta
			new RGB(0, 205, 205), // cyan
			new RGB(229, 229, 229), // gray
			new RGB(127, 127, 127), // dark gray
			new RGB(255, 0, 0), // bright red
			new RGB(0, 255, 0), // bright green
			new RGB(255, 255, 0), // yellow
			new RGB(92, 92, 255), // bright blue
			new RGB(255, 0, 255), // bright magenta
			new RGB(0, 255, 255), // bright cyan
			new RGB(255, 255, 255) // white
	};
	private static final RGB[] paletteMirc = { new RGB(0, 0, 0), // black
			new RGB(127, 0, 0), // red
			new RGB(0, 147, 0), // green
			new RGB(252, 127, 0), // brown/yellow
			new RGB(0, 0, 127), // blue
			new RGB(156, 0, 156), // magenta
			new RGB(0, 147, 147), // cyan
			new RGB(210, 210, 210), // gray
			new RGB(127, 127, 127), // dark gray
			new RGB(255, 0, 0), // bright red
			new RGB(0, 252, 0), // bright green
			new RGB(255, 255, 0), // yellow
			new RGB(0, 0, 252), // bright blue
			new RGB(255, 0, 255), // bright magenta
			new RGB(0, 255, 255), // bright cyan
			new RGB(255, 255, 255) // white
	};
	private static final RGB[] paletteUbuntu = { new RGB(1, 1, 1), // black
			new RGB(222, 56, 43), // red
			new RGB(57, 181, 74), // green
			new RGB(255, 199, 6), // brown/yellow
			new RGB(0, 111, 184), // blue
			new RGB(118, 38, 113), // magenta
			new RGB(44, 181, 233), // cyan
			new RGB(204, 204, 204), // gray
			new RGB(128, 128, 128), // dark gray
			new RGB(255, 0, 0), // bright red
			new RGB(0, 255, 0), // bright green
			new RGB(255, 255, 0), // yellow
			new RGB(0, 0, 255), // bright blue
			new RGB(255, 0, 255), // bright magenta
			new RGB(0, 255, 255), // bright cyan
			new RGB(255, 255, 255) // white
	};

	private static final RGB[] paletteTango = { new RGB(0, 0, 0), // black
			new RGB(204, 0, 0), // red
			new RGB(78, 154, 6), // green
			new RGB(196, 160, 0), // brown/yellow
			new RGB(52, 101, 164), // blue
			new RGB(117, 80, 123), // magenta
			new RGB(6, 152, 154), // cyan
			new RGB(211, 215, 207), // gray
			new RGB(85, 87, 83), // dark gray
			new RGB(239, 41, 41), // bright red
			new RGB(138, 226, 52), // bright green
			new RGB(252, 233, 79), // yellow
			new RGB(114, 159, 207), // bright blue
			new RGB(173, 127, 168), // bright magenta
			new RGB(52, 226, 226), // bright cyan
			new RGB(238, 238, 236) // white
	};

	private static final RGB[] paletteRxvt = { new RGB(0, 0, 0), // black
			new RGB(205, 0, 0), // red
			new RGB(0, 205, 0), // green
			new RGB(205, 205, 0), // brown/yellow
			new RGB(0, 0, 205), // blue
			new RGB(205, 0, 205), // magenta
			new RGB(0, 205, 205), // cyan
			new RGB(250, 235, 215), // gray
			new RGB(64, 64, 64), // dark gray
			new RGB(255, 0, 0), // bright red
			new RGB(0, 255, 0), // bright green
			new RGB(255, 255, 0), // yellow
			new RGB(0, 0, 255), // bright blue
			new RGB(255, 0, 255), // bright magenta
			new RGB(0, 255, 255), // bright cyan
			new RGB(255, 255, 255) // white
	};

	private static final Map<String, RGB[]> KNOWN_PALETTES = new HashMap<>();
	static {
		KNOWN_PALETTES.put(PALETTE_MAC, paletteMac);
		KNOWN_PALETTES.put(PALETTE_VGA, paletteVGA);
		KNOWN_PALETTES.put(PALETTE_WINXP, paletteXP);
		KNOWN_PALETTES.put(PALETTE_WIN10, paletteWin10);
		KNOWN_PALETTES.put(PALETTE_XTERM, paletteXTerm);
		KNOWN_PALETTES.put(PALETTE_PUTTY, palettePuTTY);
		KNOWN_PALETTES.put(PALETTE_MIRC, paletteMirc);
		KNOWN_PALETTES.put(PALETTE_UBUNTU, paletteUbuntu);
		KNOWN_PALETTES.put(PALETTE_TANGO, paletteTango);
		KNOWN_PALETTES.put(PALETTE_RXVT, paletteRxvt);
	}
	private static final String PALETTE_NAME = getBestPaletteForOS();
	private static RGB[] palette = KNOWN_PALETTES.get(PALETTE_NAME).clone();

	public static RGB[] getCurrentPalette() {
		return palette;
	}

	public static RGB[] getPalette(Object name) {
		return KNOWN_PALETTES.get(name);

	}

	public static boolean isValidIndex(int value) {
		return value >= 0 && value < PALETTE_SIZE;
	}

	public static int hackRgb(int r, int g, int b) {
		if (!isValidIndex(r) || !isValidIndex(g) || !isValidIndex(b)) {
			return -1;
		}
		return TRUE_RGB_FLAG | r << 16 | g << 8 | b;
	}

	private static int safe256(int value, int modulo) {
		final int result = value * PALETTE_SIZE / modulo;
		return result < PALETTE_SIZE ? result : PALETTE_SIZE - 1;
	}

	private static final HashMap<RGB, Color> CACHE = new HashMap<>();

	public static synchronized Color getColor(RGB rgb) {
		return CACHE.computeIfAbsent(rgb, color -> new Color(null, color));
	}

	public static RGB getColor(Integer index) {
		if (null == index) {
			return null;
		}

		if (index >= TRUE_RGB_FLAG) {
			final int red = index >> 16 & 0xff;
			final int green = index >> 8 & 0xff;
			final int blue = index & 0xff;
			return new RGB(red, green, blue);
		}

		if (index >= 0 && index < palette.length) { // basic, 16 color palette
			return palette[index];
		}

		if (index >= 16 && index < 232) { // 6x6x6 color matrix
			int color = index - 16;
			final int blue = color % 6;
			color = color / 6;
			final int green = color % 6;
			final int red = color / 6;

			return new RGB(safe256(red, 6), safe256(green, 6), safe256(blue, 6));
		}

		if (index >= 232 && index < PALETTE_SIZE) { // grayscale
			final int gray = safe256(index - 232, 24);
			return new RGB(gray, gray, gray);
		}

		return null;
	}

	public static String getBestPaletteForOS() {

		final String os = Platform.getOS();
		if (os == null) {
			return PALETTE_VGA;
		}

		switch (os) {
		case Platform.OS_LINUX:
			return PALETTE_XTERM;
		case Platform.OS_MACOSX:
			return PALETTE_MAC;
		case Platform.OS_WIN32:
			return PALETTE_WINXP;
		default:
			return PALETTE_VGA;
		}

	}
}
