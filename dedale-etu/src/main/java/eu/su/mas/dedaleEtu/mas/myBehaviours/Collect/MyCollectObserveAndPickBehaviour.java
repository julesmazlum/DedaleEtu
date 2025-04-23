package eu.su.mas.dedaleEtu.mas.myBehaviours.Collect;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.myAgents.MyCollectAgent;
import global.Global;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;


public class MyCollectObserveAndPickBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyCollectObserveAndPickBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	@Override
	public void action() {
		
		/* Affichage */
		String agentName = ((MyCollectAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		Observation resType = ((MyCollectAgent) this.myAgent).getMyTreasureType();
		List<Couple<Observation, Integer>> items = ((MyCollectAgent) this.myAgent).getBackPackFreeSpace();
		List<Couple<Location, List<Couple<Observation, String>>>> observations = ((AbstractDedaleAgent) this.myAgent).observe();
		HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> liste_pos_ressources = ((MyCollectAgent)this.myAgent).getListe_pos_ressources();
		ArrayList<Tuple3<String, Integer, Instant>> listMyType = liste_pos_ressources.get(resType.toString());
		MapRepresentation myMap = ((MyCollectAgent) this.myAgent).getMyMap();
		Location myPosition =((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		String goToTres = ((MyCollectAgent) this.myAgent).getGoToTres();
		
		/* Gestion des agents */
		Location tankLoc = ((MyCollectAgent) this.myAgent).getTankLoc();
		Map<String, String> agent_types = ((MyCollectAgent) this.myAgent).getAgent_types();
		String tanker = ((MyCollectAgent) this.myAgent).getTanker();
		
		
		//Parcours des observations
		for (Couple<Location, List<Couple<Observation, String>>> locationCouple : observations) {
			Location location = locationCouple.getLeft();
		    List<Couple<Observation, String>> observationDetails = locationCouple.getRight();

		    // afficher la location observée
		    //System.out.println("Location observée : " + location);
		    
		    if(tankLoc!=null && location.toString().equals(tankLoc.toString())) {
		    	boolean foundTank = false;
	    		for (Couple<Observation, String> detail : observationDetails) {
			        String valeur = detail.getRight();
			        if(valeur.equals(tanker)){
			        	foundTank=true;
			        	break;
			        }
	    		}
	    		
	    		if(!foundTank) {
	    			System.out.println(color + agentName+ " : Le tank n'est plus la...");
		    		((MyCollectAgent) this.myAgent).setTankLoc(null);
	    		}
		    }
		    
		    if(goToTres!=null && location.toString().equals(goToTres)) {
		    	boolean foundTres = false;
		    	for (Couple<Observation, String> detail : observationDetails) {
			        Observation obs = detail.getLeft();
			        if(obs==resType) {
			        	foundTres = true;
			        	break;
			        }
		    	}
		    	
		    	if(!foundTres) {
		    		System.out.println(color + agentName+ " : Il n'y a plus de trésors ici finalement...");
		    		Instant time2 = Instant.now();
		    		
		    		for (int i = 0; i < listMyType.size(); i++) {
		        	    Tuple3<String, Integer, Instant> tuple = listMyType.get(i);
		        	    
		        	    if (tuple.getFirst().equals(location.toString())) {
		        	        Tuple3<String, Integer, Instant> updatedTuple = new Tuple3<>(location.toString(), 0, time2);
		        	        System.out.println(color + agentName+" : Je met à jour la quantité et le temps "+updatedTuple);
		        	        listMyType.set(i, updatedTuple);
		        	        System.out.println(color + agentName+" : Nouvelle liste  "+listMyType);
		        	        break;
		        	    }
		        	}
		    	}
		    	((MyCollectAgent) this.myAgent).setGoToTres(null);
		    }

		    // parcourir les détails des observations
		    for (Couple<Observation, String> detail : observationDetails) {
		        Observation obs = detail.getLeft();
		        String valeur = detail.getRight();

		        // afficher les détails
		        //System.out.println("  Observation : " + obs.getName() + ", Valeur : " + valeur);
		        
		        if(obs.getName().equals("AgentName")) {
		        	//Si on ne connait pas son type, alors on lui demande son type par message
		        	if(!agent_types.containsKey(valeur)) {
		        		System.out.println(color + agentName+ " : J'ai rencontré "+valeur+" mais je ne sais pas son type je vais lui demander.");
		        		
		        		//Demande de type
		        		ACLMessage msgASKTYPE = new ACLMessage(ACLMessage.INFORM);
		        		msgASKTYPE.setProtocol("ASK-TYPE");
		        		msgASKTYPE.setSender(this.myAgent.getAID());
		        		String receiver = valeur;
		        		msgASKTYPE.addReceiver(new AID(receiver,AID.ISLOCALNAME));
		        		
		        		String msg = ((MyCollectAgent) this.myAgent).getLocalName();
		        		
		        		try {					
		        			msgASKTYPE.setContentObject(msg);
		        		} catch (IOException e) {
		        			e.printStackTrace();
		        		}
		        		((AbstractDedaleAgent)this.myAgent).sendMessage(msgASKTYPE);
		        		System.out.println(color + agentName+ " : J'ai demandé son type");
		        	}
		        	
		        	//Je lui envoie la carte
		        	if (agent_types.containsKey(valeur) && !"agentTanker".equals(agent_types.get(valeur))) {
			        	System.out.println(color + agentName+ " : J'ai rencontré "+valeur+" je vais lui envoyer les données que j'ai (carte, position ressources et tank)");
			        	((MyCollectAgent) this.myAgent).setAgentNameToSendTo(valeur);
			        	exit = 2;
						finished = true;
						return;
		        	} 
		        }
		        
		        //si j'ai le nom du tanker et je rencontre tank
		        if(tanker!=null && valeur!= null && valeur.equals(tanker)) {
		        	((MyCollectAgent) this.myAgent).setTankLoc(location);
		        	myMap.addNewNode(location.getLocationId());
		        	myMap.addEdge(myPosition.getLocationId(), location.getLocationId());
		        	System.out.println(color + agentName+" : J'ai rencontré "+valeur+" est le tanker. j'ai maj sa position.");
		        	System.out.println(color + agentName+ " : Je vais me vider.");
		        	
		        	if(((MyCollectAgent) this.myAgent).emptyMyBackPack(valeur)==true) {
		    			System.out.println(color + agentName +" : J'ai réussi à vider mon sac dans"+valeur);
		    		}else {
		    			System.out.println(color+ agentName +" : J'ai pas réussi à vider mon sac");
		    		}
		    		
		        	items = ((MyCollectAgent) this.myAgent).getBackPackFreeSpace();
		        	for (Couple<Observation, Integer> couple : items) {
		                System.out.println(color+ agentName +" : Il me reste "+ couple.getRight()+" de capacité pour "+couple.getLeft());
		            }
		        }
		        
		        //Rencontrer trésor
		        if(obs == resType) {
		        	System.out.println(color + agentName+ " : J'ai trouvé "+ valeur + " " + obs.getName());
		        	System.out.println(color + agentName+ " : Je vais tenter de déverouiller le coffre");
		        	
		        	if(((AbstractDedaleAgent) this.myAgent).openLock(obs)) {
		        		System.out.println(color + agentName+" : J'ai réussi à déverouiller");
		        		
		        		int cap = 0;
		            	for (Couple<Observation, Integer> couple : items) {
		                    if (couple.getLeft()==resType) {
		                        cap = couple.getRight();
		                        break;
		                    }
		                }

		            	int picked = 0;
		            	Instant time = Instant.now();
		            	
		            	if(cap>0) {
		            		System.out.println(color + agentName+" : J'ai "+cap+" de capacité et j'ai trouvé "+valeur+" "+ obs+ " je peux donc ramasser");
		            		picked = ((MyCollectAgent) this.myAgent).pick();
		            		System.out.println(color + agentName+" : J'ai ramassé "+picked+" obj");
		            	}else {
		            		System.out.println(color + agentName+" : J'ai "+cap+" capacité et j'ai trouvé "+valeur+ "obj je peux donc PAS ramasser. Je retourne explorer");
		            	}
		            		
	            		if(listMyType==null) {
	            			listMyType = new ArrayList<>();
	            		}
	            		
	            		boolean found = false;
	            		
	            		for (int i = 0; i < listMyType.size(); i++) {
			        	    Tuple3<String, Integer, Instant> tuple = listMyType.get(i);
			        	    
			        	    if (tuple.getFirst().equals(location.toString())) {
			        	        Tuple3<String, Integer, Instant> updatedTuple = new Tuple3<>(location.toString(), Integer.parseInt(valeur)-picked, time);
			        	        System.out.println(color + agentName+" : Je met à jour la quantité et le temps "+updatedTuple);
			        	        listMyType.set(i, updatedTuple);
			        	        System.out.println(color + agentName+" : Nouvelle liste  "+listMyType);
			        	        found = true;
			        	        break;
			        	    }
			        	}
	            		
	            		if(!found) {
	            			Tuple3<String, Integer, Instant> updatedTuple = new Tuple3<>(location.toString(), Integer.parseInt(valeur)-picked, time);
	            			listMyType.add(updatedTuple);
	            			System.out.println(color + agentName+" : J'ajoute la quantité et le temps "+updatedTuple);
	            			System.out.println(color + agentName+" : Nouvelle liste  "+listMyType);
	            		}
	            		
	            		liste_pos_ressources.put(resType.toString(), listMyType);
	            		
	            		items = ((MyCollectAgent) this.myAgent).getBackPackFreeSpace();
	                	for (Couple<Observation, Integer> couple : items) {
	                        System.out.println(color + agentName +" : Il me reste "+ couple.getRight()+" de capacité pour "+couple.getLeft());
	                    }

		            	
		            	
		        	}else {
		        		System.out.println(color + agentName+" : J'ai PAS réussi à déverouiller");
		        	}
		        }
		    }
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
