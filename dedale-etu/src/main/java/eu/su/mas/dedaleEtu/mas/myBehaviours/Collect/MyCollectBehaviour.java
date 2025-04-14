package eu.su.mas.dedaleEtu.mas.myBehaviours.Collect;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import global.Global;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.myAgents.MyCollectAgent;
import jade.core.behaviours.SimpleBehaviour;


public class MyCollectBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;
	private MapRepresentation myMap;
	private int exit;


	public MyCollectBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	@Override
	public void action() {
		
		String agentName = ((MyCollectAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		Observation resType = ((MyCollectAgent) this.myAgent).getMyTreasureType();
		HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> liste_pos_ressources = ((MyCollectAgent)this.myAgent).getListe_pos_ressources();
		ArrayList<Tuple3<String, Integer, Instant>> listMyType = liste_pos_ressources.get(resType.toString());
		List<Couple<Observation, Integer>> items = ((MyCollectAgent) this.myAgent).getBackPackFreeSpace();
		Location tankLoc = ((MyCollectAgent) this.myAgent).getTankLoc();
		Location myPosition =((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			((MyCollectAgent) this.myAgent).setMyMap(this.myMap);
		}
		
		
		int cap = 0;
    	for (Couple<Observation, Integer> couple : items) {
            if (couple.getLeft()==resType) {
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
        			move(myMap.getShortestPath(myPosition.toString(), tankLoc.toString()));
        		}else {
        			System.out.println(color+ agentName+" : Ma capacité est réduite : "+cap+" mais je sais pas ou est le tank donc j'explore aléatoirement.");
        		}
    		}
    	}else {
    		if(cap<=5) {
        		if(tankLoc !=null) {
        			System.out.println(color+ agentName+" : Ma capacité est réduite : "+cap+" et je sais ou est le tank je vais aller le voir.");
        			move(myMap.getShortestPath(myPosition.toString(), tankLoc.toString()));
        		}else {
        			System.out.println(color+ agentName+" : Ma capacité est réduite : "+cap+" mais je sais pas ou est le tank donc j'explore aléatoirement.");
        		}
        	}
    	}

		
		/*
		 * Marcher de façon random
		 */
		
    	//0) Retrieve the current position
		myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	
		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<Location,List<Couple<Observation,String>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			try {
				this.myAgent.doWait(Global.temps);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition.getLocationId(),MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNodeId=null;
			Iterator<Couple<Location, List<Couple<Observation, String>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				Location accessibleNode=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(accessibleNode.getLocationId());
				
				
				//the node may exist, but not necessarily the edge
				if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
					this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
					
					
					if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
				}
			}

			//3) while openNodes is not empty, continues.
			if (!this.myMap.hasOpenNode()){
				//Explo finished
				System.out.println(color + agentName+" - Exploration successufully done, behaviour removed.");
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
					nextNodeId=this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					//System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"| nextNode: "+nextNode);
				}else {
					//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				
			}
			((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(nextNodeId));
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
	
	public void move(List<String> liste) {
	    while (!liste.isEmpty()) {
	        String nextNode = liste.get(0);
	        try {
	            boolean success = ((AbstractDedaleAgent) this.myAgent).moveTo(new GsLocation(nextNode));
	            if (!success) {
	                System.out.println("❌ Impossible de bouger vers " + nextNode);
	                // tu peux décider ici de soit break, soit skip le node
	                break; 
	            }
	            liste.remove(0); // move réussi ➔ on passe au prochain
	        } catch (Exception e) {
	            System.out.println("⚠️ ERREUR dans move vers " + nextNode + " : " + e.getMessage());
	            break; // on stoppe la boucle si y'a une exception
	        }

	        try {
	            this.myAgent.doWait(Global.temps);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

}
