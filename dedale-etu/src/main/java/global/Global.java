package global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import dataStructures.tuple.Couple;
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
	
	// temps de déplacement
	public static int temps = 250;
	
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
	
	// fonction pour se déplacer à un point avec un chemin
	public static void move(List<String> liste, AbstractDedaleAgent myAgent, String color, String agentName) {
		
		boolean moved;
	    while (!liste.isEmpty()) {
	    	
	        String nextNode = liste.get(0);
	        
	        System.out.println(color+agentName+" : "+myAgent.getCurrentPosition()+ " : "+liste);
	        System.out.println(color+agentName+" : "+myAgent.getCurrentPosition()+ " : "+nextNode);
	        
	        moved = Global.moveNextNode(nextNode, (AbstractDedaleAgent) myAgent, color, agentName);
	        
	        if(moved) {
	        	liste.remove(0);
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


	public static boolean move2(String nextNode, List<String> liste, AbstractDedaleAgent myAgent, String color, String agentName) {
		//Réception demande de bouger
		MessageTemplate msgMOVE=MessageTemplate.and(
				MessageTemplate.MatchProtocol("MOVE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceivedMOVE=myAgent.receive(msgMOVE);
		if (msgReceivedMOVE!=null) {
			
			System.out.println(color + agentName + " : Je dois bouger mais j'étais en chemin pour "+nextNode);
			
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
			if(listToMove.size()==0) {
				System.out.println(color+agentName+" : Je suis dans une impasse.");
			}else {
				
				pos = listToMove.get((int)(Math.random() * listToMove.size()));
				myMap.addNewNode(pos.getLocationId());
	        	myMap.addEdge(myPosition.getLocationId(), pos.getLocationId());
	        	
	        	if(myAgent.moveTo(new GsLocation(pos.toString()))) {
					System.out.println(color+agentName+" : J'ai réussi à m'écarter du chemin.");
					
	    			List<String> nouveauChemin = myMap.getShortestPath(pos.toString(), liste.get(liste.size()-1));
	    			move(nouveauChemin, myAgent, color, agentName);
	    			return true;
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
			System.out.println(color + agentName+" : Je peux pas avancer au next node : "+nextNodeId);
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
				System.out.println(color + agentName+" : "+ receiver + " tu peux bouger stp");
				//J'ai pas réussi à me déplacer
				return false;
			}
			
		}
		//J'ai réussi à me déplacer
		return true;
	}

}
