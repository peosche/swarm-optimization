package AntSystem;

import java.util.ArrayList;

public class Agent {
	
	private static int counter = 0;//compte le nombre d'instances pour g�n�rer des ids unique
	private int id;//id unique de l'agent
	private ArrayList<String> visitedEdges;//liste des id des ar�tes visit�es par l'agent
	private ArrayList<String> visitedNodes;//liste des id des noeuds visit�s par l'agent
	private ArrayList<Double> distances;//liste des distances que l'agent a parcouru pour arriver � currentNodeId
	private String currentNodeId;//id du noeud o� est plac� l'agent
	
	public Agent() {
		this.id = Agent.counter;
		this.visitedEdges = new ArrayList<>();
		this.visitedNodes = new ArrayList<>();
		this.distances = new ArrayList<>();
		this.currentNodeId = null;
		Agent.counter++;
	}
	
	//retourne vrai si l'ar�te pass�e en param�tre a d�j� �t� visit�e par l'agent
	public boolean hasVisitedEdge(String idEdge) {
		return this.visitedEdges.contains(idEdge);
	}
	
	public void addVisitedEdge(String edgeId) {
		this.visitedEdges.add(edgeId);
	}
	
	//retourne vrai si le noeud pass� en param�tre a d�j� �t� visit� par l'agent
	public boolean hasVisitedNode(String idNode) {
		return this.visitedNodes.contains(idNode);
	}
	
	public void addVisitedNode(String nodeId) {
		this.visitedNodes.add(nodeId);
	}	
	
	public void addDistance(double distance) {
		this.distances.add(distance);
	}
	
	//retourne vrai si l'agent a termin� son chemin
	public boolean hasFinishedPath() {
		return AntSystem.ENDING_NODE.equals(this.currentNodeId);
	}
	
	//r�cup�re la distance totale parcourue par l'agent
	public double getTotalDistance() {
		double totalDistance = 0;
		for (Double distance : this.distances) {
			totalDistance += distance;
		}
		return totalDistance;
	}
	
	public void resetTour(String startingNode) {
		this.visitedEdges.clear();
		this.visitedNodes.clear();
		this.visitedNodes.add(startingNode);
		this.distances.clear();
		this.currentNodeId = startingNode;
	}
	
	public void setCurrentNodeId(String currentNodeId) {
		this.currentNodeId = currentNodeId;
	}
	
	public String getCurrentNodeId() {
		return this.currentNodeId;
	}

	public int getId() {
		return id;
	}
	
	public ArrayList<String> getVisitedEdges() {
		return visitedEdges;
	}
	
	public ArrayList<String> getVisitedNodes() {
		return visitedNodes;
	}

	@Override
	public String toString() {
		String s = "Agent n�"+this.id+"\n";
		s += "Ar�tes emprunt�es : "+this.visitedEdges.toString();
		s += "\nNoeud courant : "+this.currentNodeId+" | Noeud objectif : "+AntSystem.ENDING_NODE+"\n";
		s += "Distance total parcourue : "+this.getTotalDistance()+"\n";
		return s;
	}
	
}