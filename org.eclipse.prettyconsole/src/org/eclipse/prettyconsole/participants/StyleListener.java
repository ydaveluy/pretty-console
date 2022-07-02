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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.prettyconsole.preferences.PreferenceUtils;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyledText;

public class StyleListener implements LineStyleListener {

	private static StyleListener instance = new StyleListener();

	public static void install(StyledText viewer) {

		viewer.removeLineStyleListener(StyleListener.instance);
		viewer.addLineStyleListener(StyleListener.instance);
	}

	private StyleListener() {
	}

	@Override
	public void lineGetStyle(LineStyleEvent event) {

		// update the event only if pretty console is enabled
		if (!PreferenceUtils.isPrettyConsoleEnabled() || !(event.widget instanceof StyledText)) {
			return;
		}

		final StyledText text = (StyledText) event.widget;

		final IDocument document = PageParticipant.getDocument(text);

		if (!(document instanceof IDocumentExtension3)) {
			return;
		}

		final IDocumentExtension3 docExt = (IDocumentExtension3) document;

		DocumentPartitioner partitioner = (DocumentPartitioner) docExt
				.getDocumentPartitioner(DocumentPartitioner.PARTITION_NAME);

		// Install the AnsiDocumentPartitioner if not already installed
		if (partitioner == null) {
			partitioner = new DocumentPartitioner();
			partitioner.connect(document);
			docExt.setDocumentPartitioner(DocumentPartitioner.PARTITION_NAME, partitioner);
		}

		// update event styles
		partitioner.updateEventStyles(event, text.getForeground(), text.getBackground());

	}

}
