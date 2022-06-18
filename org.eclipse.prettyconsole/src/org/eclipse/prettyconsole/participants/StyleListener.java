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

		// update the event only if the pretty console is enabled
		if (!PreferenceUtils.isPrettyConsoleEnabled() || !(event.getSource() instanceof StyledText)) {
			return;
		}

		final StyledText text = (StyledText) event.getSource();

		final IDocument document = PageParticipant.getDocument(text);

		if (!(document instanceof IDocumentExtension3)) {
			return;
		}

		final IDocumentExtension3 docExt = (IDocumentExtension3) document;

		DocumentPartitioner partitioner = (DocumentPartitioner) docExt
				.getDocumentPartitioner(DocumentPartitioner.PARTITION_NAME);

		// Install the DocumentPartitioner if not already installed
		if (partitioner == null) {
			partitioner = new DocumentPartitioner();
			partitioner.connect(document);
			docExt.setDocumentPartitioner(DocumentPartitioner.PARTITION_NAME, partitioner);
		}

		// update event styles
		partitioner.updateEventStyles(event, text.getForeground(), text.getBackground());

	}

}
