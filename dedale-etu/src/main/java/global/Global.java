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

public class Global {
	
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
	
	public static int temps = 100;

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
	
	public static void move(List<String> liste, AbstractDedaleAgent myAgent, String color, String agentName) {
		boolean moved;
	    while (!liste.isEmpty()) {
	        String nextNode = liste.get(0);
	        moved = Global.moveNextNode(nextNode, (AbstractDedaleAgent) myAgent, color, agentName);
	        
	        if(moved) {
	        	liste.remove(0);
	        }

	        try {
	        	myAgent.doWait(Global.temps);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	public static boolean moveNextNode(String nextNodeId, AbstractDedaleAgent myagent, String color, String agentName) {
		List<Couple<Location,List<Couple<Observation,String>>>> lobs= myagent.observe();
		
		if(!myagent.moveTo(new GsLocation(nextNodeId))) {
			System.out.println(color + agentName+" : Je peux pas avancer");
			String receiver = null;
			
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

			if(receiver!=null) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("MOVE");
				msg.setSender(myagent.getAID());
				msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));
				try {					
					msg.setContentObject((java.io.Serializable) nextNodeId);
				} catch (IOException e) {
					e.printStackTrace();
				}
				myagent.sendMessage(msg);
				System.out.println(color + agentName+" : "+ receiver + " tu peux bouger stp");
				return false;
			}
			
		}
		return true;
	}
	
	
	
	
	
	
	public static void moveNode(String nextNode, AbstractDedaleAgent myagent) {
        try {
            boolean success = myagent.moveTo(new GsLocation(nextNode));
            if (!success) {
                System.out.println("❌ Impossible de bouger vers " + nextNode);
                // tu peux décider ici de soit break, soit skip le node
                return; 
            }
        } catch (Exception e) {
            System.out.println("⚠️ ERREUR dans move vers " + nextNode + " : " + e.getMessage());
            return; // on stoppe la boucle si y'a une exception
        }

        try {
        	myagent.doWait(Global.temps);
        } catch (Exception e) {
            e.printStackTrace();
        }
	    
	}

	
	/*
    try {
        boolean success = myagent.moveTo(new GsLocation(nextNode));
        if (!success) {
            System.out.println("❌ Impossible de bouger vers " + nextNode);
            // tu peux décider ici de soit break, soit skip le node
        }
        liste.remove(0); // move réussi ➔ on passe au prochain
    } catch (Exception e) {
        System.out.println("⚠️ ERREUR dans move vers " + nextNode + " : " + e.getMessage());
        break; // on stoppe la boucle si y'a une exception
    }*/

}
