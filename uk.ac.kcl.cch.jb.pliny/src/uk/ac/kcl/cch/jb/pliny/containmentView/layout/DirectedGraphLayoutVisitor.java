/*******************************************************************************
 * Copyright (c) 2007 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.containmentView.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.NodeList;

import uk.ac.kcl.cch.jb.pliny.containmentView.parts.ContainmentItemPart;
import uk.ac.kcl.cch.jb.pliny.containmentView.parts.ContainmentLinkPart;
import uk.ac.kcl.cch.jb.pliny.containmentView.parts.ContainmentSetPart;

/**
 * Visitor with support for populating nodes and edges of DirectedGraph
 * from model objects.   This code was borrowed for use within Pliny 
 * with only minor conceptual changes from 
 * <code>com.realpersist.gef.schemaeditor</code> with thanks to 
 * the original authors.
 * 
 * @author Phil Zoio
 * @author a few changes by John Bradley
 */

public class DirectedGraphLayoutVisitor {
	Map partToNodesMap;
	DirectedGraph graph;

	public DirectedGraphLayoutVisitor() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Public method for reading graph nodes
	 */
	public void layoutDiagram(ContainmentSetPart diagram)
	{

		partToNodesMap = new HashMap();
		
		graph = new DirectedGraph();
		addNodes(diagram);
		if (graph.nodes.size() > 0)
		{	
			addEdges(diagram);
			//new NodeJoiningDirectedGraphLayout().visit(graph);
			new DirectedGraphLayout().visit(graph);
			applyResults(diagram);
		}

	}

	//******************* ContainmentSetPart contribution methods **********/

	protected void addNodes(ContainmentSetPart diagram)
	{
		GraphAnimation.recordInitialState(diagram.getFigure());
		// IFigure fig = diagram.getFigure();
		for (int i = 0; i < diagram.getChildren().size(); i++)
		{
			ContainmentItemPart tp = (ContainmentItemPart) diagram.getChildren().get(i);
			addNodes(tp);
		}
	}

	/**
	 * Adds nodes to the graph object for use by the GraphLayoutManager
	 */
	protected void addNodes(ContainmentItemPart itemPart)
	{
		Node n = new Node(itemPart);
		n.width = itemPart.getFigure().getPreferredSize(400, 300).width;
		n.height = itemPart.getFigure().getPreferredSize(400, 300).height;
		n.setPadding(new Insets(10, 8, 10, 12));
		partToNodesMap.put(itemPart, n);
		graph.nodes.add(n);
	}

	protected void addEdges(ContainmentSetPart diagram)
	{
		for (int i = 0; i < diagram.getChildren().size(); i++)
		{
			ContainmentItemPart itemPart = (ContainmentItemPart) diagram.getChildren().get(i);
			addEdges(itemPart);
		}
	}

	//******************* ContainmentItemPart contribution methods **********/

	protected void addEdges(ContainmentItemPart tablePart)
	{
		List outgoing = tablePart.getSourceConnections();
		for (int i = 0; i < outgoing.size(); i++)
		{
			ContainmentLinkPart linkPart = (ContainmentLinkPart) tablePart.getSourceConnections().get(i);
			addEdges(linkPart);
		}
	}

	//******************* ContainmentLinkPart contribution methods **********/

	protected void addEdges(ContainmentLinkPart linkPart)
	{
		GraphAnimation.recordInitialState((Connection) linkPart.getFigure());
		Node source = (Node) partToNodesMap.get(linkPart.getSource());
		Node target = (Node) partToNodesMap.get(linkPart.getTarget());
		Edge e = new Edge(linkPart, source, target);
		e.weight = 2;
		graph.edges.add(e);
		partToNodesMap.put(linkPart, e);
	}

	//******************* ContainmentSetPart apply methods **********/

	protected void applyResults(ContainmentSetPart diagram)
	{
		for (int i = 0; i < diagram.getChildren().size(); i++)
		{
			ContainmentItemPart tablePart = (ContainmentItemPart) diagram.getChildren().get(i);
			applyResults(tablePart);
		}
	}

	//******************* ContainmentItemPart apply methods **********/

	public void applyResults(ContainmentItemPart tablePart)
	{

		Node n = (Node) partToNodesMap.get(tablePart);
		IFigure tableFigure = tablePart.getFigure();

		Rectangle bounds = new Rectangle(n.x, n.y, tableFigure.getPreferredSize().width,
				tableFigure.getPreferredSize().height);

		tableFigure.setBounds(bounds);

		for (int i = 0; i < tablePart.getSourceConnections().size(); i++)
		{
			ContainmentLinkPart relationship = (ContainmentLinkPart) tablePart.getSourceConnections().get(i);
			applyResults(relationship);
		}
	}

	//******************* ContainmentLinkPart apply methods **********/

	protected void applyResults(ContainmentLinkPart relationshipPart)
	{

		Edge e = (Edge) partToNodesMap.get(relationshipPart);
		NodeList nodes = e.vNodes;

		PolylineConnection conn = (PolylineConnection) relationshipPart.getConnectionFigure();
		//conn.setTargetDecoration(new PolygonDecoration());
        //System.out.println("applyResults: conn:"+conn);
		if (nodes != null)
		{
			List bends = new ArrayList();
			for (int i = 0; i < nodes.size(); i++)
			{
				Node vn = nodes.getNode(i);
				int x = vn.x;
				int y = vn.y;
				if (e.isFeedback)
				{
					bends.add(new AbsoluteBendpoint(x, y + vn.height));
					bends.add(new AbsoluteBendpoint(x, y));

				}
				else
				{
					bends.add(new AbsoluteBendpoint(x, y));
					bends.add(new AbsoluteBendpoint(x, y + vn.height));
				}
			}
			conn.setRoutingConstraint(bends);
	        //System.out.println("--- bends: "+bends);
		}
		else
		{
			conn.setRoutingConstraint(Collections.EMPTY_LIST);
		}
	}

}
