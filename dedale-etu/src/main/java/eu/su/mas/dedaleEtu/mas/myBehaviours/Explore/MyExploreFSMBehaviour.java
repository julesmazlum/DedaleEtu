package eu.su.mas.dedaleEtu.mas.myBehaviours.Explore;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class MyExploreFSMBehaviour extends FSMBehaviour {

    private static final long serialVersionUID = 1L;
    
	private static final String STATE_EXPLORE = "Explore";
	private static final String STATE_COM = "Communication";
	private static final String STATE_OBSERVE = "Observe";
	private static final String STATE_SEND = "Send";


    public MyExploreFSMBehaviour(final AbstractDedaleAgent myagent) {
        // créer les comportements
        SimpleBehaviour explore = new MyExploreBehaviour(myagent);
        SimpleBehaviour communication = new MyExploreCommunicationBehaviour(myagent);
        SimpleBehaviour observe = new MyExploreObserveBehaviour(myagent);
        SimpleBehaviour send = new MyExploreShareMapBehaviour(myagent);

        // ajouter les états à la machine
        registerFirstState(explore, STATE_EXPLORE);
        registerState(communication, STATE_COM);
        registerState(observe, STATE_OBSERVE);
        registerState(send, STATE_SEND);

        // définir les transitions
        registerTransition(STATE_EXPLORE, STATE_OBSERVE, 1);
        registerTransition(STATE_OBSERVE, STATE_COM, 1);
        registerTransition(STATE_OBSERVE, STATE_SEND, 2);
        registerTransition(STATE_COM, STATE_EXPLORE, 1);
        registerTransition(STATE_SEND, STATE_COM, 1);

    }
}