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
		
		String initPosition = ((MyTankerAgent) this.myAgent).getInitPosition();
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		boolean needToMove = ((MyTankerAgent) this.myAgent).getNeedToMove();
		List<String> listToMove = new ArrayList<String>();
		
		List<Couple<Location, List<Couple<Observation, String>>>> observations = ((AbstractDedaleAgent) this.myAgent).observe();
		
		if(initPosition==null) {
			((MyTankerAgent) this.myAgent).setInitPosition(myPosition.toString());
		}
		
		if(needToMove) {
			//Parcours des observations
			for (Couple<Location, List<Couple<Observation, String>>> locationCouple : observations) {
			    Location location = locationCouple.getLeft();

			    listToMove.add(location.toString());
			}
			listToMove.remove(myPosition.toString());
			String pos = listToMove.get((int)(Math.random() * listToMove.size()));
			
			((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(pos));
			((MyTankerAgent) this.myAgent).setNeedToMove(false);
			
			exit = 1;
			finished = true;
			return;
			    
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
