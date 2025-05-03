package eu.su.mas.dedaleEtu.mas.myBehaviours.Collect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import dataStructures.tuple.Tuple3;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.myAgents.MyCollectAgent;
import global.Global;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.Serializable;
import java.time.Instant;


public class MyCollectCommunicationBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyCollectCommunicationBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	@SuppressWarnings("unchecked")
	public void action() {
		
		/* Affichage */
		String agentName = ((MyCollectAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		MapRepresentation myMap = ((MyCollectAgent) this.myAgent).getMyMap();
		MapRepresentation myMap2 = ((MyCollectAgent) this.myAgent).getMyMap2();
		HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> liste_pos_ressources = ((MyCollectAgent)this.myAgent).getListe_pos_ressources();
		List<Couple<Location, List<Couple<Observation, String>>>> observations = ((AbstractDedaleAgent) this.myAgent).observe();
		List<Location> listToMove = new ArrayList<>();
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		/* Gestion des agents */
		HashMap<String, String> agent_types = ((MyCollectAgent) this.myAgent).getAgent_types();
		Tuple3<String, Location, Instant> tanker = ((MyCollectAgent) this.myAgent).getTanker();
		
		
		/*
		 * Réception des messages
		 */
		
		
		//Réception du type
		MessageTemplate msgREPLYTYPE2=MessageTemplate.and(
				MessageTemplate.MatchProtocol("REPLY-TYPE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceivedREPLYTYPE=this.myAgent.receive(msgREPLYTYPE2);
		if (msgReceivedREPLYTYPE!=null) {
			ArrayList<String> sltype2=null;
			try {
				sltype2 = (ArrayList<String>) msgReceivedREPLYTYPE.getContentObject();
				System.out.println(color + agentName+ " : J'ai son type : "+sltype2);
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			agent_types.put(sltype2.get(0), sltype2.get(1));
			if(sltype2.get(1).equals("agentTanker")) {
				Tuple3<String, Location, Instant> newTanker = new Tuple3<>(sltype2.get(0), null, null);
				((MyCollectAgent) this.myAgent).setTanker(newTanker);
			}
			System.out.println(color + agentName+ " : Je l'ai ajouté au dico : "+agent_types);			
		}
		
		//Réception demande de type
		MessageTemplate msgASKTYPE=MessageTemplate.and(
				MessageTemplate.MatchProtocol("ASK-TYPE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceivedTYPE=this.myAgent.receive(msgASKTYPE);
		//S'il y a bien qlq demandant le type
		if (msgReceivedTYPE!=null) {
			String asker=null;
			try {
				asker = (String) msgReceivedTYPE.getContentObject();
				System.out.println(color + agentName+ " : "+asker + " me demande mon type");
				
				//Envoie du type à asker
				ACLMessage msgREPLYTYPE = new ACLMessage(ACLMessage.INFORM);
				msgREPLYTYPE.setProtocol("REPLY-TYPE");
				msgREPLYTYPE.setSender(this.myAgent.getAID());
				String receiver = asker;
				msgREPLYTYPE.addReceiver(new AID(receiver,AID.ISLOCALNAME));
				
				ArrayList<String> sltype = new ArrayList<>();
				sltype.add(agentName);
				sltype.add("agentCollect");
				
				try {					
					msgREPLYTYPE.setContentObject(sltype);
				} catch (IOException e) {
					e.printStackTrace();
				}
				((AbstractDedaleAgent)this.myAgent).sendMessage(msgREPLYTYPE);
				System.out.println(color + agentName+ " : Je lui envoie mon type : "+sltype);
				
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		//Réception demande de bouger
		MessageTemplate msgMOVE=MessageTemplate.and(
				MessageTemplate.MatchProtocol("MOVE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceivedMOVE=this.myAgent.receive(msgMOVE);
		Location posSender = null;
		if (msgReceivedMOVE!=null) {
			System.out.println(color + agentName + " : Je dois bouger et je suis pas sur un chemin précis");
			
			//Récupération de la position de l'agent qui demande à ce qu'on se déplace
			try {
				posSender = (Location) msgReceivedMOVE.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//Parcours des observations
			//Liste des positions disponibles pour qu'on se déplace
			for (Couple<Location, List<Couple<Observation, String>>> locationCouple : observations) {
			    Location location = locationCouple.getLeft();
			    if(!location.toString().equals(myPosition.toString()) && !location.toString().equals(posSender.toString())) {
			    	listToMove.add(location);
			    }
			}
			
			//Choix aléatoire parmi les positions disponibles
			Location pos = null;
			
			if(listToMove.size()==0) {
				System.out.println(color+agentName+" : Je suis dans une impasse.");
			}else {
				
				pos = listToMove.get((int)(Math.random() * listToMove.size()));
				myMap.addNewNode(pos.getLocationId());
	        	myMap.addEdge(myPosition.getLocationId(), pos.getLocationId());
				
				//if(!((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(pos.toString()))) {
	        	if(!Global.moveNextNode(pos.toString(), (AbstractDedaleAgent) myAgent, color, agentName)) {
					System.out.println(color+agentName+" : Je n'ai pas réussi à m'écarter du chemin.");
				}else {
					System.out.println(color+agentName+" : J'ai réussi à aller m'écarter du chemin ");
				}
				
			}
		}
		
		//Réception IMPASSE
		MessageTemplate msgIMPASSE=MessageTemplate.and(
				MessageTemplate.MatchProtocol("IMPASSE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceivedIMPASSE=this.myAgent.receive(msgIMPASSE);
		if (msgReceivedIMPASSE!=null) {
			ArrayList<Location> node=null;
			try {
				node = (ArrayList<Location>) msgReceivedIMPASSE.getContentObject();
				System.out.println(color + agentName+ " : J'ai reçu un noeud d'un agent dans une impasse");
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			myMap.addNewNode(node.get(0).getLocationId());
			myMap.addNewNode(node.get(1).getLocationId());
        	myMap.addEdge(node.get(0).getLocationId(), node.get(1).getLocationId());
        	myMap.addNode(node.get(0).getLocationId(),MapAttribute.closed);
        	if(myMap2!=null) {
        		myMap2.addNewNode(node.get(0).getLocationId());
    			myMap2.addNewNode(node.get(1).getLocationId());
            	myMap2.addEdge(node.get(0).getLocationId(), node.get(1).getLocationId());
            	myMap2.addNode(node.get(0).getLocationId(),MapAttribute.closed);
        	}
        	System.out.println(color + agentName+ " : Je l'ai ajouté à ma carte");
		}
		
		
		//Reception données
		List<Serializable> s=null;
		MessageTemplate msg=MessageTemplate.and(
				MessageTemplate.MatchProtocol("DATA"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msg);
		if (msgReceived!=null) {
			try {
				s = (List<Serializable>) msgReceived.getContentObject();
				System.out.println(color + agentName+ " : J'ai son message sur les données");
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}	
		
		if(s!=null) {
			//s[0] : carte
			myMap.mergeMap((SerializableSimpleGraph<String, MapAttribute>) s.get(0));
			System.out.println(color + agentName+" : J'ai reçu la carte.");
			
			//s[1] : liste_pos_ressources
			((MyCollectAgent) this.myAgent).setListe_pos_ressources(mergeRessources(liste_pos_ressources, (HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>>) s.get(1)));
			liste_pos_ressources = ((MyCollectAgent) this.myAgent).getListe_pos_ressources();
			System.out.println(color + agentName+" : J'ai reçu la position des données : "+(HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>>) s.get(1));
			System.out.println(color + agentName+" : Ma nouvelle liste : "+liste_pos_ressources);
			
			//s[2] : agent types
			HashMap<String, String> mergedMap = new HashMap<>((HashMap<String, String>) s.get(2)); // Copie initiale de la première HashMap
	        for (String key : agent_types.keySet()) {
	            // Si la clé n'existe pas dans mergedMap, ou si tu veux écraser la valeur existante :
	            mergedMap.put(key, agent_types.get(key));
	        }
			((MyCollectAgent) this.myAgent).setAgent_types(mergedMap);
			agent_types = ((MyCollectAgent) this.myAgent).getAgent_types();
			System.out.println(color + agentName+" : J'ai reçu la liste de type des agents : "+agent_types);
			
			//s[3] : couple<pos,nom>
			if(s.size()>3) {
				Tuple3<String, Location, Instant> tankerReceived = (Tuple3<String, Location, Instant>) s.get(3);
				if(tanker.getThird()==null || tankerReceived.getThird().isAfter(tanker.getThird())) {
					((MyCollectAgent) this.myAgent).setTanker(tankerReceived);
				}
				System.out.println(color + agentName+" : J'ai reçu la position du tank et son nom : "+tanker.getFirst()+ " "+tanker.getSecond());
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
	
	// fonction pour fusionner les données en gardant la plus récente
	public HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> mergeRessources(
		    HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> localListe,
		    HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> receivedListe) {
		    
		    // nouvelle map fusionnée
		    HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> mergedListe = new HashMap<>();
		    
		    // on copie tout ce qui est en local d'abord
		    for (Map.Entry<String, ArrayList<Tuple3<String, Integer, Instant>>> entry : localListe.entrySet()) {
		        mergedListe.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		    }

		    // on parcourt ce qu'on a reçu
		    for (Map.Entry<String, ArrayList<Tuple3<String, Integer, Instant>>> entry : receivedListe.entrySet()) {
		        String resource = entry.getKey();
		        ArrayList<Tuple3<String, Integer, Instant>> receivedList = entry.getValue();
		        
		        // récupère ou crée la liste dans la fusion
		        ArrayList<Tuple3<String, Integer, Instant>> mergedList = mergedListe.get(resource);
		        if (mergedList == null) {
		            mergedList = new ArrayList<>();
		            mergedListe.put(resource, mergedList);
		        }
		        
		        for (Tuple3<String, Integer, Instant> receivedTuple : receivedList) {
		            String receivedLocation = receivedTuple.getFirst();
		            Integer receivedQuantity = receivedTuple.getSecond();
		            Instant receivedTime = receivedTuple.getThird();
		            
		            boolean found = false;
		            for (int i = 0; i < mergedList.size(); i++) {
		                Tuple3<String, Integer, Instant> mergedTuple = mergedList.get(i);
		                
		                if (mergedTuple.getFirst().equals(receivedLocation)) {
		                    // même position => on garde celui avec le temps le plus récent
		                    if (receivedTime.isAfter(mergedTuple.getThird())) {
		                        Tuple3<String, Integer, Instant> updatedTuple = new Tuple3<>(receivedLocation, receivedQuantity, receivedTime);
		                        mergedList.set(i, updatedTuple);
		                    }
		                    found = true;
		                    break;
		                }
		            }
		            
		            if (!found) {
		                // si pas trouvé, on ajoute direct
		                mergedList.add(new Tuple3<>(receivedLocation, receivedQuantity, receivedTime));
		            }
		        }
		    }
		    
		    return mergedListe;
		}

}
