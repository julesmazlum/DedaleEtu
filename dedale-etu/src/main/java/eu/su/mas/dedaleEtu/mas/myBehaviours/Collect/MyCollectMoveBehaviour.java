package eu.su.mas.dedaleEtu.mas.myBehaviours.Collect;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import dataStructures.tuple.Tuple3;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import global.Global;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.myAgents.MyCollectAgent;
import jade.core.behaviours.SimpleBehaviour;


public class MyCollectMoveBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyCollectMoveBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	@Override
	public void action() {
		
		/* Affichage */
		String agentName = ((MyCollectAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		MapRepresentation map = ((MyCollectAgent) this.myAgent).getMyMap();
		Observation resType = ((MyCollectAgent) this.myAgent).getMyTreasureType();
		Location myPosition =((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> liste_pos_ressources = ((MyCollectAgent)this.myAgent).getListe_pos_ressources();
		ArrayList<Tuple3<String, Integer, Instant>> listMyType = liste_pos_ressources.get(resType.toString());
		
		if(resType.toString().equals("Any")) {
			listMyType = liste_pos_ressources.get("Gold");
			listMyType.addAll(liste_pos_ressources.get("Diamond"));
		}
		

		int shortestpathlen = 100000;
		List<String> shortestPath = null;

		List<String> path = null;
		String posRemove = listMyType.get(0).getFirst();
		
		System.out.println(color+ agentName+" : Je calcule le chemin vers la ressource avec qte>0 la plus proche");
		for(Tuple3<String, Integer, Instant> tuple : listMyType) {
			if(tuple.getSecond()>0) {
				path = map.getShortestPath(myPosition.toString(), tuple.getFirst());
				if(path.size()<shortestpathlen) {
					shortestPath = path;
					shortestpathlen = shortestPath.size();
					posRemove = tuple.getFirst();
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
