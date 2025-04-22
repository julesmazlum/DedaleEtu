package eu.su.mas.dedaleEtu.mas.myBehaviours.Tanker;

import java.util.ArrayList;
import java.util.List;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.myAgents.MyTankerAgent;
import global.Global;
import jade.core.behaviours.SimpleBehaviour;


public class MyTankerBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;
	private int exit;
	
	public MyTankerBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}


	public void action() {
		
		String agentName = ((MyTankerAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		String posSender = ((MyTankerAgent) this.myAgent).getPosSender();
		List<String> listToMove = new ArrayList<String>();
		List<Couple<Location, List<Couple<Observation, String>>>> observations = ((AbstractDedaleAgent) this.myAgent).observe();
		
		//Si bloque un autre agent, alors doit se déplacer
		if(posSender!=null) {
			//Parcours des observations
			for (Couple<Location, List<Couple<Observation, String>>> locationCouple : observations) {
			    Location location = locationCouple.getLeft();
			    //Liste des positions accessibles
			    listToMove.add(location.toString());
			}
			//On retire la position actuelle et celle de l'agent qui demande à ce qu'on se déplace
			listToMove.remove(myPosition.toString());
			listToMove.remove(posSender);
			
			//Choix aléatoire parmis les positions restantes
			String pos = null;
			if(listToMove.size()==0) {
				System.out.println(color+agentName+" : Je suis dans une impasse.");
			}else {
				
				pos = listToMove.get((int)(Math.random() * listToMove.size()));
				
				if(((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(pos))) {
					System.out.println(color+agentName+" : J'ai réussi à me déplacer pour laisser le chemin.");
					((MyTankerAgent) this.myAgent).setPosSender(null);
				}else {
					System.out.println(color+agentName+" : Je n'ai pas réussi à me déplacer pour laisser le chemin.");
				}
				
			}
			((MyTankerAgent) this.myAgent).setPosSender(null);
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
