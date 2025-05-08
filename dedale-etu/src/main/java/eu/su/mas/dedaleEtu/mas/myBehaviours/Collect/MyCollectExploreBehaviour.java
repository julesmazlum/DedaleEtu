package eu.su.mas.dedaleEtu.mas.myBehaviours.Collect;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import global.Global;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.myAgents.MyCollectAgent;
import jade.core.behaviours.SimpleBehaviour;


public class MyCollectExploreBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyCollectExploreBehaviour(final AbstractDedaleAgent myagent) {
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
		MapRepresentation myMap = ((MyCollectAgent)this.myAgent).getMyMap();
		MapRepresentation myMap2 = ((MyCollectAgent)this.myAgent).getMyMap2();
		Location myPosition =((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>>> listTreasureData = ((MyCollectAgent)this.myAgent).getListTreasureData();
		Observation resType = ((MyCollectAgent) this.myAgent).getMyTreasureType();
		ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>> listMyType = listTreasureData.get(resType.toString());
		
		/* Gestion des agents */
		List<Couple<Observation, Integer>> items = ((MyCollectAgent) this.myAgent).getBackPackFreeSpace();
		Location tankLoc = ((MyCollectAgent) this.myAgent).getTanker().getSecond();
		int capMax = ((MyCollectAgent) this.myAgent).getCapMax();
		int myLockPicking = ((MyCollectAgent) this.myAgent).getMyLockPicking();
		int myStretgh = ((MyCollectAgent) this.myAgent).getMyStrentgh();
		
		/* Initialisation de la carte */
		if(myMap==null) {
			myMap= new MapRepresentation(agentName);
			((MyCollectAgent) this.myAgent).setMyMap(myMap);
		}
		
		/* Si myLockPicking et myStretgh n'ont pas encore été récupérés, on les récupère une fois */
		if(myLockPicking == -1 || myStretgh == -1) {
			Set<Couple<Observation, Integer>> set = ((MyCollectAgent) this.myAgent).getMyExpertise();
			for (Couple<Observation, Integer> couple : set) {
			    Observation obs = couple.getLeft();
			    Integer valeur = couple.getRight();
			    if(obs.toString().equals("lockPickingExpertise")) {
			    	((MyCollectAgent) this.myAgent).setMyLockPicking(valeur);
			    }
			    if(obs.toString().equals("strengthExpertise")) {
			    	((MyCollectAgent) this.myAgent).setMyStrentgh(valeur);
			    }
			}
		}
		
		/* Récupérer la capacité */
		int cap = 0;
    	for (Couple<Observation, Integer> couple : items) {
            if (couple.getLeft()==resType) {
            	//Récupérer aussi la capacité maximale
            	if(capMax==-1) {
            		((MyCollectAgent) this.myAgent).setCapMax(cap);
            	}
                cap = couple.getRight();
                break;
            }
        }
    	
    	// quantité de ressources disponibles dans la liste qu'un agent peut récolter seul
    	int qte = 0;
    	if(listMyType!=null) {
    		//parcours de la liste
    		for(Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple : listMyType) {
    			//si la quantité de ressource > 0 ET ((myLockPicking et myStretgh sont assez) OU (le coffre est déjà ouvert))
        		if(tuple.get_2()>0 && ((tuple.get_3().getFirst()<=myLockPicking && tuple.get_3().getSecond()<=myStretgh) || tuple.get_3().getThird()==1)) {
        			qte++;
        		}
        	}
    	}
    	
    	
    	// Si il y a des ressources à aller récuper dans la liste
    	if(qte>0) {
    		
    		// si capacité disponible
    		if(cap>0) {
    			System.out.println(color+ agentName+" : J'ai la position de " + qte+ " ressources et j'ai une capacité de "+cap+" donc je vais les chercher.");
    			exit = 2;
    			finished = true;
    			return;
    			
    		// si pas de capacité
    		}else {
    			System.out.println(color+ agentName+" : J'ai la position de " + qte+ " ressources et j'ai une capacité de "+cap);
    			
    			//si position du tank connue
    			if(tankLoc !=null) {
        			System.out.println(color+ agentName+" : Ma capacité est réduite : "+cap+" et je sais ou est le tanker je vais aller le voir.");
        			//calcul du chemin
        			List<String> chemin = myMap.getShortestPath(myPosition.toString(), tankLoc.toString());
        			if(chemin.size()!=0) {
        				chemin.remove(chemin.size()-1);
        			}
        			Global.move(chemin, (AbstractDedaleAgent) myAgent, color, agentName);
        			exit = 1;
        			finished = true;
        			return;
        		
        		//si position du tank pas connu
        		}else {
        			System.out.println(color+ agentName+" : Ma capacité est réduite : "+cap+" mais je sais pas ou est le tanker donc j'explore.");
        		}
    		}
    		
    	// Si aucune ressources récupérable n'est présente dans la liste
    	}else {
    		
    		//si pas capacité maximal
    		if(cap<=capMax) {
    			
    			//si position du tanker connue
        		if(tankLoc !=null) {
        			System.out.println(color+ agentName+" : Ma capacité n'est pas maximale : "+cap+" et je sais ou est le tanker je vais aller le voir.");
        			//calcul du chemin
        			List<String> chemin = myMap.getShortestPath(myPosition.toString(), tankLoc.toString());
        			System.out.println("ma pos "+myPosition.toString()+" loc tank "+tankLoc.toString()+ " chemin "+chemin);
        			if(chemin.size()!=0) {
        				chemin.remove(chemin.size()-1);
        			}
        			Global.move(chemin, (AbstractDedaleAgent) myAgent, color, agentName);
        			exit = 1;
        			finished = true;
        			return;
        		
        		//si position du tanker pas connu
        		}else {
        			System.out.println(color+ agentName+" : Ma capacité n'est pas maximale : "+cap+" mais je sais pas ou est le tanker donc j'explore.");
        		}
        	
        	//si capacité maximale
        	}else {
        		System.out.println(color+ agentName+" : Ma capacité est maximale et je connais pas l'endroit des trésors donc j'explore.");
        	}
    	}

		
		/*
		 * Exploration
		 */
    	
    	/* Exploration virtuelle si exploration initiale finie */
    	if(((MyCollectAgent) this.myAgent).getIsMapExplored()) {
			System.out.println(color + agentName+" : Je recommence une exploration virtuelle.");
			myMap2 = new MapRepresentation(false);
			((MyCollectAgent) this.myAgent).setMyMap2(myMap2);
			((MyCollectAgent) this.myAgent).setIsMapExplored(false);
			myMap = myMap2;
		}
		if(myMap2!=null) {
			myMap = myMap2;
		}
		
		
		/* Stratégie d'exploration */
		
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
				//Exploration finie
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
