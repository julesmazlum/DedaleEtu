package eu.su.mas.dedaleEtu.mas.myBehaviours.Explore;

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
import eu.su.mas.dedaleEtu.mas.myAgents.MyExploreAgent;
import jade.core.behaviours.SimpleBehaviour;


public class MyExploreMoveToHelpBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyExploreMoveToHelpBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	@Override
	public void action() {
		
		/*
		 * Données
		 */
		
		/* Affichage */
		String agentName = ((MyExploreAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		MapRepresentation map = ((MyExploreAgent) this.myAgent).getMyMap();
		Observation resType = ((MyExploreAgent) this.myAgent).getMyTreasureType();
		Location myPosition =((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>>> listTreasureData = ((MyExploreAgent)this.myAgent).getListTreasureData();
		ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>> listMyType = listTreasureData.get(resType.toString());
		ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>> listGoldType = listTreasureData.get("Gold");
		
		/* Gestion des agents */
		int nbExplored = ((MyExploreAgent) this.myAgent).getNbExplored();
		
		// Dans le cas ou l'agent à 2 types de trésor
		if(resType.toString().equals("Any")) {
			listMyType = listTreasureData.get("Gold");
			listMyType.addAll(listTreasureData.get("Diamond"));
		}
    	
		int qteToUnlock = 0;
    	if(listGoldType!=null) {
    		//parcours de la liste
    		for(Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple : listGoldType) {
    			//si la quantité de ressource > 0 ET ((myLockPicking et myStretgh sont assez) OU (le coffre est déjà ouvert))
        		if(tuple.get_2()>0 && tuple.get_3().getThird()==0) {
        			qteToUnlock++;
        		}
        	}
    	}
		
		
		/* Calcul distance de trésor le plus proche */
		// initialiser la distance à + l'infini
		int maxUnclockQte = 0;
		// initialiser le chemin le plus court à null
		List<String> pathToMaxUnlock = null;
		List<String> path = null;

    	
		if(qteToUnlock > 0 && nbExplored>1) {
			System.out.println(color+ agentName+" : Je calcule le chemin vers la plus grosse ressource.");
    		//Parcourir la liste de ressources
    		for(Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple : listGoldType) {
    			//les ressources disponibles seul
    			if(tuple.get_2()>0 && tuple.get_3().getThird()==0) {
    				
    				//si le chemin est plus court que le plus court courant, on le garde
    				if(tuple.get_2()>maxUnclockQte) {
    					//calculer le chemin
        				path = map.getShortestPath(myPosition.toString(), tuple.get_1());
        				maxUnclockQte = tuple.get_2();
        				pathToMaxUnlock = path;
    				}
    			}
    		}
    		
    		
    		if(pathToMaxUnlock.size()!=0) {
    			pathToMaxUnlock.remove(pathToMaxUnlock.size()-1);
    		}
    		/*
    		if(pathToMaxUnlock.size()!=0) {
    			pathToMaxUnlock.remove(pathToMaxUnlock.size()-1);
    		}
    		*/
    		
    		
    		System.out.println(color+ agentName+" : J'y vais.");
    		Global.move(pathToMaxUnlock, (AbstractDedaleAgent) myAgent, color, agentName);
		}
		
		
		
		
		
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
