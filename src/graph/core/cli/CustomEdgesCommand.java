package graph.core.cli;

import graph.core.CommonConcepts;
import graph.core.CycDAG;
import graph.core.DAGEdge;
import graph.core.DAGNode;
import graph.core.DirectedAcyclicGraph;
import graph.core.Edge;
import graph.core.Node;
import graph.module.NLPToStringModule;
import graph.module.QueryModule;
import graph.module.RelatedEdgeModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import util.UtilityMethods;

public class CustomEdgesCommand extends CollectionCommand {
	@Override
	public String helpText() {
		return "{0} <node> T/N/O : Returns all edges for a given node, "
				+ "for three groups of edges: taxonomic, natural language, "
				+ "and other. The set of outputs consist of a predicate, "
				+ "the number of edges under that predicate, and a triple "
				+ "of edge, marked-up dagtotext of edge, and creator. This "
				+ "command is highly specialised and largely for the "
				+ "interface only.";
	}

	@Override
	public String shortDescription() {
		return "A highly customised command for returning all edges pertaining to a given node.";
	}

	@Override
	protected void executeImpl() {
		super.executeImpl();
		DAGPortHandler dagHandler = (DAGPortHandler) handler;
		DirectedAcyclicGraph dag = dagHandler.getDAG();
		RelatedEdgeModule relatedEdge = (RelatedEdgeModule) dag
				.getModule(RelatedEdgeModule.class);
		NLPToStringModule nlpModule = (NLPToStringModule) dag
				.getModule(NLPToStringModule.class);
		QueryModule querier = (QueryModule) dag.getModule(QueryModule.class);
		if (relatedEdge == null) {
			print("-1|Related Edge Module is not in use for this DAG.\n");
			return;
		}
		if (nlpModule == null) {
			print("-1|NLP Module is not in use for this DAG.\n");
			return;
		}
		if (querier == null) {
			print("-1|Query Module is not in use for this DAG.\n");
			return;
		}

		if (data.isEmpty()) {
			printErrorNoData();
			return;
		}

		// Split the data
		ArrayList<String> split = UtilityMethods.split(data, ' ');
		if (split.size() < 2) {
			print("-1|Enter two arguments: node and result type (T/N/O).\n");
			return;
		}

		// Get the node
		Node conceptNode = dag.findOrCreateNode(split.get(0), null);
		if (conceptNode == null) {
			print("-1|Could not parse node.\n");
			return;
		}

		// Run through every edge involving concept, enforcing range constraints
		// manually.
		String type = split.get(1);
		Collection<Edge> edges = relatedEdge.execute(conceptNode, "!1");
		SortedMap<DAGNode, SortedSet<Edge>> predEdges = new TreeMap<>(
				dagHandler.getComparator());
		for (Edge e : edges) {
			DAGNode pred = (DAGNode) e.getNodes()[0];
			if (isHierarchical(pred, dag)) {
				if (!type.equalsIgnoreCase("T"))
					continue;
			} else if (isNaturalLanguage(pred, querier, dag)) {
				if (!type.equalsIgnoreCase("N"))
					continue;
			} else {
				if (!type.equalsIgnoreCase("O"))
					continue;
			}
			
			// Initialise the sorted edge set per predicate
			SortedSet<Edge> predEdge = predEdges.get(pred);
			if (predEdge == null) {
				predEdge = new TreeSet<>();
				predEdges.put(pred, predEdge);
			}
			predEdge.add(e);
		}

		// Print the edges, in sorted and shortened order
		for (DAGNode key : predEdges.keySet()) {
			// Pred key
			print(dagHandler.textIDObject(key) + "|");
			Collection<Edge> sortedEdges = dagHandler.postProcess(
					predEdges.get(key), rangeStart_, rangeEnd_);
			// Num edges
			print(sortedEdges.size() + "|");

			// Edge triples
			for (Edge e : sortedEdges) {
				print(dagHandler.textIDObject(e) + "|"
						+ nlpModule.execute(true, e) + "|"
						+ ((DAGEdge) e).getCreator() + "|");
			}
			print("\n");
		}
	}

	/**
	 * If a given predicate is a natural language predicate.
	 * 
	 * @param predicate
	 *            The predicate to check.
	 * @param querier
	 *            The query module.
	 * @param dag
	 *            The DAG access.
	 * @return True if the predicate is natural language.
	 */
	private boolean isNaturalLanguage(DAGNode predicate, QueryModule querier,
			DirectedAcyclicGraph dag) {
		return predicate.equals(CommonConcepts.NLP_PREDICATE_STRING
				.getNode(dag))
				|| querier.prove(CommonConcepts.GENLPREDS.getNode(dag),
						predicate, CommonConcepts.TERM_STRING.getNode(dag));
	}

	/**
	 * If a given predicate is hierarchical.
	 * 
	 * @param predicate
	 *            The predicate to check.
	 * @param dag
	 *            The DAG access.
	 * @return True if the predicate is hierarchical.
	 */
	private boolean isHierarchical(DAGNode predicate, DirectedAcyclicGraph dag) {
		return predicate.equals(CommonConcepts.ISA.getNode(dag))
				|| predicate.equals(CommonConcepts.GENLS.getNode(dag))
				|| predicate.equals(CommonConcepts.GENLPREDS.getNode(dag))
				|| predicate.equals(CommonConcepts.GENLMT.getNode(dag));
	}
}
