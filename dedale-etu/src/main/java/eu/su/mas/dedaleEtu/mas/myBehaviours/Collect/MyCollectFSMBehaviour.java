package eu.su.mas.dedaleEtu.mas.myBehaviours.Collect;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class MyCollectFSMBehaviour extends FSMBehaviour {

    private static final long serialVersionUID = 1L;
    
	private static final String STATE_EXPLORE = "Explore";
	private static final String STATE_OBSERVE_AND_PICK = "ObserveAndPick";
	private static final String STATE_COM = "Communication";
	private static final String STATE_MOVE = "Move";


    public MyCollectFSMBehaviour(final AbstractDedaleAgent myagent) {
        // créer les comportements
        SimpleBehaviour explore = new MyCollectBehaviour(myagent);
        SimpleBehaviour observeAndPick = new MyCollectObserveAndPickBehaviour(myagent);
        SimpleBehaviour communication = new MyCollectCommunicationBehaviour(myagent);
        SimpleBehaviour move = new MyCollectMoveBehaviour(myagent);


        // ajouter les états à la machine
        registerFirstState(explore, STATE_EXPLORE);
        registerState(observeAndPick, STATE_OBSERVE_AND_PICK);
        registerState(communication, STATE_COM);
        registerState(move, STATE_MOVE);


        // définir les transitions
        registerTransition(STATE_EXPLORE, STATE_OBSERVE_AND_PICK, 1);
        registerTransition(STATE_EXPLORE, STATE_MOVE, 2);
        registerTransition(STATE_MOVE, STATE_OBSERVE_AND_PICK, 1);
        registerTransition(STATE_OBSERVE_AND_PICK, STATE_COM, 1);
        registerTransition(STATE_COM, STATE_EXPLORE, 1);



    }
}