package global;

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
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.myAgents.MyCollectAgent;
import eu.su.mas.dedaleEtu.mas.myAgents.MyExploreAgent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class Global {
	
	
	/*
	 * Temps de déplacement
	 */
	public static int temps = 100;
	
	/*
	 * Affichage
	 */
	
	// définir les codes de couleur ansi
	public static final String RESET = "\u001B[0m";
	public static final String RED = "\u001B[38;2;255;0;0m";
	public static final String GREEN = "\u001B[38;2;19;140;12m";
	public static final String PINK = "\u001B[38;2;255;143;199m";
	public static final String PURPLE = "\u001B[35m";
	public static final String BROWN = "\u001B[33m";
	public static final String ORANGE = "\u001B[38;5;214m";
	public static final String CYAN = "\u001B[36m";
	public static final String BLACK = "\u001B[30m";

	// fonction pour obtenir une couleur en fonction du nom de l'agent
	public static String getColorForAgent(String agentName) {
	    switch (agentName) {
	        case "Mario": return RED;
	        case "Luigi": return GREEN;
	        case "Peach": return PINK;
	        case "Daisy": return PURPLE;
	        case "Toad": return BROWN;
	        case "Bowser": return ORANGE;
	        default: return CYAN; // couleur par défaut
	    }
	}
	
	/*
	 * Fonction de déplacement
	 */
	
	// fonction pour se déplacer à un point avec un chemin
	public static void move(List<String> liste, AbstractDedaleAgent myAgent, String color, String agentName) {
		
		boolean moved;
		//tant que je chemin n'est pas fini
	    while (!liste.isEmpty()) {
	    	
	        String nextNode = liste.get(0);
	        
	        //essayer de se déplacer au prochain noeud
	        moved = Global.moveNextNode(nextNode, (AbstractDedaleAgent) myAgent, color, agentName);
	        
	        //si réussi, passer enlever le noeud actuel
	        if(moved) {
	        	liste.remove(0);
	        //sinon, un blocage a eu lieu il faut se déplacer
	        }else {
	        	if(move2(nextNode, liste, myAgent, color, agentName)) {
	        		return;
	        	}
	        }

	        try {
	        	myAgent.doWait(Global.temps);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

	// fonction de déplacement avec blocage
	public static boolean move2(String nextNode, List<String> liste, AbstractDedaleAgent myAgent, String color, String agentName) {
		
		//Réception demande de bouger
		MessageTemplate msgMOVE=MessageTemplate.and(
				MessageTemplate.MatchProtocol("MOVE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceivedMOVE=myAgent.receive(msgMOVE);
		if (msgReceivedMOVE!=null) {
			
			System.out.println(color + agentName + " : Je dois me déplacer mais j'étais en chemin pour "+nextNode);
			
			//Données utiles
			List<Couple<Location, List<Couple<Observation, String>>>> observations = myAgent.observe();
			List<Location> listToMove = new ArrayList<>();
			Location myPosition=myAgent.getCurrentPosition();
			MapRepresentation myMap = null;
			Location posSender = null;
			
			//Récupération de la position de l'autre agent
			try {
				posSender = (Location) msgReceivedMOVE.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Récupération de myMap
			if(myAgent instanceof MyCollectAgent) {
				myMap = ((MyCollectAgent) myAgent).getMyMap();
			}
			if(myAgent instanceof MyExploreAgent) {
				myMap = ((MyExploreAgent) myAgent).getMyMap();
			}
			
			
			//Parcours des observations
			//Liste des positions disponibles pour m'écarter
			for (Couple<Location, List<Couple<Observation, String>>> locationCouple : observations) {
			    Location location = locationCouple.getLeft();
			    if(!location.toString().equals(myPosition.toString()) && !location.toString().equals(posSender.toString())) {
			    	listToMove.add(location);
			    }
			}
			
			//Choix aléatoire parmi les options
			Location pos = null;
			//si aucun choix possible, impasse
			if(listToMove.size()==0) {
				System.out.println(color+agentName+" : Je suis dans une impasse.");
				
			//sinon, choix aléatoire et déplacement
			}else {
				pos = listToMove.get((int)(Math.random() * listToMove.size()));
				myMap.addNewNode(pos.getLocationId());
	        	myMap.addEdge(myPosition.getLocationId(), pos.getLocationId());
	        	
	        	//tentative de déplacement
	        	if(myAgent.moveTo(new GsLocation(pos.toString()))) {
					System.out.println(color+agentName+" : J'ai réussi à m'écarter du chemin.");
					
					//calcul du nouveau chemin vers la destination depuis le nouveau noeud
	    			List<String> nouveauChemin = myMap.getShortestPath(pos.toString(), liste.get(liste.size()-1));
	    			move(nouveauChemin, myAgent, color, agentName);
	    			return true;
	    			//retourner true pour annuler la fonction appelante
	    			
	    		//si pas réussi on retante depuis la fonction appelante
				}else {
					System.out.println(color+agentName+" : J'ai pas réussi à m'écarter du chemin");
				}	
			}
		}
		return false;
	}
	
	
	// fonction pour se déplacer au prochain noeud
	public static boolean moveNextNode(String nextNodeId, AbstractDedaleAgent myagent, String color, String agentName) {
		List<Couple<Location,List<Couple<Observation,String>>>> lobs= myagent.observe();
		Location myPosition = myagent.getCurrentPosition();
		
		//Bloqué
		if(!myagent.moveTo(new GsLocation(nextNodeId))) {
			System.out.println(color + agentName+" : Je peux pas avancer au noeud suivant : "+nextNodeId);
			String receiver = null;
			
			//Récupérer le nom de l'agent qui nous bloque
			for (Couple<Location, List<Couple<Observation, String>>> locationCouple : lobs) {
			    Location location = locationCouple.getLeft();
			    List<Couple<Observation, String>> observationDetails = locationCouple.getRight();
			    
			    if (location.toString().equals(nextNodeId)) {
			    	for (Couple<Observation, String> detail : observationDetails) {
				        Observation obs = detail.getLeft();
				        String valeur = detail.getRight();
				        
				        if(obs.getName().equals("AgentName")) {
				        	receiver = valeur;
				        	break;
				        }
			    	}
			    }
			}

			//Envoyer un message à l'agent qui bloque
			if(receiver!=null) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("MOVE");
				msg.setSender(myagent.getAID());
				msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));
				try {					
					msg.setContentObject((java.io.Serializable) myPosition);
				} catch (IOException e) {
					e.printStackTrace();
				}
				myagent.sendMessage(msg);
				System.out.println(color + agentName+" : Hey "+ receiver + ", tu peux te déplacer pls <3.");
				//J'ai pas réussi à me déplacer
				return false;
			}	
		}
		
		//J'ai réussi à me déplacer
		return true;
	}
	
	// fonction pour fusionner les données en gardant la plus récente
	public static HashMap<String, ArrayList<Tuple4<String, Integer, Tuple3<Integer, Integer, Integer>, Instant>>> mergeRessources(
		    HashMap<String, ArrayList<Tuple4<String, Integer, Tuple3<Integer, Integer, Integer>, Instant>>> localListe,
		    HashMap<String, ArrayList<Tuple4<String, Integer, Tuple3<Integer, Integer, Integer>, Instant>>> receivedListe) {

		    HashMap<String, ArrayList<Tuple4<String, Integer, Tuple3<Integer, Integer, Integer>, Instant>>> mergedListe = new HashMap<>();

		    // copie des données locales
		    for (Map.Entry<String, ArrayList<Tuple4<String, Integer, Tuple3<Integer, Integer, Integer>, Instant>>> entry : localListe.entrySet()) {
		        mergedListe.put(entry.getKey(), new ArrayList<>(entry.getValue()));
		    }

		    // fusion avec les données reçues
		    for (Map.Entry<String, ArrayList<Tuple4<String, Integer, Tuple3<Integer, Integer, Integer>, Instant>>> entry : receivedListe.entrySet()) {
		        String resource = entry.getKey();
		        ArrayList<Tuple4<String, Integer, Tuple3<Integer, Integer, Integer>, Instant>> receivedList = entry.getValue();

		        ArrayList<Tuple4<String, Integer, Tuple3<Integer, Integer, Integer>, Instant>> mergedList = mergedListe.get(resource);
		        if (mergedList == null) {
		            mergedList = new ArrayList<>();
		            mergedListe.put(resource, mergedList);
		        }

		        for (Tuple4<String, Integer, Tuple3<Integer, Integer, Integer>, Instant> receivedTuple : receivedList) {
		            String receivedLocation = receivedTuple.get_1();
		            Integer receivedQuantity = receivedTuple.get_2();
		            Tuple3<Integer, Integer, Integer> receivedCouple = receivedTuple.get_3();
		            Instant receivedTime = receivedTuple.get_4();

		            boolean found = false;
		            for (int i = 0; i < mergedList.size(); i++) {
		                Tuple4<String, Integer, Tuple3<Integer, Integer, Integer>, Instant> mergedTuple = mergedList.get(i);

		                if (mergedTuple.get_1().equals(receivedLocation)) {
		                    if (receivedTime.isAfter(mergedTuple.get_4())) {
		                        Tuple4<String, Integer, Tuple3<Integer, Integer, Integer>, Instant> updatedTuple =
		                            new Tuple4<>(receivedLocation, receivedQuantity, receivedCouple, receivedTime);
		                        mergedList.set(i, updatedTuple);
		                    }
		                    found = true;
		                    break;
		                }
		            }

		            if (!found) {
		                mergedList.add(new Tuple4<>(receivedLocation, receivedQuantity, receivedCouple, receivedTime));
		            }
		        }
		    }

		    return mergedListe;
		}

}
