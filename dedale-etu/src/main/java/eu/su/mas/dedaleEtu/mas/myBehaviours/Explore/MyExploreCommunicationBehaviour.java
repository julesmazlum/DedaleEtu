package eu.su.mas.dedaleEtu.mas.myBehaviours.Explore;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dataStructures.serializableGraph.SerializableSimpleGraph;
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
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Serializable;


public class MyExploreCommunicationBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyExploreCommunicationBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	@SuppressWarnings({ "unchecked" })
	public void action() {
		
		/*
		 * Données
		 */
		
		/* Affichage */
		String agentName = ((MyExploreAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		Location myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		List<Couple<Location, List<Couple<Observation, String>>>> observations = ((AbstractDedaleAgent) this.myAgent).observe();
		HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer,Integer>, Instant>>> listTreasureData = ((MyExploreAgent) this.myAgent).getListTreasureData();
		MapRepresentation myMap = ((MyExploreAgent) this.myAgent).getMyMap();
		MapRepresentation myMap2 = ((MyExploreAgent) this.myAgent).getMyMap2();
		List<Location> listToMove = new ArrayList<>();
		
		/* Gestion des agents */
		Map<String, String> agentTypes = ((MyExploreAgent) this.myAgent).getAgentTypes();
		Tuple3<String, Location, Instant> tanker = ((MyExploreAgent) this.myAgent).getTanker();
		
		
		/*
		 * Réception des messages
		 */
		
		
		
		/* Réception du type d'un agent */
		MessageTemplate msgREPLYTYPE2=MessageTemplate.and(
				MessageTemplate.MatchProtocol("REPLY-TYPE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceivedREPLYTYPE=this.myAgent.receive(msgREPLYTYPE2);
		if (msgReceivedREPLYTYPE!=null) {
			ArrayList<String> sltype2=null;
			try {
				sltype2 = (ArrayList<String>) msgReceivedREPLYTYPE.getContentObject();
				System.out.println(color + agentName+ " : J'ai reçu son type : "+sltype2);
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			//Ajouter l'information dans le dictionnaire correspondant
			agentTypes.put(sltype2.get(0), sltype2.get(1));
			//Si le type est agentTanker, on ajoute l'information dans l'attribut tanker
			if(sltype2.get(1).equals("agentTanker")) {
				Tuple3<String, Location, Instant> newTanker = new Tuple3<>(sltype2.get(0), null, null);
				((MyExploreAgent) this.myAgent).setTanker(newTanker);
			}
			System.out.println(color + agentName+ " : Je l'ai ajouté au dictionnaire : "+agentTypes);			
		}
		
		
		
		/* Réception demande de type */
		MessageTemplate msgASKTYPE=MessageTemplate.and(
				MessageTemplate.MatchProtocol("ASK-TYPE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceivedTYPE=this.myAgent.receive(msgASKTYPE);
		//S'il y a bien qlq demandant le type
		if (msgReceivedTYPE!=null) {
			String asker=null;
			try {
				asker = (String) msgReceivedTYPE.getContentObject();
				System.out.println(color + agentName+ " : "+asker + " me demande mon type.");
				
				//Envoie du type à asker
				ACLMessage msgREPLYTYPE = new ACLMessage(ACLMessage.INFORM);
				msgREPLYTYPE.setProtocol("REPLY-TYPE");
				msgREPLYTYPE.setSender(this.myAgent.getAID());
				String receiver = asker;
				msgREPLYTYPE.addReceiver(new AID(receiver,AID.ISLOCALNAME));
				
				ArrayList<String> sltype = new ArrayList<>();
				sltype.add(agentName);
				sltype.add("agentExplo");
				
				try {					
					msgREPLYTYPE.setContentObject(sltype);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				((AbstractDedaleAgent)this.myAgent).sendMessage(msgREPLYTYPE);
				System.out.println(color + agentName+ " : Je lui envoie mon type : "+sltype);
				
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
		}
		
		
		
		/* Réception demande de me déplacer */
		MessageTemplate msgMOVE=MessageTemplate.and(
				MessageTemplate.MatchProtocol("MOVE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceivedMOVE=this.myAgent.receive(msgMOVE);
		Location posSender = null;
		if (msgReceivedMOVE!=null) {
			System.out.println(color + agentName + " : Je dois me déplacer et je suis pas sur un chemin précis.");
			
			//Récupération de la position de l'agent qui demande à ce qu'on se déplace
			try {
				posSender = (Location) msgReceivedMOVE.getContentObject();
			} catch (UnreadableException e) {
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
			
			//Si je n'ai nul part ou aller
			if(listToMove.size()==0) {
				System.out.println(color+agentName+" : Je suis dans une impasse.");
			}else {
				
				pos = listToMove.get((int)(Math.random() * listToMove.size()));
				myMap.addNewNode(pos.getLocationId());
	        	myMap.addEdge(myPosition.getLocationId(), pos.getLocationId());
				
	        	
	        	if(!Global.moveNextNode(pos.toString(), (AbstractDedaleAgent) myAgent, color, agentName)) {
	        		//Si je n'arrive pas à me déplacer
					System.out.println(color+agentName+" : Je n'ai pas réussi à m'écarter du chemin.");
				}else {
					//Sinon je me déplace
					System.out.println(color+agentName+" : J'ai réussi à m'écarter du chemin.");
				}
				
			}
		}
		
		
		
		/* Réception impasse en tant que explo */
		MessageTemplate msgIMPASSE=MessageTemplate.and(
				MessageTemplate.MatchProtocol("IMPASSE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceivedIMPASSE=this.myAgent.receive(msgIMPASSE);
		if (msgReceivedIMPASSE!=null) {
			ArrayList<Location> node=null;
			try {
				//node : noeud de sender + noeud oue je suis
				node = (ArrayList<Location>) msgReceivedIMPASSE.getContentObject();
				System.out.println(color + agentName+ " : J'ai reçu un noeud d'un agent dans une impasse.");
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			
			// j'ajoute ce noeud dans ma carte, comme ça je n'ai pas à aller sur le noeud de l'agent qui dans une impasse
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
        	System.out.println(color + agentName+ " : Je l'ai ajouté à ma carte.");
		}
		
		
		
		/* Réception données */
		List<Serializable> s=null;
		MessageTemplate msg=MessageTemplate.and(
				MessageTemplate.MatchProtocol("DATA"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msg);
		if (msgReceived!=null) {
			try {
				s = (List<Serializable>) msgReceived.getContentObject();
				System.out.println(color + agentName+ " : J'ai reçu des données");
			} catch (UnreadableException e) {
				e.printStackTrace();
			}		
		}	
		
		if(s!=null) {
			//s[0] : carte
			myMap.mergeMap((SerializableSimpleGraph<String, MapAttribute>) s.get(0));
			System.out.println(color + agentName+" : J'ai reçu la carte.");
			
			//s[1] : liste_pos_ressources
			//merge des deux liste (locale et reçu) en gardant celle avec les informations les plus récentes
			((MyExploreAgent) this.myAgent).setListTreasureData(Global.mergeRessources(listTreasureData, (HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer,Integer>, Instant>>>) s.get(1)));
			listTreasureData = ((MyExploreAgent) this.myAgent).getListTreasureData();
			System.out.println(color + agentName+" : J'ai reçu les informations sur les données : "+(HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>>) s.get(1));
			System.out.println(color + agentName+" : Ma nouvelle liste des données : "+listTreasureData);
			
			//s[2] : agent types
			//merge des deux dictionnaires
			HashMap<String, String> mergedMap = new HashMap<>((HashMap<String, String>) s.get(2));
	        for (String key : agentTypes.keySet()) {
	            mergedMap.put(key, agentTypes.get(key));
	        }
			((MyExploreAgent) this.myAgent).setAgentTypes(mergedMap);
			agentTypes = ((MyExploreAgent) this.myAgent).getAgentTypes();
			System.out.println(color + agentName+" : J'ai reçu la liste de type des agents : "+agentTypes);
			
			//s[3] : couple<pos,nom>
			//si le sender avait cette information et l'a envoyé
			if(s.size()>3) {
				Tuple3<String, Location, Instant> tankerReceived = (Tuple3<String, Location, Instant>) s.get(3);
				//Si je n'avais aucune information sur tanker OU l'information que j'ai reçu est plus récente, je garde l'information reçu
				if(tanker.getThird()==null || tankerReceived.getThird().isAfter(tanker.getThird())) {
					((MyExploreAgent) this.myAgent).setTanker(tankerReceived);
					System.out.println(color + agentName+" : J'ai reçu la (nouvelle) position du tanker et son nom : "+tanker.getFirst()+ " "+tanker.getSecond());
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
