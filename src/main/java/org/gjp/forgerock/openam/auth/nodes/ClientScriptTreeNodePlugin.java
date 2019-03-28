package org.gjp.forgerock.openam.auth.nodes;

import java.util.Collections;
import java.util.Map;

import org.forgerock.openam.auth.node.api.AbstractNodeAmPlugin;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.plugins.PluginException;

public class ClientScriptTreeNodePlugin extends AbstractNodeAmPlugin {

	private static String currentVersion = "1.0.0";

	@Override
	protected Map<String, Iterable<? extends Class<? extends Node>>> getNodesByVersion() {
		return Collections.singletonMap(ClientScriptTreeNodePlugin.currentVersion,
				Collections.singletonList(ClientScriptTreeNode.class));
	}

	@Override
	public void onInstall() throws PluginException {
		System.err.println("[ClientScriptTreeNodePlugin] - onInstall() called.");
		super.onInstall();
	}

	@Override
	public void upgrade(String fromVersion) throws PluginException {
		super.upgrade(fromVersion);
	}

	@Override
	public String getPluginVersion() {
		return ClientScriptTreeNodePlugin.currentVersion;
	}

}
