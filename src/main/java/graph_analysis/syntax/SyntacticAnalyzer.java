package graph_analysis.syntax;

import bpmn.graph.Graph;
import bpmn.graph.Node;
import bpmn.types.process.BpmnProcessType;
import bpmn.types.process.Gateway;
import bpmn.types.process.Task;
import loops_management.Loop;
import loops_management.LoopFinder;

public class SyntacticAnalyzer
{
	private SyntacticAnalyzer()
	{

	}

	/**
	 * This method performs a basic syntactic analysis of the processes given as input.
	 * It compares, node after node, whether the current nodes have the same number of children
	 * and whether their children are equal.
	 * If it reaches a state where at least one of these two conditions are not satisfied, it returns false.
	 * Otherwise, if it has finished traversing the graph while always respecting these conditions, it returns true.
	 *
	 * @param graph1 the first graph to analyze
	 * @param graph2 the second graph to analyze
	 * @return true if graph1 and graph2 are syntactically equivalent
	 */
	public static boolean compare(final Graph graph1,
								  final Graph graph2)
	{
		final LoopFinder loopFinder1 = new LoopFinder(graph1);
		loopFinder1.findLoops();

		final LoopFinder loopFinder2 = new LoopFinder(graph2);
		loopFinder2.findLoops();

		return SyntacticAnalyzer.nodesMatch(loopFinder1, loopFinder2, graph1, graph2, graph1.initialNode(), graph2.initialNode(), null, null);
	}

	//Private methods

	private static boolean successorsMatch(final LoopFinder loopFinder1,
										   final LoopFinder loopFinder2,
										   final Graph graph1,
										   final Graph graph2,
										   final Node node1,
										   final Node node2,
										   final Node loop1Bound,
										   final Node loop2Bound)
	{
		boolean result = true;

		for (Node child1 : node1.childNodes())
		{
			boolean intermediateResult = false;

			for (Node child2 : node2.childNodes())
			{
				if (SyntacticAnalyzer.nodesMatch(loopFinder1, loopFinder2, graph1, graph2, child1, child2, loop1Bound, loop2Bound))
				{
					intermediateResult = true;
				}
			}

			result = result && intermediateResult;
		}

		return result;
	}

	private static boolean nodesMatch(final LoopFinder loopFinder1,
									  final LoopFinder loopFinder2,
									  final Graph graph1,
									  final Graph graph2,
									  final Node node1,
									  final Node node2,
									  final Node loop1Bound,
									  final Node loop2Bound)
	{
		if (node1.bpmnObject().type() != node2.bpmnObject().type())
		{
			return false;
		}

		if (node1.bpmnObject() instanceof Task)
		{
			return node1.bpmnObject().name().equals(node2.bpmnObject().name())
					&& SyntacticAnalyzer.successorsMatch(loopFinder1, loopFinder2, graph1, graph2, node1, node2, loop1Bound, loop2Bound);
		}
		else if (node1.bpmnObject() instanceof Gateway)
		{
			if (node1.bpmnObject().type() == BpmnProcessType.EXCLUSIVE_GATEWAY
					&& ((Gateway) node1.bpmnObject()).isMergeGateway()
					&& ((Gateway) node2.bpmnObject()).isMergeGateway())
			{
				if (loopFinder1.nodeIsInLoop(node1)
						&& loopFinder2.nodeIsInLoop(node2))
				{
					final Loop loop1 = loopFinder1.findInnerLoopOf(node1);
					final Loop loop2 = loopFinder2.findInnerLoopOf(node2);

					if (node1.equals(loop1.entryPoint())
							&& node2.equals(loop2.entryPoint()))
					{
						if ((loop1Bound != null && loop1Bound.equals(node1))
								|| (loop2Bound != null && loop2Bound.equals(node2)))
						{
							return node1.equals(node2);
						}

						//Disconnect loops entry points to avoid looping
						final Node loop1EntryAncestorFlow = loop1.entryPoint().parentNodes().iterator().next();
						final Node loop2EntryAncestorFlow = loop2.entryPoint().parentNodes().iterator().next();

						loop1EntryAncestorFlow.removeChildren();
						loop1.entryPoint().removeParents();
						loop2EntryAncestorFlow.removeChildren();
						loop2.entryPoint().removeParents();

						final boolean loopMatches = SyntacticAnalyzer.successorsMatch(loopFinder1, loopFinder2, graph1, graph2, loop1.entryPoint(), loop2.entryPoint(), loop1.entryPoint(), loop2.entryPoint());

						if (!loopMatches)
						{
							return false;
						}

						//Reconnect loops entry points
						loop1EntryAncestorFlow.addChild(loop1.entryPoint());
						loop1.entryPoint().addParent(loop1EntryAncestorFlow);
						loop2EntryAncestorFlow.addChild(loop2.entryPoint());
						loop2.entryPoint().addParent(loop2EntryAncestorFlow);

						final Node loop1ExitPoint = graph1.getNodeFromID(loop1.exitPoint().bpmnObject().id());
						final Node loop2ExitPoint = graph2.getNodeFromID(loop2.exitPoint().bpmnObject().id());

						//We assume one exit point child per loop
						Node exitChild1 = null;
						Node exitChild2 = null;

						for (Node child1 : loop1ExitPoint.childNodes())
						{
							if (!loop1.hasNode(child1))
							{
								exitChild1 = child1;
								break;
							}
						}

						for (Node child2 : loop2ExitPoint.childNodes())
						{
							if (!loop2.hasNode(child2))
							{
								exitChild2 = child2;
								break;
							}
						}

						if (exitChild1 == null || exitChild2 == null) throw new IllegalStateException();

						return SyntacticAnalyzer.successorsMatch(loopFinder1, loopFinder2, graph1, graph2, exitChild1, exitChild2, loop1Bound, loop2Bound);
					}
					else
					{
						return node1.childNodes().size() == node2.childNodes().size()
								&& node1.parentNodes().size() == node2.parentNodes().size()
								&& SyntacticAnalyzer.successorsMatch(loopFinder1, loopFinder2, graph1, graph2, node1, node2, loop1Bound, loop2Bound);
					}
				}
				else
				{
					return node1.childNodes().size() == node2.childNodes().size()
							&& node1.parentNodes().size() == node2.parentNodes().size()
							&& SyntacticAnalyzer.successorsMatch(loopFinder1, loopFinder2, graph1, graph2, node1, node2, loop1Bound, loop2Bound);
				}
			}
			else
			{
				return node1.childNodes().size() == node2.childNodes().size()
						&& node1.parentNodes().size() == node2.parentNodes().size()
						&& SyntacticAnalyzer.successorsMatch(loopFinder1, loopFinder2, graph1, graph2, node1, node2, loop1Bound, loop2Bound);
			}
		}
		else
		{
			return SyntacticAnalyzer.successorsMatch(loopFinder1, loopFinder2, graph1, graph2, node1, node2, loop1Bound, loop2Bound);
		}
	}
}
