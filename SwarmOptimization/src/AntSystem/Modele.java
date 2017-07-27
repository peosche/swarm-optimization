package AntSystem;

public class Modele {
 
	private AntSystem antSystem;
	
	
	public Modele() {
		this.antSystem = new AntSystem();
	}
	
	//méthode d'initialisation de l'algorithme Ant System
	//appelle les méthodes d'AntSystem pour créer le graphe et les agents
	public void initAntSystem() {
		this.antSystem.initGraph();
		this.antSystem.initAgents();
		this.antSystem.initPheromones();
		this.antSystem.initDistances();
	}
	
	//méthode d'initialisation de l'algorithme Ant System avec un graphe de test (2 ponts)
	public void initAntSystemTest() {
		this.antSystem.initTestGraph();
		this.antSystem.initAgents();
		this.antSystem.initPheromones();
		this.antSystem.initDistances();
		this.antSystem.initTestDistances();
	}
	
	
	//exécute une itération de l'algorithme (permet à tous les agents de générer un parcours)
	//met à jour les phéromones
	public void executeAntSystemAlgorithm() {
		for (int i = 0; i < 300; i++) {
			this.antSystem.iteration();
			this.antSystem.bestTour();
			this.antSystem.updatePheromone();
			this.antSystem.updateDisplayAttributes();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.antSystem.getSolution();
	}
	
	//affiche les informations de chaque agent
	public void printAgents() {
		for (Agent agent : this.antSystem.getAgents()) {
			System.out.println(agent);
		}
	}
	
	public void fancyDisplayInit() {
		this.antSystem.fancyDisplayInit();
	}
	
	public void displayGraph() {
		this.antSystem.getGraph().display();
	}
	
	public AntSystem getAntSystem() {
		return antSystem;
	}
	
	public void getBestTour() {
		System.out.println(this.antSystem.getBestTourNodes());
	}

	public static void main(String[] args) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		Modele modele = new Modele();
		modele.initAntSystem();
		//modele.initAntSystemTest();
		modele.getAntSystem().getGraph().addAttribute("ui.stylesheet", "url('./src/AntSystem/stylesheetAS')");
		modele.fancyDisplayInit();
		modele.executeAntSystemAlgorithm();
		modele.getBestTour();
		//modele.printAgents();
	}
}
