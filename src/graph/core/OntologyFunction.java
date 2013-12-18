/*******************************************************************************
 * Copyright (C) 2013 University of Waikato, Hamilton, New Zealand
 ******************************************************************************/
package graph.core;

import graph.module.QueryModule;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public class OntologyFunction extends DAGNode implements Edge {
	private static final long serialVersionUID = 473544398260462641L;
	private Boolean anonymous_;
	protected Node[] nodes_;

	public OntologyFunction() {
		super();
	}

	public OntologyFunction(boolean anonymous, Node... nodes) {
		super();
		nodes_ = nodes;
		anonymous_ = anonymous;
		if (anonymous_)
			id_ = -1;
	}

	public OntologyFunction(DirectedAcyclicGraph dag, Node... nodes) {
		super();
		nodes_ = nodes;
		anonymous_ = checkIfAnonymous(dag);
		if (anonymous_)
			id_ = -1;
	}

	private boolean checkIfAnonymous(DirectedAcyclicGraph dag) {
		// Check if function is unreifiable
		QueryModule queryModule = (QueryModule) dag
				.getModule(QueryModule.class);
		// If nodes[0] is unreifiable OR is not a function
		if (queryModule.prove(CommonConcepts.ISA.getNode(dag), nodes_[0],
				CommonConcepts.UNREIFIABLE_FUNCTION.getNode(dag)))
			return true;
		if (!queryModule.prove(CommonConcepts.ISA.getNode(dag), nodes_[0],
				CommonConcepts.FUNCTION.getNode(dag)))
			return true;
		return false;
	}

	@Override
	public int compareTo(DAGObject o) {
		int result = super.compareTo(o);
		if (result != 0)
			return result;

		return toString().compareTo(o.toString());
	}

	@Override
	public boolean containsNode(Node node) {
		for (Node edgeNode : getNodes())
			if (edgeNode.equals(node))
				return true;
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		OntologyFunction other = (OntologyFunction) obj;
		if (!Arrays.equals(nodes_, other.nodes_))
			return false;
		return true;
	}

	@Override
	public String getIdentifier() {
		if (id_ != -1)
			return id_ + "";

		StringBuffer buffer = new StringBuffer("(");
		boolean first = true;
		for (Node n : nodes_) {
			if (!first)
				buffer.append(" ");
			buffer.append(n.getIdentifier());
			first = false;
		}
		buffer.append(")");
		return buffer.toString();
	}

	@Override
	public String getName() {
		return "(" + StringUtils.join(nodes_, ' ') + ")";
	}

	@Override
	public Node[] getNodes() {
		return nodes_;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + Arrays.hashCode(nodes_);
		return result;
	}

	@Override
	public boolean isAnonymous() {
		return anonymous_;
	}

	@Override
	public String toString(boolean useIDs) {
		if (!useIDs)
			return toString();
		StringBuffer buffer = new StringBuffer("(");
		boolean first = true;
		for (Node n : nodes_) {
			if (!first)
				buffer.append(" ");
			buffer.append(n.getIdentifier());
			first = false;
		}
		buffer.append(")");
		return buffer.toString();
	}
}
