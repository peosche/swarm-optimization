package AntSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.graphstream.algorithm.generator.*;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

public class AntSystem {

	public static final int NB_NODES = 10;//nombre de noeuds du graphe
	public static final int NB_AGENTS = 10;//nombre d'agents parcourant le graphe
	public static final int DEGREE_GRAPH = 10;//degrés du graphe
	public static final double INITIAL_PHEROMONE_VALUE = 0.1;//valeur initiale des phéromones sur chaque arête
	public static final int MIN_DISTANCE = 10;//distance minimale reliant 2 noeuds
	public static final int MAX_DISTANCE = 200;//distance maximale reliant 2 noeuds
	public static final String STARTING_NODE = "0";//id du noeud de départ
	public static final String ENDING_NODE = "9";//id du noeud d'arrivée
	public static final double ALPHA = 1;//importance des phéromones dans la state transition probability 
	public static final double BETA = 0.3;//importance de l'heuristique dans la state transition probability
	public static final double RHO = 0.1;//paramètre d'évaporation des phéromones
	
	private Graph graph;//graphe représentant l'environnement
	private ArrayList<Agent> agents;//liste de tous les agents naviguant dans le graphe
	private ArrayList<String> bestTourEdges;//liste des id des arêtes appartenant au meilleur chemin de l'itération courante
	private ArrayList<String> bestTourNodes;//liste des id des noeuds appartenant au meilleur chemin de l'itération courante
	private double bestTourDistance;//distance totale du meilleur chemin de l'itération courante 
	private SpriteManager spriteManagerPheromone;//utilisé pour gérer l'ensemble des sprites accrochés aux arêtes indiquant les phéromones
	
	public AntSystem() {
		this.graph = new SingleGraph("AntSystem");
		this.agents = new ArrayList<>();
		this.bestTourEdges = new ArrayList<>();
		this.bestTourNodes = new ArrayList<>();
		this.bestTourDistance = Double.MAX_VALUE;
		this.spriteManagerPheromone = new SpriteManager(this.graph);
	}
	
	//crée un graphe aléatoire avec ses noeuds et ses arêtes
	public void initGraph() {
		//Generator gen = new RandomGenerator(AntSystem.DEGREE_GRAPH);
		Generator gen = new RandomGenerator(AntSystem.DEGREE_GRAPH, false, true);
		gen.addSink(this.graph);
		gen.begin();
		while (this.graph.getNodeCount() < AntSystem.NB_NODES && gen.nextEvents());
		gen.end();
	}
	
	//crée un graphe de test suivant l'expérience des deux ponts
	public void initTestGraph() {
		this.graph.addNode("0");
		this.graph.addNode("1");
		this.graph.addNode("2");
		this.graph.addNode("9");
		this.graph.addEdge("0_1","0","1", true);
		this.graph.addEdge("0_2","0","2", true);
		this.graph.addEdge("1_9","1","9", true);
		this.graph.addEdge("2_9","2","9", true);
	}
	
	//initialise les agents
	public void initAgents() {
		for (int i = 0; i < AntSystem.NB_AGENTS; i++) {
			this.agents.add(new Agent());
		}
	}
	
	//initialise les pheromones de chaque arête
	public void initPheromones() {
		for(Edge edge : graph.getEachEdge()) {
			edge.addAttribute("pheromone", AntSystem.INITIAL_PHEROMONE_VALUE);
		}
	}
	
	//initialise aléatoirement les distances de chaque arête
	public void initDistances() {
		Random random = new Random();
		double randomDistance;
		for(Edge e:graph.getEachEdge()) {
			randomDistance = random.nextInt(AntSystem.MAX_DISTANCE + 1 - AntSystem.MIN_DISTANCE) + AntSystem.MIN_DISTANCE;
			e.addAttribute("distance", randomDistance);
		}
	}
	
	//initialise les distances de chaque arête afin de créer des chemins proches
	public void initTestDistances() {
		this.graph.getEdge("0_1").addAttribute("distance", 100.0);
		this.graph.getEdge("0_2").addAttribute("distance", 10.0);
		this.graph.getEdge("1_9").addAttribute("distance", 1.0);
		this.graph.getEdge("2_9").addAttribute("distance", 200.0);
	}
	
