package eu.su.mas.dedaleEtu.mas.myBehaviours.Tanker;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.myAgents.MyTankerAgent;
import global.Global;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;


public class MyTankerBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/* Gestion FSM */
	private boolean finished = false;
	private int exit;
	
	/* Constructeur */
	public MyTankerBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	public void action() {
		
		/* Affichage */
		String agentName = ((MyTankerAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		List<String> listToMove = new ArrayList<String>();
		List<Couple<Location, List<Couple<Observation, String>>>> observations = ((AbstractDedaleAgent) this.myAgent).observe();
		
		/* Gestion des agents */
		Location posSender = ((MyTankerAgent) this.myAgent).getPosSender();
		String receiver = null;
		
		
		//Si bloque un autre agent, alors doit se déplacer
		if(posSender!=null) {
			
			//Parcours des observations
			for (Couple<Location, List<Couple<Observation, String>>> locationCouple : observations) {
			    Location location = locationCouple.getLeft();
			    List<Couple<Observation, String>> observationDetails = locationCouple.getRight();
			    
			    //Liste des positions accessibles
			    listToMove.add(location.toString());
			    
			    //si location == position du sender, alors recup son nom
			    if (location.toString().equals(posSender.toString())) {
			    	
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
			
			//On retire la position actuelle et celle de l'agent qui demande à ce qu'on se déplace
			listToMove.remove(myPosition.toString());
			listToMove.remove(posSender.toString());
			
			//Choix aléatoire parmis les positions restantes
			String pos = null;
			
			//si aucune possibilité, impasse
			if(listToMove.size()==0) {
				System.out.println(color+agentName+" : Je suis dans une impasse.");
				
				// Je dis que je suis dans une impaase et j'envoie mon noeud
				if(receiver!=null) {
					//Envoie du noeud
					ACLMessage msgIMPASSE = new ACLMessage(ACLMessage.INFORM);
					msgIMPASSE.setProtocol("IMPASSE");
					msgIMPASSE.setSender(this.myAgent.getAID());
					msgIMPASSE.addReceiver(new AID(receiver,AID.ISLOCALNAME));
					ArrayList<Location> node = new ArrayList<Location>();
					node.add(myPosition);
					node.add(posSender);
					try {					
						msgIMPASSE.setContentObject((Serializable) node);
					} catch (IOException e) {
						e.printStackTrace();
					}
					((AbstractDedaleAgent)this.myAgent).sendMessage(msgIMPASSE);
					System.out.println(color+agentName+" : J'ai envoyé un message d'impasse avec mon noeud");
				}
			
			//choix aléatoire puis déplacement
			}else {
				pos = listToMove.get((int)(Math.random() * listToMove.size()));
				
				if(((AbstractDedaleAgent)this.myAgent).moveTo(new GsLocation(pos))) {
					System.out.println(color+agentName+" : J'ai réussi à me déplacer pour laisser le chemin.");
					((MyTankerAgent) this.myAgent).setPosSender(null);
				}else {
					System.out.println(color+agentName+" : Je n'ai pas réussi à me déplacer pour laisser le chemin.");
				}
			}
			((MyTankerAgent) this.myAgent).setPosSender(null);
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
