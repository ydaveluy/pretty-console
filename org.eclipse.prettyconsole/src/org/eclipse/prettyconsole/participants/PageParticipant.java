package org.eclipse.prettyconsole.participants;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.prettyconsole.PrettyConsoleActivator;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TypedListener;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

public class PageParticipant implements IConsolePageParticipant {
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public void activated() {
		// Nothing to do, but we are forced to implement it for IConsolePageParticipant
	}

	@Override
	public void deactivated() {
		// Nothing to do, but we are forced to implement it for IConsolePageParticipant
	}

	@Override
	public void dispose() {
		PrettyConsoleActivator.getDefault().removeViewerWithPageParticipant(this);
	}

	@Override
	public void init(IPageBookViewPage page, IConsole console) {
		if (page.getControl() instanceof StyledText) {
			final StyledText viewer = (StyledText) page.getControl();
			final IDocument document = getDocument(viewer);
			if (document == null) {
				return;
			}
			StyleListener.install(viewer);
			PrettyConsoleActivator.getDefault().addViewer(viewer, this);
		}
	}

	// Find the document associated with the viewer
	static IDocument getDocument(StyledText viewer) {
		for (final Listener listener : viewer.getListeners(ST.LineGetStyle)) {
			if (listener instanceof TypedListener) {
				final Object evenListener = ((TypedListener) listener).getEventListener();
				if (evenListener instanceof ITextViewer) {
					return ((ITextViewer) evenListener).getDocument();
				}
			}
		}
		return null;
	}
}
