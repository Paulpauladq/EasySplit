package easysplit.model.server.paxos.leader;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Leader extends Remote {

  String REGISTRY_NAME = "Leader";

  boolean isAlive() throws RemoteException;

  void startNewElection() throws RemoteException;

  int sendElectionMessage(int instancePort) throws RemoteException;

  void acceptNewLeader(int leaderPort) throws RemoteException;

  int getLeaderPort() throws RemoteException;
}
