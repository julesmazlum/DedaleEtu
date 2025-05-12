package eu.su.mas.dedaleEtu.mas.myBehaviours.Collect;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import dataStructures.tuple.Tuple4;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.myAgents.MyCollectAgent;
import global.Global;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;


public class MyCollectObservePickEmptyBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyCollectObservePickEmptyBehaviour(final AbstractDedaleAgent myagent) {
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
		Observation resType = ((MyCollectAgent) this.myAgent).getMyTreasureType();
		List<Couple<Observation, Integer>> items = ((MyCollectAgent) this.myAgent).getBackPackFreeSpace();
		List<Couple<Location, List<Couple<Observation, String>>>> observations = ((AbstractDedaleAgent) this.myAgent).observe();
		HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>>> listTreasureData = ((MyCollectAgent)this.myAgent).getListTreasureData();
		ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>> listMyType = listTreasureData.get(resType.toString());
		MapRepresentation myMap = ((MyCollectAgent) this.myAgent).getMyMap();
		Location myPosition =((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		String goToTres = ((MyCollectAgent) this.myAgent).getGoToTres();
		
		/* Gestion des agents */
		Map<String, String> agentTypes = ((MyCollectAgent) this.myAgent).getAgentTypes();
		Tuple3<String, Location, Instant> tanker = ((MyCollectAgent) this.myAgent).getTanker();
		
		//Parcours des observations
		for (Couple<Location, List<Couple<Observation, String>>> locationCouple : observations) {
			Location location = locationCouple.getLeft();
		    List<Couple<Observation, String>> observationDetails = locationCouple.getRight();

		    // afficher la location observée
		    //System.out.println("Location observée : " + location);
		    
		    
		    //Si le tanker devait être présent à cet emplacement
		    if(tanker.getSecond()!=null && location.toString().equals(tanker.getSecond().toString())) {
		    	// chercher le tanker
		    	boolean foundTank = false;
	    		for (Couple<Observation, String> detail : observationDetails) {
			        String valeur = detail.getRight();
			        if(valeur!= null && valeur.equals(tanker.getFirst())){
			        	foundTank=true;
			        	break;
			        }
	    		}
	    		
	    		// Si le tanker n'est plus present
	    		if(!foundTank) {
	    			System.out.println(color + agentName+ " : Le tanker n'est plus la...");
	    			Tuple3<String, Location, Instant> newTanker = new Tuple3<>(tanker.getFirst(), null, Instant.now());
		    		((MyCollectAgent) this.myAgent).setTanker(newTanker);
	    		}
		    }
		    
		    
		    //Si je devais me déplacer vers un trésor, et qu'il devait être la
		    if(goToTres!=null && location.toString().equals(goToTres)) {
		    	// chercher le tresor
		    	boolean foundTres = false;
		    	for (Couple<Observation, String> detail : observationDetails) {
			        Observation obs = detail.getLeft();
			        if(obs==resType) {
			        	foundTres = true;
			        	break;
			        }
		    	}
		    	
		    	// si le tresor n'est plus present
		    	if(!foundTres) {
		    		System.out.println(color + agentName+ " : Il n'y a plus de trésors ici finalement...");
		    		
		    		for (int i = 0; i < listMyType.size(); i++) {
		    			Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple = listMyType.get(i);
		        	    
		        	    if (tuple.get_1().equals(location.toString())) {
		        	    	Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> updatedTuple = new Tuple4<>(location.toString(), 0, tuple.get_3() ,Instant.now());
		        	        System.out.println(color + agentName+" : Je met à jour la quantité : "+updatedTuple);
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
		        
		        
		        // si observer un agent
		        if(obs.getName().equals("AgentName")) {
		        	
		        	//Si on ne connait pas son type, alors on lui demande son type par message
		        	if(!agentTypes.containsKey(valeur)) {
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
		        		System.out.println(color + agentName+ " : J'ai demandé son type.");
		        	}
		        	
		        	//Si je connais son type et ce n'est pas le tanker, j'envoie les données
		        	if (agentTypes.containsKey(valeur) && !"agentTanker".equals(agentTypes.get(valeur))) {
			        	System.out.println(color + agentName+ " : J'ai rencontré "+valeur+" je vais lui envoyer les données que j'ai (carte, position ressources et tank)");
			        	((MyCollectAgent) this.myAgent).setAgentNameToSendTo(valeur);
			        	exit = 2;
						finished = true;
						return;
		        	} 
		        }
		        
		        
		        //si j'ai le nom du tanker et je rencontre tank
		        if(tanker!=null && valeur!= null && valeur.equals(tanker.getFirst())) {
		        	//mise à jour de la position du tanker
		        	Tuple3<String, Location, Instant> newtanker = new Tuple3<>(tanker.getFirst(), location, Instant.now());
	        		((MyCollectAgent) this.myAgent).setTanker(newtanker);
	        		
	        		//j'ajoute son noeud dans la carte
		        	myMap.addNewNode(location.getLocationId());
		        	myMap.addEdge(myPosition.getLocationId(), location.getLocationId());
		        	
		        	System.out.println(color + agentName+" : J'ai rencontré "+valeur+"qui est le tanker. J'ai mis à jour sa position.");
		        	System.out.println(color + agentName+ " : Je vais vider mes ressources.");
		        	
		        	// J'essaye de vider mon sac
		        	if(((MyCollectAgent) this.myAgent).emptyMyBackPack(valeur)==true) {
		    			System.out.println(color + agentName +" : J'ai réussi à vider mon sac dans "+valeur);
		    		}else {
		    			System.out.println(color+ agentName +" : J'ai pas réussi à vider mon sac.");
		    		}
		    		
		        	//calcul de nouvelles capacités
		        	items = ((MyCollectAgent) this.myAgent).getBackPackFreeSpace();
		        	for (Couple<Observation, Integer> couple : items) {
		                System.out.println(color+ agentName +" : Il me reste "+ couple.getRight()+" de capacité pour "+couple.getLeft());
		            }
		        }
		        
		        
		        //Rencontrer trésor
		        if(obs == resType) {
		        	System.out.println(color + agentName+ " : J'ai trouvé "+ valeur + " " + obs.getName());
		        	System.out.println(color + agentName+ " : Je vais tenter de déverouiller le coffre.");
		        	
		        	// essayer openlock
		        	if(((AbstractDedaleAgent) this.myAgent).openLock(obs)) {
		        		System.out.println(color + agentName+" : J'ai réussi à déverouiller.");
		        		
		        		// calculer la capacité
		        		int cap = 0;
		            	for (Couple<Observation, Integer> couple : items) {
		                    if (couple.getLeft()==resType) {
		                        cap = couple.getRight();
		                        break;
		                    }
		                }

		            	//quantité ramassée
		            	int picked = 0;
		            	
		            	// si j'ai de la capacité
		            	if(cap>0) {
		            		System.out.println(color + agentName+" : J'ai "+cap+" de capacité et j'ai trouvé "+valeur+" "+ obs+ " je peux donc ramasser.");
		            		picked = ((MyCollectAgent) this.myAgent).pick();
		            		System.out.println(color + agentName+" : J'ai ramassé "+picked+" "+obs);
		            		
		            	//si j'ai pas de capacité
		            	}else {
		            		System.out.println(color + agentName+" : J'ai "+cap+" capacité et j'ai trouvé "+valeur+ " "+obs+" je peux donc pas ramasser. Je retourne explorer");
		            	}
		            	
		            	/* Mise à jour de la liste de ressource */
	            		if(listMyType==null) {
	            			listMyType = new ArrayList<>();
	            		}
	            		
	            		// Recherche de la ressource
	            		boolean found = false;
	            		for (int i = 0; i < listMyType.size(); i++) {
	            			Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple = listMyType.get(i);
			        	    
			        	    if (tuple.get_1().equals(location.toString())) {
			        	    	Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> updatedTuple = new Tuple4<>(location.toString(), Integer.parseInt(valeur)-picked, new Tuple3<>(tuple.get_3().getFirst(),tuple.get_3().getSecond(), 1), Instant.now());
			        	        System.out.println(color + agentName+" : Je met à jour la quantité et l'état : "+updatedTuple);
			        	        listMyType.set(i, updatedTuple);
			        	        System.out.println(color + agentName+" : Nouvelle liste  "+listMyType);
			        	        found = true;
			        	        break;
			        	    }
			        	}
	            		
	            		//Si pas trouvé, l'ajouter
	            		if(!found) {
	            			Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> updatedTuple = new Tuple4<>(location.toString(), Integer.parseInt(valeur)-picked, new Tuple3<>(0, 0, 1),Instant.now());
	            			listMyType.add(updatedTuple);
	            			System.out.println(color + agentName+" : J'ajoute le trésor : "+updatedTuple);
	            			System.out.println(color + agentName+" : Nouvelle liste  "+listMyType);
	            		}
	            		
	            		listTreasureData.put(resType.toString(), listMyType);
	            		
	            		//calcul nouvelles capacité
	            		items = ((MyCollectAgent) this.myAgent).getBackPackFreeSpace();
	                	for (Couple<Observation, Integer> couple : items) {
	                        System.out.println(color + agentName +" : Il me reste "+ couple.getRight()+" de capacité pour "+couple.getLeft());
	                    }

		            	
		            //coffre verouillé
		        	}else {
		        		System.out.println(color + agentName+" : J'ai pas réussi à déverouiller.");
		        		
		        		//mettre à jour la liste de ressource
		        		if(listMyType==null) {
	            			listMyType = new ArrayList<>();
	            		}
	            		
		        		//Recherche du tresor
	            		boolean found = false;
	            		for (int i = 0; i < listMyType.size(); i++) {
	            			Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple = listMyType.get(i);
			        	    
			        	    if (tuple.get_1().equals(location.toString())) {
			        	    	Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> updatedTuple = new Tuple4<>(location.toString(), Integer.parseInt(valeur), new Tuple3<>(tuple.get_3().getFirst(),tuple.get_3().getSecond(), 0), Instant.now());
			        	        System.out.println(color + agentName+" : Je met à jour la quantité et l'état "+updatedTuple);
			        	        listMyType.set(i, updatedTuple);
			        	        System.out.println(color + agentName+" : Nouvelle liste  "+listMyType);
			        	        found = true;
			        	        break;
			        	    }
			        	}
	            		
	            		//si pas trouvé, on l'ajoute
	            		if(!found) {
	            			Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> updatedTuple = new Tuple4<>(location.toString(), Integer.parseInt(valeur), new Tuple3<>(0,0, 0),Instant.now());
	            			listMyType.add(updatedTuple);
	            			System.out.println(color + agentName+" : J'ajoute la ressource : "+updatedTuple);
	            			System.out.println(color + agentName+" : Nouvelle liste  "+listMyType);
	            		}
	            		
	            		listTreasureData.put(resType.toString(), listMyType);
		        	}
		        }
		        
		        //mise à jour de strength
		        if(obs.getName().equals("Strength") && listMyType!=null) {
		        	System.out.println(color + agentName+" : Son strentgh est de "+valeur);

		        	// on parcourt les tuples
		        	for (int i = 0; i < listMyType.size(); i++) {
		        		Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple = listMyType.get(i);
		        	    
		        	    if (tuple.get_1().equals(location.toString())) {
		        	        // si la position existe, on update le strength
		        	        Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> updatedTuple = new Tuple4<>(location.toString(), tuple.get_2(), new Tuple3<>(tuple.get_3().getFirst(), Integer.parseInt(valeur), tuple.get_3().getThird()), Instant.now());
		        	        System.out.println(color + agentName+" : Je met à jour le strength "+updatedTuple);
		        	        listMyType.set(i, updatedTuple);
		        	        break;
		        	    }
		        	}
		        }
		        
		        //mise à jour de lockpicking
		        if(obs.getName().equals("LockPicking") && listMyType!=null) {
		        	System.out.println(color + agentName+" : Son lockpicking est de "+valeur);

		        	// on parcourt les tuples
		        	for (int i = 0; i < listMyType.size(); i++) {
		        		Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple = listMyType.get(i);
		        	    
		        	    if (tuple.get_1().equals(location.toString())) {
		        	        // si la position existe, on update le lockpicking
		        	        Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> updatedTuple = new Tuple4<>(location.toString(), tuple.get_2(), new Tuple3<>(Integer.parseInt(valeur), tuple.get_3().getSecond(), tuple.get_3().getThird()), Instant.now());
		        	        System.out.println(color + agentName+" : Je met à jour lle lockpicking "+updatedTuple);
		        	        listMyType.set(i, updatedTuple);
		        	        break;
		        	    }
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
