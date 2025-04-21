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

	private boolean finished = false;
	private int exit;


	public MyCollectMoveBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	@Override
	public void action() {
		
		String agentName = ((MyCollectAgent) this.myAgent).getLocalName();
		MapRepresentation map = ((MyCollectAgent) this.myAgent).getMyMap();
		String color = Global.getColorForAgent(agentName);
		Observation resType = ((MyCollectAgent) this.myAgent).getMyTreasureType();
		Location myPosition =((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> liste_pos_ressources = ((MyCollectAgent)this.myAgent).getListe_pos_ressources();
		
		
		ArrayList<Tuple3<String, Integer, Instant>> listMyType = liste_pos_ressources.get(resType.toString());
		if(resType.toString().equals("Any")) {
			listMyType = liste_pos_ressources.get("Gold");
			listMyType.addAll(liste_pos_ressources.get("Diamond"));
		}
		/*
		List<String> shortestPath = map.getShortestPath(myPosition.toString(), listMyType.get(0).getFirst());

		if (shortestPath == null || shortestPath.isEmpty()) {
		    System.out.println("⚠️ Impossible de trouver un chemin vers le premier élément " + listMyType.get(0));
		    return;
		}*/
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
