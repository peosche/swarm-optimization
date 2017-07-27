package AntColonyOptimization;

public class Modele {
 
	private AntColonyOptimization antSystem;
	
	
	public Modele() {
		this.antSystem = new AntColonyOptimization();
	}
	
	//m�thode d'initialisation de l'algorithme Ant System
	//appelle les m�thodes d'AntSystem pour cr�er le graphe et les agents
	public void initAntSystem() {
		this.antSystem.initGraph();
		this.antSystem.initAgents();
		this.antSystem.initPheromones();
		this.antSystem.initDistances();
	}
	
	//m�thode d'initialisation de l'algorithme Ant System avec un graphe de test (2 ponts)
	public void initAntSystemTest() {
		this.antSystem.initTestGraph();
		this.antSystem.initAgents();
		this.antSystem.initPheromones();
		//this.antSystem.initDistances();
		this.antSystem.initTestDistances();
	}
	
	
	//ex�cute une it�ration de l'algorithme (permet � tous les agents de g�n�rer un parcours)
	//met � jour les ph�romones
	public void executeAntSystemAlgorithm() {
		for (int i = 0; i < 100; i++) {
			this.antSystem.iteration();
			this.antSystem.bestTour();
			this.antSystem.globalPheromoneUpdate();
			this.antSystem.updateDisplayAttributes();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
	
	public AntColonyOptimization getAntSystem() {
		return antSystem;
	}
	
	public void getBestTour() {
		System.out.println(this.antSystem.getBestTourNodes());
	}

	public static void main(String[] args) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		Modele modele = new Modele();
		//modele.initAntSystem();
		modele.initAntSystemTest();
		modele.getAntSystem().getGraph().addAttribute("ui.stylesheet", "url('./src/AntSystem/stylesheetAS')");
		modele.fancyDisplayInit();
		modele.executeAntSystemAlgorithm();
		modele.getBestTour();
		//modele.printAgents();
	}
}