	//méthode faisant une itération de l'algorithme : génère un parcours complet pour chaque agent
	public void iteration() {
		//réinitialisation des caractéristiques relatives au dernier parcours des agents
		for(Agent agent : this.agents) {
			agent.resetTour(AntSystem.STARTING_NODE);
		}
		
		//tant que tous les agents n'ont pas terminé leur parcours
		while(!this.allPathsFinished()) {
			for(Agent agent : this.agents) {
				//System.out.println(agent);
				//si l'agent courant n'a pas terminé son parcours ou s'il n'est pas bloqué
				if (!agent.hasFinishedPath() && !this.getPossibleEdges(agent).isEmpty()) {
					Edge edge = this.step(agent);
					if (edge != null) {//si l'agent n'est pas bloqué à son noeud courant
						agent.addVisitedEdge(edge.getId());
						agent.addVisitedNode(edge.getNode1().getId());
						agent.addDistance((double)edge.getAttribute("distance"));
						//System.out.println(edge.getNode0().getId()+" "+edge.getNode1().getId());
						agent.setCurrentNodeId(edge.getNode1().getId());
					}
				}
			}
		}
	}
	
	//méthode faisant avancer l'agent passé en paramètre d'un pas
	public Edge step(Agent agent) {
		ArrayList<Edge> edgesList = this.getPossibleEdges(agent);
		Edge chosenEdge = null;
		if (!edgesList.isEmpty()) {//s'il existe au moins une arête à emprunter à partir du noeud courant
			//calcul de la somme totale du dénominateur
			double denominator = 0;
			for (Edge edge : edgesList) {
				denominator += Math.pow((double) edge.getAttribute("pheromone"), AntSystem.ALPHA)
						* Math.pow((1.0/(double) edge.getAttribute("distance")), AntSystem.BETA);
			}
			
			//calcul de la probabilité finale pour chaque arête
			//et tirage d'une variable aléatoire déterminant l'arête à choisir
			double numerator = 0;
			double randomVariable = Math.random();
			double stateTransition = 0;
			double stateTransitionSum = 0;
			for (Edge edge : edgesList) {
				numerator = Math.pow((double) edge.getAttribute("pheromone"), AntSystem.ALPHA)
						* Math.pow((1.0/(double) edge.getAttribute("distance")), AntSystem.BETA);
				stateTransition = numerator / denominator;
				stateTransitionSum += stateTransition;
				//System.out.println(edge.getId()+" "+stateTransition);
				//System.out.println(randomVariable);
				if (stateTransitionSum >= randomVariable) {
					chosenEdge = edge;
					break;
				}
				//System.out.println(currentEdge+" : "+stateTransition);
			}
		}
		return chosenEdge;
	}
	
	//retourne une arraylist des arêtes pouvant être empruntées pour l'agent passé en paramètre et son noeud courant
	public ArrayList<Edge> getPossibleEdges(Agent agent) {
		//récupère toutes les arêtes quittant le noeud courant
		//System.out.println("currentnodeid "+agent.getCurrentNodeId());
		Iterator<Edge> it = this.graph.getNode(agent.getCurrentNodeId()).getEachLeavingEdge().iterator();
		
		//suppression des arêtes déjà empruntées par l'agent
		//+ résolution du bug de la fonction getEachLeavingEdge()
		ArrayList<Edge> edgesList = new ArrayList<>();
		//System.out.println();
		//System.out.print(agent.getCurrentNodeId()+" ");
		while (it.hasNext()) {
			Edge edge = (Edge) it.next();
			//System.out.print(edge.getId()+" ");
			if (edge.getNode0().getId().equals(agent.getCurrentNodeId())) {//si le premier noeud de l'arête est le même que le noeud courant (cas normal)
				//System.out.println(edge.getId());
				//suppression des arêtes déjà empruntées par l'agent
				if (!agent.hasVisitedEdge(edge.getId())) {
					edgesList.add(edge);
				}
			}
		}
		return edgesList;
	}
	
