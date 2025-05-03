package eu.su.mas.dedaleEtu.mas.myBehaviours.Explore;


import java.io.IOException;
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
		
		/* Affichage */
		String agentName = ((MyExploreAgent) this.myAgent).getLocalName();
		String color = Global.getColorForAgent(agentName);
		
		/* Gestion de la carte */
		MapRepresentation myMap = ((MyExploreAgent) this.myAgent).getMyMap();
		Location myPosition =((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		List<Couple<Location, List<Couple<Observation, String>>>> observations = ((AbstractDedaleAgent) this.myAgent).observe();
		HashMap<String, ArrayList<Tuple3<String, Integer, Instant>>> liste_pos_ressources = ((MyExploreAgent) this.myAgent).getListe_pos_ressources();
		
		/* Gestion des agents */
		Map<String, String> agent_types = ((MyExploreAgent) this.myAgent).getAgent_types();
		Tuple3<String, Location, Instant> tanker = ((MyExploreAgent) this.myAgent).getTanker();
		


		//Parcours des observations
		for (Couple<Location, List<Couple<Observation, String>>> locationCouple : observations) {
		    Location location = locationCouple.getLeft();
		    List<Couple<Observation, String>> observationDetails = locationCouple.getRight();

		    // afficher la location observée
		    //System.out.println("Location observée : " + location);

		    // parcourir les détails des observations
		    for (Couple<Observation, String> detail : observationDetails) {
		        Observation obs = detail.getLeft();
		        String valeur = detail.getRight();

		        // afficher les détails
		        //System.out.println("  Observation : " + obs.getName() + ", Valeur : " + valeur);
		        
		        //Si on détécte un agent
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
		        		System.out.println(color + agentName+ " : J'ai demandé son type");
		        	}
		        	
		        	//Je lui envoie la carte
		        	if (agent_types.containsKey(valeur) && !"agentTanker".equals(agent_types.get(valeur))) {
			        	System.out.println(color + agentName+ " : J'ai rencontré "+valeur+" je vais lui envoyer les données que j'ai (carte, position ressources et tank)");
			        	((MyExploreAgent) this.myAgent).setAgentNameToSendTo(valeur);
			        	exit = 2;
						finished = true;
						return;
		        	} 
		        	
		        	if(agent_types.containsKey(valeur) && "agentTanker".equals(agent_types.get(valeur))){
			        	//Alors j'enregistre sa position et j'ajoute son noeud dans la carte
		        		Tuple3<String, Location, Instant> newtanker = new Tuple3<>(tanker.getFirst(), location, Instant.now());
		        		((MyExploreAgent) this.myAgent).setTanker(newtanker);
		        		
			        	myMap.addNewNode(location.getLocationId());
			        	myMap.addEdge(myPosition.getLocationId(), location.getLocationId());
			        	System.out.println(color + agentName+" : J'ai rencontré "+valeur+" j'ai ajouté sa position.");
		        	}
			        
		        }
		        
		        //Si je croise des ressources.
		        if(obs.getName().equals("Diamond")||obs.getName().equals("Gold")) {
		        	System.out.println(color + agentName+" : J'ai trouve "+valeur+" "+obs.getName()+" à la position "+location);
		        	Instant time1 = Instant.now();
		        	
		        	// récupère la liste pour ce type de ressource
		        	ArrayList<Tuple3<String, Integer, Instant>> liste = liste_pos_ressources.get(obs.getName());

		        	// si la ressource n'existe pas encore dans la map, on crée une nouvelle liste
		        	if (liste == null || liste.size()==0) {
		        	    liste = new ArrayList<>();
		        	    liste_pos_ressources.put(obs.getName(), liste);
		        	    System.out.println(color + agentName+" : Je n'en avais jamais trouvé avant");
		        	}

		        	boolean found = false;

		        	// on parcourt les tuples
		        	for (int i = 0; i < liste.size(); i++) {
		        	    Tuple3<String, Integer, Instant> tuple = liste.get(i);
		        	    
		        	    if (tuple.getFirst().equals(location.toString())) {
		        	    	System.out.println(color + agentName+" : J'ai avais déjà trouvé avant : "+tuple);
		        	        // si la position existe, on update la quantité et le temps
		        	        int newQuantite = Integer.parseInt(valeur); // ou tu veux additionner à l'ancienne valeur ? (dis-moi)
		        	        Tuple3<String, Integer, Instant> updatedTuple = new Tuple3<>(location.toString(), newQuantite, time1);
		        	        System.out.println(color + agentName+" : Je met à jour la quantité et le temps "+updatedTuple);
		        	        liste.set(i, updatedTuple);
		        	        found = true;
		        	        break;
		        	    }
		        	}

		        	if (!found) {
		        	    // si pas trouvé, on ajoute un nouveau tuple
		        	    Tuple3<String, Integer, Instant> newTuple = new Tuple3<>(location.toString(), Integer.parseInt(valeur), time1);
		        	    liste.add(newTuple);
		        	    System.out.println(color + agentName+" : Je l'ajoute dans la liste : "+liste);
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
