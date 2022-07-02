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

import static org.eclipse.prettyconsole.preferences.PreferenceConstants.PREF_COLOR;
import static org.eclipse.prettyconsole.preferences.PreferenceConstants.PREF_COLOR_PALETTE;
import static org.eclipse.prettyconsole.preferences.PreferenceConstants.PREF_PRETTY_CONSOLE_ENABLED;
import static org.eclipse.prettyconsole.preferences.PreferenceConstants.PREF_PUT_RTF_IN_CLIPBOARD;
import static org.eclipse.prettyconsole.preferences.PreferenceConstants.PREF_SHOW_ESCAPES;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.prettyconsole.PrettyConsoleActivator;
import org.eclipse.prettyconsole.utils.ColorPalette;
import org.eclipse.swt.graphics.RGB;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = PrettyConsoleActivator.getDefault().getPreferenceStore();
		store.setDefault(PREF_PRETTY_CONSOLE_ENABLED, true);
		store.setDefault(PREF_SHOW_ESCAPES, false);
		store.setDefault(PREF_COLOR_PALETTE, ColorPalette.getBestPaletteForOS());
		store.setDefault(PREF_PUT_RTF_IN_CLIPBOARD, true);

		final RGB[] palette = ColorPalette.getCurrentPalette();
		for (int i = 0; i < PREF_COLOR.length; ++i) {
			PreferenceConverter.setDefault(store, PREF_COLOR[i], palette[i]);
		}

	}

}
