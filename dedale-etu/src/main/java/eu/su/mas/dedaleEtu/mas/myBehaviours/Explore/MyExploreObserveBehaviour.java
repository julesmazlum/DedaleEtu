package eu.su.mas.dedaleEtu.mas.myBehaviours.Explore;


import java.io.IOException;
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
import eu.su.mas.dedaleEtu.mas.myAgents.MyExploreAgent;
import global.Global;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import java.time.Instant;


public class MyExploreObserveBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;
	
	/* Gestion FSM */
	private boolean finished = false;
	private int exit;

	/* Constructeur */
	public MyExploreObserveBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	/* Behaviour */
	@Override
	public void action() {
		
		/*
		 * Données
		 */
		
		/* Affichage */
		String agentName = ((MyExploreAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		MapRepresentation myMap = ((MyExploreAgent) this.myAgent).getMyMap();
		Location myPosition =((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		List<Couple<Location, List<Couple<Observation, String>>>> observations = ((AbstractDedaleAgent) this.myAgent).observe();
		HashMap<String, ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>>> listTreasureData = ((MyExploreAgent) this.myAgent).getListTreasureData();
		
		/* Gestion des agents */
		Map<String, String> agent_types = ((MyExploreAgent) this.myAgent).getAgentTypes();
		Tuple3<String, Location, Instant> tanker = ((MyExploreAgent) this.myAgent).getTanker();
		


		//Parcours des observations
		for (Couple<Location, List<Couple<Observation, String>>> locationCouple : observations) {
		    Location location = locationCouple.getLeft();
		    List<Couple<Observation, String>> observationDetails = locationCouple.getRight();

		    // afficher la location observée
		    //System.out.println("Location observée : " + location);
		    
		    ArrayList<Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant>> liste = null;
		    
		    // parcourir les détails des observations
		    for (Couple<Observation, String> detail : observationDetails) {
		        Observation obs = detail.getLeft();
		        String valeur = detail.getRight();

		        // afficher les détails
		        //System.out.println("  Observation : " + obs.getName() + ", Valeur : " + valeur);
		        
		        /* Détection d'un agent */
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
		        		
		        		String msg = ((MyExploreAgent) this.myAgent).getLocalName();
		        		
		        		try {					
		        			msgASKTYPE.setContentObject(msg);
		        		} catch (IOException e) {
		        			e.printStackTrace();
		        		}
		        		((AbstractDedaleAgent)this.myAgent).sendMessage(msgASKTYPE);
		        		System.out.println(color + agentName+ " : J'ai demandé son type.");
		        	}
		        	
		        	
		        	//Si je connais son type et ce n'est pas le tanker
		        	if (agent_types.containsKey(valeur) && !"agentTanker".equals(agent_types.get(valeur))) {
			        	System.out.println(color + agentName+ " : J'ai rencontré "+valeur+" je vais lui envoyer les données que j'ai (carte, position ressources et tank)");
			        	((MyExploreAgent) this.myAgent).setAgentNameToSendTo(valeur);
			        	exit = 2;
						finished = true;
						return;
		        	} 
		        	
		        	//Si c'est le tanker
		        	if(agent_types.containsKey(valeur) && "agentTanker".equals(agent_types.get(valeur))){
			        	//Alors j'enregistre sa position et j'ajoute son noeud dans la carte
		        		Tuple3<String, Location, Instant> newtanker = new Tuple3<>(tanker.getFirst(), location, Instant.now());
		        		((MyExploreAgent) this.myAgent).setTanker(newtanker);
		        		
			        	myMap.addNewNode(location.getLocationId());
			        	myMap.addEdge(myPosition.getLocationId(), location.getLocationId());
			        	System.out.println(color + agentName+" : J'ai rencontré "+valeur+" qui est le tanker j'ai ajouté sa position.");
		        	}
			        
		        }
		        
		        //Si je croise des ressources.
		        if(obs.getName().equals("Diamond")||obs.getName().equals("Gold")) {
		        	System.out.println(color + agentName+" : J'ai trouvé "+valeur+" "+obs.getName()+" à la position "+location);
		        	
		        	// récupère la liste pour ce type de ressource
		        	liste = listTreasureData.get(obs.getName());

		        	// si la ressource n'existe pas encore dans la map, on crée une nouvelle liste
		        	if (liste == null || liste.size()==0) {
		        	    liste = new ArrayList<>();
		        	    listTreasureData.put(obs.getName(), liste);
		        	    System.out.println(color + agentName+" : Je n'en avais jamais trouvé avant.");
		        	}

		        	boolean found = false;

		        	// on parcourt les tuples
		        	for (int i = 0; i < liste.size(); i++) {
		        		Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple = liste.get(i);
		        	    
		        	    if (tuple.get_1().equals(location.toString())) {
		        	    	System.out.println(color + agentName+" : J'en avais déjà trouvé avant : "+tuple);
		        	        // si la position existe, on update la quantité et le temps
		        	        int newQuantite = Integer.parseInt(valeur); 
		        	        Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> updatedTuple = new Tuple4<>(location.toString(), newQuantite, tuple.get_3(), Instant.now());
		        	        System.out.println(color + agentName+" : Je met à jour la quantité et le temps "+updatedTuple);
		        	        liste.set(i, updatedTuple);
		        	        listTreasureData.put(obs.getName(), liste);
		        	        found = true;
		        	        break;
		        	    }
		        	}

		        	if (!found) {
		        	    // si pas trouvé, on ajoute un nouveau tuple
		        		Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> newTuple = new Tuple4<>(location.toString(), Integer.parseInt(valeur), new Tuple3<>(0,0,1), Instant.now());
		        	    liste.add(newTuple);
		        	    listTreasureData.put(obs.getName(), liste);
		        	    System.out.println(color + agentName+" : Je l'ajoute dans la liste : "+liste);
		        	}

		        }
		        
		        // mise à jour de l'état du coffre
		        if(liste!=null && (obs.getName().equals("LockIsOpen"))) {
		        	if(Integer.parseInt(valeur)==0) {
		        		System.out.println(color + agentName+" : Le coffre est fermé.");
		        	}else {
		        		System.out.println(color + agentName+" : Le coffre est ouvert.");
		        	}
		        	// on parcourt les tuples
		        	for (int i = 0; i < liste.size(); i++) {
		        		Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple = liste.get(i);
		        	    
		        	    if (tuple.get_1().equals(location.toString())) {
		        	        // si la position existe, on update la quantité et le temps
		        	    	Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> updatedTuple = new Tuple4<>(location.toString(), tuple.get_2(), new Tuple3<>(tuple.get_3().getFirst(),tuple.get_3().getSecond(), Integer.parseInt(valeur)), Instant.now());
		        	        System.out.println(color + agentName+" : Je met à jour l'état "+updatedTuple);
		        	        liste.set(i, updatedTuple);
		        	        break;
		        	    }
		        	}
		        }
		        
		        // mise à jour du stregth qu'il faut pour ouvrir le coffre
		        if(liste!=null && (obs.getName().equals("Strength"))) {
		        	System.out.println(color + agentName+" : Son strength est de "+valeur);
		        	
		        	// on parcourt les tuples
		        	for (int i = 0; i < liste.size(); i++) {
		        		Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple = liste.get(i);
		        	    
		        	    if (tuple.get_1().equals(location.toString())) {
		        	        // si la position existe, on update la quantité et le temps
		        	    	Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> updatedTuple = new Tuple4<>(location.toString(), tuple.get_2(), new Tuple3<>(tuple.get_3().getFirst(),Integer.parseInt(valeur), tuple.get_3().getThird()), Instant.now());
		        	        System.out.println(color + agentName+" : Je met à jour le strength "+updatedTuple);
		        	        liste.set(i, updatedTuple);
		        	        break;
		        	    }
		        	}
		        }
		        
		        // mise à jour du lockpicking qu'il faut pour ouvrir le coffre
		        if(liste!=null && (obs.getName().equals("LockPicking"))) {
		        	System.out.println(color + agentName+" : Son lockpicking est de "+valeur);

		        	// on parcourt les tuples
		        	for (int i = 0; i < liste.size(); i++) {
		        		Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> tuple = liste.get(i);
		        	    
		        	    if (tuple.get_1().equals(location.toString())) {
		        	    	System.out.println(color + agentName+" : J'ai trouvé : "+tuple);
		        	        // si la position existe, on update la quantité et le temps
		        	    	Tuple4<String, Integer,Tuple3<Integer, Integer, Integer>, Instant> updatedTuple = new Tuple4<>(location.toString(), tuple.get_2(), new Tuple3<>(Integer.parseInt(valeur),tuple.get_3().getSecond(), tuple.get_3().getThird()), Instant.now());
		        	        System.out.println(color + agentName+" : Je met à jour le lockpicking "+updatedTuple);
		        	        liste.set(i, updatedTuple);
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
