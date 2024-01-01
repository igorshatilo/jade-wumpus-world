package wumpusworld.core.agent.impl;

import wumpusworld.core.agent.EnvironmentState;

/**
 * @author Ravi Mohan
 * @author Ciaran O'Reilly
 */
public class DynamicEnvironmentState extends ObjectWithDynamicAttributes
		implements EnvironmentState {
	public DynamicEnvironmentState() {
	}

	@Override
	public String describeType() {
		return EnvironmentState.class.getSimpleName();
	}
}