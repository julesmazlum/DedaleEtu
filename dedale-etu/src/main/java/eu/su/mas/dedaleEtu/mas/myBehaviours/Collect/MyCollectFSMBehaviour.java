package eu.su.mas.dedaleEtu.mas.myBehaviours.Collect;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;

public class MyCollectFSMBehaviour extends FSMBehaviour {

    private static final long serialVersionUID = 1L;
    
	private static final String STATE_HOME = "Home";
	private static final String STATE_OBSERVE = "Observe";
	private static final String STATE_COMMUNICATION = "Communication";
	private static final String STATE_MOVE = "Move";
	private static final String STATE_SEND = "Send";


    public MyCollectFSMBehaviour(final AbstractDedaleAgent myagent) {
        // créer les comportements
        SimpleBehaviour home = new MyCollectExploreBehaviour(myagent);
        SimpleBehaviour observe = new MyCollectObservePickEmptyBehaviour(myagent);
        SimpleBehaviour communication = new MyCollectCommunicationBehaviour(myagent);
        SimpleBehaviour move = new MyCollectMoveToTreasureBehaviour(myagent);
        SimpleBehaviour send = new MyCollectShareDataBehaviour(myagent);


        // ajouter les états à la machine
        registerFirstState(home, STATE_HOME);
        registerState(observe, STATE_OBSERVE);
        registerState(communication, STATE_COMMUNICATION);
        registerState(move, STATE_MOVE);
        registerState(send, STATE_SEND);


        // définir les transitions
        registerTransition(STATE_HOME, STATE_OBSERVE, 1);
        registerTransition(STATE_HOME, STATE_MOVE, 2);
        registerTransition(STATE_MOVE, STATE_OBSERVE, 1);
        registerTransition(STATE_OBSERVE, STATE_COMMUNICATION, 1);
        registerTransition(STATE_OBSERVE, STATE_SEND, 2);
        registerTransition(STATE_SEND, STATE_COMMUNICATION, 1);
        registerTransition(STATE_COMMUNICATION, STATE_HOME, 1);



    }
}