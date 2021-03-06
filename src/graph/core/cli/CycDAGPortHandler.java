/*******************************************************************************
 * Copyright (C) 2013 University of Waikato, Hamilton, New Zealand.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Sam Sarjant - initial API and implementation
 ******************************************************************************/
package graph.core.cli;

import graph.core.DAGObject;
import graph.core.DirectedAcyclicGraph;
import graph.core.Node;
import graph.core.cli.comparator.CycStringCaseInsComparator;
import graph.core.cli.comparator.CycStringComparator;
import graph.core.cli.comparator.DefaultComparator;
import graph.core.cli.comparator.DepthComparator;
import graph.inference.Substitution;

import java.net.Socket;
import java.util.Map;

import core.CommandQueue;

public class CycDAGPortHandler extends DAGPortHandler {
	public CycDAGPortHandler(Socket aSocket, CommandQueue aQueue,
			DirectedAcyclicGraph dag) {
		super(aSocket, aQueue, dag);
	}

	@Override
	protected DefaultComparator getComparator() {
		if (get(SORT_ORDER).equals("depth"))
			return new DepthComparator(this);
		else if (get(SORT_ORDER).equals("alpha"))
			return new CycStringComparator(this);
		else if (get(SORT_ORDER).equals("alphaNoCase"))
			return new CycStringCaseInsComparator(this);
		return super.getComparator();
	}

	@Override
	public Object convertToComparable(Object o) {
		if (o == null)
			return null;
		if (o instanceof Substitution) {
			Map<String, Node> subMap = ((Substitution) o).getSubstitutionMap();
			return subMap.get(subMap.keySet().iterator().next());
		}
		return super.convertToComparable(o);
	}

	@Override
	public DAGObject convertToDAGObject(Object obj) {
		if (obj == null)
			return null;
		if (obj instanceof Substitution) {
			Map<String, Node> subMap = ((Substitution) obj)
					.getSubstitutionMap();
			if (subMap.isEmpty())
				return null;
			obj = subMap.get(subMap.keySet().iterator().next());
		}
		return super.convertToDAGObject(obj);
	}
}