	//méthode retournant true si tous les agents ont terminé leur parcours (en étant arrivé au noeud objectif ou en étant bloqué)
	public boolean allPathsFinished() {
		boolean finished = true;
		for(Agent agent : this.agents) {
			if (!agent.hasFinishedPath() && !this.getPossibleEdges(agent).isEmpty()) finished = false;
		}
		return finished;
	}
	
	//récupère le meilleur parcours parmi tous les agents
	public void bestTour() {
		ArrayList<String> bestEdges = new ArrayList<>();
		ArrayList<String> bestNodes = new ArrayList<>();
		double totalDistance = Double.MAX_VALUE;
		for (Agent agent : this.agents) {
			if (agent.hasVisitedNode(AntSystem.ENDING_NODE)) {//si l'agent a trouvé un chemin pour aller au noeud objectif
				if (agent.getTotalDistance() < totalDistance) {
					bestEdges = agent.getVisitedEdges();
					bestNodes = agent.getVisitedNodes();
					totalDistance = agent.getTotalDistance();
				}
			}
		}
		if (!bestEdges.isEmpty()) {//fonction d'évaluation
			if (totalDistance <= bestTourDistance) {
				this.bestTourEdges = bestEdges;
				this.bestTourNodes = bestNodes;
				this.bestTourDistance = totalDistance;
				//System.out.println("-----------------------------");
				//System.out.println("bestTourEdges : "+this.bestTourEdges);
				//System.out.println("bestTourNodes : "+this.bestTourNodes);
				//System.out.println("totalDistance : "+totalDistance);
			}

		} else {
			//System.out.println("Aucun agent n'a pu compléter de parcours");
			//System.out.println("bestTour : []");
			//System.out.println("totalDistance : -");
		}
	}
	
	//met à jour les phéromones
	public void updatePheromone() {
		Iterator<Edge> it = this.graph.getEdgeIterator();
		while (it.hasNext()) {//toutes les arêtes
			Edge e = it.next();
			double deltaTauTotal = 0;
			for (Agent agent : this.agents) {
				double deltaTau = 0;
				if (agent.hasFinishedPath()) {//si l'agent a trouvé une solution
					if (agent.hasVisitedEdge(e.getId())) {//si l'agent a visité l'arête courante
						deltaTau = 1.0 / agent.getTotalDistance();
					}
				}
				deltaTauTotal += deltaTau;
			}
			double oldPheromone = e.getAttribute("pheromone");
			double newPheromone = (1.0 - AntSystem.RHO) * oldPheromone
					+ deltaTauTotal;
			e.changeAttribute("pheromone", (double)Math.round(newPheromone * 10000000d) / 10000000d);
			//System.out.println(e.getId());
			//System.out.println("oldPheromone : "+oldPheromone);
			//System.out.println("newPheromone : "+newPheromone);
		}
	}
	
	public ArrayList<Agent> getAgents() {
		return agents;
	}
	
	public ArrayList<String> getBestTourNodes() {
		return bestTourNodes;
	}

	//affichage basique du graphe
	public void simpleDisplay() {
		this.graph.display();
	}
	
	//initialisation de l'affichage avancé du graphe
	public void fancyDisplayInit() {
        this.graph.addAttribute("ui.quality");
        this.graph.addAttribute("ui.antialias");
        
        //label des noeuds (id)
        Iterator<Node> itNode = this.graph.getNodeIterator();
        Node n;
        while (itNode.hasNext()) {
        	n = itNode.next();
        	n.addAttribute("ui.label", n.getId());
        }
        
        //label, taille et couleur des arêtes (phéromone + distance)
        Iterator<Edge> itEdge = this.graph.getEdgeIterator();
        Edge edge;
        while (itEdge.hasNext()) {
        	edge = itEdge.next();
        	//label des arêtes (phéromones + distance)
        	edge.addAttribute("ui.label", (Double)(edge.getAttribute("distance")));
        	Sprite pheromoneSprite = this.spriteManagerPheromone.addSprite(edge.getId());
        	pheromoneSprite.addAttribute("ui.class", "pheromone");
        	pheromoneSprite.attachToEdge(edge.getId());
        	pheromoneSprite.setPosition(0.5);
        	pheromoneSprite.addAttribute("ui.label", (Double)edge.getAttribute("pheromone"));
        	//taille des arêtes (phéromones)
        	edge.addAttribute("ui.size", ((double)edge.getAttribute("pheromone"))*10);
        	//couleur des arêtes (phéromones)
        	edge.addAttribute("ui.color", (double)edge.getAttribute("pheromone"));
        }
        
        //outline des noeuds de départs et d'arrivée
        this.graph.getNode(AntSystem.STARTING_NODE).addAttribute("ui.class", "start");
        this.graph.getNode(AntSystem.ENDING_NODE).addAttribute("ui.class", "objective");
        this.graph.display();
	}
	
