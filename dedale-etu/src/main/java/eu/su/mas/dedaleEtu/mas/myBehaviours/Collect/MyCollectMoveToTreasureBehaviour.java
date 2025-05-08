package eu.su.mas.dedaleEtu.mas.myBehaviours.Collect;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import global.Global;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.myAgents.MyCollectAgent;
import jade.core.behaviours.SimpleBehaviour;


public class MyCollectMoveToTreasureBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyCollectMoveToTreasureBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	@Override
	public void action() {
		
		/*
		 * Données
		 */
		
		/* Affichage */
		String agentName = ((MyCollectAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		MapRepresentation map = ((MyCollectAgent) this.myAgent).getMyMap();
		Observation resType = ((MyCollectAgent) this.myAgent).getMyTreasureType();
		Location myPosition =((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>>> listTreasureData = ((MyCollectAgent)this.myAgent).getListTreasureData();
		ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>> listMyType = listTreasureData.get(resType.toString());
		
		/* Gestion des agents */
		int myLockPicking = ((MyCollectAgent) this.myAgent).getMyLockPicking();
		int myStretgh = ((MyCollectAgent) this.myAgent).getMyStrentgh();
		
		// Dans le cas ou l'agent à 2 types de trésor
		if(resType.toString().equals("Any")) {
			listMyType = listTreasureData.get("Gold");
			listMyType.addAll(listTreasureData.get("Diamond"));
		}
		
		
		/* Calcul distance de trésor le plus proche */
		// initialiser la distance à + l'infini
		int shortestpathlen = 100000;
		// initialiser le chemin le plus court à null
		List<String> shortestPath = null;

		//sélectionner le chemin vers la première ressources
		List<String> path = null;
		String posRemove = listMyType.get(0).get_1();
		
		System.out.println(color+ agentName+" : Je calcule le chemin vers la ressource disponible la plus proche.");
		//Parcourir la liste de ressources
		for(Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple : listMyType) {
			//les ressources disponibles seul
			if((tuple.get_2()>0 && ((tuple.get_3().getFirst()<=myLockPicking && tuple.get_3().getSecond()<=myStretgh) || tuple.get_3().getThird()==1))) {
				//calculer le chemin
				path = map.getShortestPath(myPosition.toString(), tuple.get_1());
				
				//si le chemin est plus court que le plus court courant, on le garde
				if(path.size()<shortestpathlen) {
					shortestPath = path;
					shortestpathlen = shortestPath.size();
					posRemove = tuple.get_1();
				}
			}
		}
		
		
		System.out.println(color+ agentName+" : Chemin vers la ressource la plus proche calculée. C'est celle en "+posRemove);
		((MyCollectAgent) this.myAgent).setGoToTres(posRemove);
		
		
		System.out.println(color+ agentName+" : J'y vais.");
		Global.move(shortestPath, (AbstractDedaleAgent) myAgent, color, agentName);
		
		
		exit = 1;
		finished = true;
		return;
		
	}

	@Override
	public boolean done() {
		return finished;
	}
	
	@Override
    public int onEnd() {
       return exit;
    }

}
