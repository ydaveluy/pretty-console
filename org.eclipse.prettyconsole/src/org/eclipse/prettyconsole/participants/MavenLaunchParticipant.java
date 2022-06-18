package org.eclipse.prettyconsole.participants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.m2e.internal.launch.IMavenLaunchParticipant;
import org.eclipse.prettyconsole.preferences.PreferenceUtils;

// Enable maven colors automatically
@SuppressWarnings("restriction")
public class MavenLaunchParticipant implements IMavenLaunchParticipant {

	@Override
	public String getProgramArguments(ILaunchConfiguration launchCfg, ILaunch launch, IProgressMonitor progMonitor) {
		return PreferenceUtils.isPrettyConsoleEnabled() ? "-Dstyle.color=always" : "";
	}

	@Override
	public List<ISourceLookupParticipant> getSourceLookupParticipants(ILaunchConfiguration launchCfg, ILaunch launch,
			IProgressMonitor progMonitor) {
		return new ArrayList<>();
	}

	@Override
	public String getVMArguments(ILaunchConfiguration launchCfg, ILaunch launch, IProgressMonitor progMonitor) {
		return PreferenceUtils.isPrettyConsoleEnabled() ? "-Djansi.passthrough=true" : "";
	}

}
