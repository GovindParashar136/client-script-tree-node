package org.gjp.forgerock.openam.auth.nodes;

import static org.forgerock.openam.auth.node.api.Action.send;
import static org.forgerock.openam.scripting.ScriptConstants.AUTHENTICATION_CLIENT_SIDE_NAME;

import java.util.Optional;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.scripting.Script;
import org.forgerock.openam.scripting.service.ScriptConfiguration;
import org.forgerock.util.Strings;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.shared.debug.Debug;

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = ClientScriptTreeNode.Config.class)
public class ClientScriptTreeNode extends SingleOutcomeNode {

	private final Config config;
	private final static String DEBUG_FILE = "ClientScriptedTreeNode";
	protected Debug debug = Debug.getInstance(DEBUG_FILE);

	public interface Config {
		@Attribute(order = 100)
		@Script(AUTHENTICATION_CLIENT_SIDE_NAME)
		ScriptConfiguration script();

		@Attribute(order = 200)
		String scriptResult();
	}

	@Inject
	public ClientScriptTreeNode(@Assisted Config config) throws NodeProcessException {
		this.config = config;
	}

	@Override
	public Action process(TreeContext context) throws NodeProcessException {
		Optional<String> result = context.getCallback(HiddenValueCallback.class).map(HiddenValueCallback::getValue)
				.filter(scriptOutput -> !Strings.isNullOrEmpty(scriptOutput));
		if (result.isPresent()) {
			JsonValue newSharedState = context.sharedState.copy();
			newSharedState.put(config.scriptResult(), result.get());
			debug.message("[" + this.getClass().getSimpleName() + "]" + "Client result is:\n" + result.get());
			return goToNext().replaceSharedState(newSharedState).build();
		} else {
			String clientSideScript = config.script().getScript();
			debug.message("[" + this.getClass().getSimpleName() + "] " + "Client script is:\n" + clientSideScript + "\n"
					+ "Client result name: " + config.scriptResult());
			ScriptTextOutputCallback scriptCallback = new ScriptTextOutputCallback(clientSideScript);
			HiddenValueCallback hiddenValueCallback = new HiddenValueCallback(config.scriptResult());
			ImmutableList<Callback> callbacks = ImmutableList.of(scriptCallback, hiddenValueCallback);
			return send(callbacks).build();
		}
	}
}