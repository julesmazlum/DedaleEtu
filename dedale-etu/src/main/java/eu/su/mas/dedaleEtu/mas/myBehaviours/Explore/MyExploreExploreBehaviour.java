package eu.su.mas.dedaleEtu.mas.myBehaviours.Explore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.myAgents.MyExploreAgent;
import global.Global;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;


public class MyExploreExploreBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyExploreExploreBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	public void action() {
		
		/* Affichage */
		String agentName = ((MyExploreAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		MapRepresentation myMap = ((MyExploreAgent) this.myAgent).getMyMap();
		MapRepresentation myMap2 = ((MyExploreAgent) this.myAgent).getMyMap2();
		boolean IsInitialMapExplored = ((MyExploreAgent) this.myAgent).getIsInitialMapExplored();
		
		/* Gestion agents/ carte */
		Map<String, MapRepresentation> listAgentMap = ((MyExploreAgent) this.myAgent).getListAgentMap();
		int nbExplored = ((MyExploreAgent) this.myAgent).getNbExplored();
		HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>>> listTreasureData = ((MyExploreAgent)this.myAgent).getListTreasureData();
		ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>> listGoldType = listTreasureData.get("Gold");

		/* Initialisation de la carte */
		if(myMap==null) {
			myMap= new MapRepresentation(agentName);
			((MyExploreAgent) this.myAgent).setMyMap(myMap);
		}
		
		
		
		/*
		 * Exploration de la carte 
		 */
		
		
		/* Exploration virtuelle si exploration initialle finie */
		if(((MyExploreAgent) this.myAgent).getIsMapExplored()) {
			System.out.println(color + agentName+" : Je recommence une exploration virtuelle.");
			
			// Initialisation de la carte virtuelle
			myMap2 = new MapRepresentation(false);
			((MyExploreAgent) this.myAgent).setMyMap2(myMap2);
			((MyExploreAgent) this.myAgent).setIsMapExplored(false);
			myMap = myMap2;
		}
		
		if(myMap2!=null) {
			myMap = myMap2;
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
    	
    	if(qteToUnlock > 0 && nbExplored > 1) {
			System.out.println(color+ agentName+" : (Explore) J'ai exploré la carte plus de 3 fois, je vais essayer d'aider à ouvrir les coffres des Godl");
			exit = 2;
			finished = true;
			return;
		}
		
		
		/*
		 * Stratégie d'exploration initiale
		 */
		
		//0) Retrieve the current position
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	
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
				
				//Si c'est un nouveau noeud et que c'est l'exploration initiale
				if(isNewNode && !IsInitialMapExplored) {
					// On ajoute également dans le dictionnaire pour chaque agent
					for (Map.Entry<String, MapRepresentation> entry : listAgentMap.entrySet()) {
					    MapRepresentation value = entry.getValue();
					    value.addNewNode(accessibleNode.getLocationId());
					}
				}
				
				//the node may exist, but not necessarily the edge
				if (myPosition.getLocationId()!=accessibleNode.getLocationId()) {
					myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
					
					//Si c'est un nouveau noeud et que c'est l'exploration initiale
					if(isNewNode && !IsInitialMapExplored) {
						// On ajoute également dans le dictionnaire pour chaque agent
						for (Map.Entry<String, MapRepresentation> entry : listAgentMap.entrySet()) {
						    MapRepresentation value = entry.getValue();
						    value.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
						}
					}
					
					if (nextNodeId==null && isNewNode) nextNodeId=accessibleNode.getLocationId();
				}
			}

			//3) while openNodes is not empty, continues.
			if (!myMap.hasOpenNode()){
				//Exploration terminée
				System.out.println(color + agentName+" : Toute la carte a été explorée.");
				((MyExploreAgent) this.myAgent).setNbExplored(((MyExploreAgent) this.myAgent).getNbExplored()+1);
				((MyExploreAgent) this.myAgent).setIsMapExplored(true);
				((MyExploreAgent) this.myAgent).setIsInitialMapExplored(true);
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
			//Avancer au next node
			System.out.println(color + agentName+" : J'explore");
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