	public void updateDisplayAttributes() {
        //colorie les arêtes appartenant au meilleur parcours
        /**if (!this.bestTourEdges.isEmpty()) {
        	Iterator<Edge> it = this.graph.getEdgeIterator();
        	while (it.hasNext()) {
        		Edge edge = it.next();
        		if (this.bestTourEdges.contains(edge.getId())) {
        			edge.addAttribute("ui.class", "bestTour");
        		} else {
        			edge.removeAttribute("ui.class");
        		}
        	}
        }**/
        
      //colorie les noeuds appartenant au meilleur parcours
        if (!this.bestTourNodes.isEmpty()) {
			Iterator<Node> it = this.graph.getNodeIterator();
			while (it.hasNext()) {
				Node node = it.next();
				if (this.bestTourNodes.contains(node.getId())) {
					//node.addAttribute("ui.class", ",bestTour");
					addNodeClass(node, "bestTour");
				} else {
					removeNodeClass(node, "bestTour");
				}
			}
		}
        
        //update des valeurs des phéromones
		//update de la taille des arêtes selon la quantité de phéromones déposée
		//update de la couleur des arêtes selon la quantité de phéromones déposée
        Iterator<Edge> itEdge = this.graph.getEdgeIterator();
        while (itEdge.hasNext()) {
        	Edge edge = itEdge.next();
        	//label des arêtes
        	Sprite pheromoneSprite = this.spriteManagerPheromone.getSprite(edge.getId());
        	pheromoneSprite.changeAttribute("ui.label", (double) edge.getAttribute("pheromone"));
        	//taille des arêtes
        	if ((double)edge.getAttribute("pheromone") > 0.1) {
        		edge.changeAttribute("ui.size", ((double)edge.getAttribute("pheromone"))*10);
        	}
        	//System.out.println((double)edge.getAttribute("ui.size"));
        	//couleur des arêtes
        	edge.changeAttribute("ui.color", (double)edge.getAttribute("pheromone"));
        }
	}
	
	//ajoute une nouvelle classe graphique à un noeud du graphe (sans écraser les autres éventuelles classes si elles existent)
    public void addNodeClass(Node node, String className) {
    	String classes = node.getAttribute("ui.class");
    	if (classes == null) {
    		node.addAttribute("ui.class", className);
    	} else {
    		classes += ","+className;
    		node.changeAttribute("ui.class", classes);
    	}
    }
    
  //supprime une nouvelle classe graphique à un noeud du graphe (sans écraser les autres éventuelles classes si elles existent)
    public void removeNodeClass(Node node, String className) {
    	String classes = node.getAttribute("ui.class");
    	if (classes != null) {
    		if (classes.contains(","+className)) {
	    		classes = classes.replace(","+className, "");
	    	} else if (classes.contains(className+",")) {
	    		classes = classes.replace(className+",", "");
	    	} else if (classes.contains(className)) {
	    		classes = classes.replace(className, "");
	    	}
    		node.changeAttribute("ui.class", classes);
    	}
    }
    
    public void getSolution() {
    	System.out.println(this.bestTourNodes);
    	System.out.println("Distance totale : "+this.bestTourDistance);
    }

	public Graph getGraph() {
		return graph;
	}
}