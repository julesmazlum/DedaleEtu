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
	private List<String> positions = new ArrayList<>();
	private String og = null;
	private boolean bool = true;
	
	public MyTankerBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}


	public void action() {
		
		String agentName = ((MyTankerAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		List<Couple<Location, List<Couple<Observation, String>>>> observations = ((AbstractDedaleAgent) this.myAgent).observe();
		
		if(og == null) {
			og = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition().toString();
			//Parcours des observations
			for (Couple<Location, List<Couple<Observation, String>>> locationCouple : observations) {
			    positions.add(locationCouple.getLeft().toString());
			}
			positions.remove(og);
		}
		
		String nextNode = null;		
		if(bool) {
			nextNode = positions.get((int) (Math.random() * positions.size())).toString();
			bool = false;
		}else {
			nextNode = og;
			bool = true;
		}
		
		System.out.println(color + agentName + " : Je me déplace aux alentours");
		move(nextNode);
	
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
	
	public void move(String nextNode) {
        try {
            boolean success = ((AbstractDedaleAgent) this.myAgent).moveTo(new GsLocation(nextNode));
            if (!success) {
                System.out.println("❌ Impossible de bouger vers " + nextNode);
                // tu peux décider ici de soit break, soit skip le node
                return; 
            }
        } catch (Exception e) {
            System.out.println("⚠️ ERREUR dans move vers " + nextNode + " : " + e.getMessage());
            return; // on stoppe la boucle si y'a une exception
        }

        try {
            this.myAgent.doWait(Global.temps);
        } catch (Exception e) {
            e.printStackTrace();
        }
	    
	}
	

}
