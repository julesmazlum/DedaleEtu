package eu.su.mas.dedaleEtu.mas.myBehaviours.Collect;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import global.Global;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.myAgents.MyCollectAgent;
import jade.core.behaviours.SimpleBehaviour;


public class MyCollectBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyCollectBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	@Override
	public void action() {
		
		/* Affichage */
		String agentName = ((MyCollectAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		MapRepresentation myMap = ((MyCollectAgent)this.myAgent).getMyMap();
		MapRepresentation myMap2 = ((MyCollectAgent)this.myAgent).getMyMap2();
		Location myPosition =((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> liste_pos_ressources = ((MyCollectAgent)this.myAgent).getListe_pos_ressources();
		Observation resType = ((MyCollectAgent) this.myAgent).getMyTreasureType();
		ArrayList<Tuple3<String, Integer, Instant>> listMyType = liste_pos_ressources.get(resType.toString());
		
		/* Gestion des agents */
		List<Couple<Observation, Integer>> items = ((MyCollectAgent) this.myAgent).getBackPackFreeSpace();
		Location tankLoc = ((MyCollectAgent) this.myAgent).getTanker().getSecond();
		int capMax = ((MyCollectAgent) this.myAgent).getCapMax();
		
		
		if(myMap==null) {
			myMap= new MapRepresentation(agentName);
			((MyCollectAgent) this.myAgent).setMyMap(myMap);
		}
		
		
		int cap = 0;
    	for (Couple<Observation, Integer> couple : items) {
            if (couple.getLeft()==resType) {
            	if(capMax==-1) {
            		((MyCollectAgent) this.myAgent).setCapMax(cap);
            	}
                cap = couple.getRight();
                break;
            }
        }
    	
    	int qte = 0;
    	if(listMyType!=null) {
    		for(Tuple3<String, Integer, Instant> tuple : listMyType) {
        		if(tuple.getSecond()>0) {
        			qte++;
        		}
        	}
    	}
    	
    	
    	
    	if(qte>0) {
    		if(cap>0) {
    			System.out.println(color+ agentName+" : J'ai reçu la position de ressources "+qte+" et j'ai une capacité de "+cap+" donc je vais les chercher");
    			exit = 2;
    			finished = true;
    			return;
    		}else {
    			System.out.println(color+ agentName+" : J'ai reçu la position de ressources "+qte+" et j'ai une capacité de "+cap);
    			if(tankLoc !=null) {
        			System.out.println(color+ agentName+" : Ma capacité est réduite : "+cap+" et je sais ou est le tank je vais aller le voir.");
        			List<String> chemin = myMap.getShortestPath(myPosition.toString(), tankLoc.toString());
        			System.out.println("ma pos "+myPosition.toString()+" loc tank "+tankLoc.toString()+ " chemin "+chemin);
        			if(chemin.size()!=0) {
        				chemin.remove(chemin.size()-1);
        			}
        			Global.move(chemin, (AbstractDedaleAgent) myAgent, color, agentName);
        			exit = 1;
        			finished = true;
        			return;
        		}else {
        			System.out.println(color+ agentName+" : Ma capacité est réduite : "+cap+" mais je sais pas ou est le tank donc j'explore aléatoirement.");
        		}
    		}
    	}else {
    		if(cap<=capMax) {
        		if(tankLoc !=null) {
        			System.out.println(color+ agentName+" : Ma capacité est réduite : "+cap+" et je sais ou est le tank je vais aller le voir.");
        			List<String> chemin = myMap.getShortestPath(myPosition.toString(), tankLoc.toString());
        			System.out.println("ma pos "+myPosition.toString()+" loc tank "+tankLoc.toString()+ " chemin "+chemin);
        			if(chemin.size()!=0) {
        				chemin.remove(chemin.size()-1);
        			}
        			Global.move(chemin, (AbstractDedaleAgent) myAgent, color, agentName);
        			exit = 1;
        			finished = true;
        			return;
        		}else {
        			System.out.println(color+ agentName+" : Ma capacité est réduite : "+cap+" mais je sais pas ou est le tank donc j'explore aléatoirement.");
        		}
        	}else {
        		System.out.println(color+ agentName+" : J'ai de la capacité mais je connais pas l'endroit des trésor donc je me balade");
        	}
    	}

		
		/*
		 * Marcher de façon random
		 */
    	
    	if(((MyCollectAgent) this.myAgent).getIsMapExplored()) {
			System.out.println(color + agentName+" : On recommence une exploration virtuelle");
			myMap2 = new MapRepresentation(false);
			((MyCollectAgent) this.myAgent).setMyMap2(myMap2);
			((MyCollectAgent) this.myAgent).setIsMapExplored(false);
			myMap = myMap2;
		}
		if(myMap2!=null) {
			myMap = myMap2;
		}
		
    	//0) Retrieve the current position
		myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();

			try {
				this.myAgent.doWait(Global.temps);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			myMap.addNode(myPosition.getLocationId(),MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNodeId=null;
			Iterator<Couple<Location, List<Couple<Observation, String>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				Location accessibleNode=iter.next().getLeft();
				boolean isNewNode=myMap.addNewNode(accessibleNode.getLocationId());
				
				
				//the node may exist, but not necessarily the edge
				if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
					myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());				
					if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
				}
			}

			//3) while openNodes is not empty, continues.
			if (!myMap.hasOpenNode()){
				//Explo finished
				System.out.println(color + agentName+" : Toute la carte a été explorée.");
				((MyCollectAgent) this.myAgent).setIsMapExplored(true);
				exit = 1;
				finished = true;
				return;
			}else{
				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNodeId==null){
					//no directly accessible openNode
					//chose one, compute the path and take the first step.
					nextNodeId=myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					//System.out.println(this.myAgent.getLocalName()+"-- list= "+myMap.getOpenNodes()+"| nextNode: "+nextNode);
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				
			}
			Global.moveNextNode(nextNodeId, (AbstractDedaleAgent) myAgent, color, agentName);
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
