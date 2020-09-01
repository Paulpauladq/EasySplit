package easysplit.model.server.paxos;

import java.rmi.Remote;
import java.rmi.RemoteException;

import easysplit.model.server.paxos.acceptor.Acceptor;
import easysplit.model.server.paxos.leader.Leader;
import easysplit.model.server.paxos.pojo.Operation;

public interface Paxos extends Remote {

  String REGISTRY_NAME = "Paxos";

  String paxosOperation(final Operation operation) throws RemoteException, IllegalArgumentException, InterruptedException;

  Leader getLeaderRMI() throws RemoteException;

  Acceptor getAcceptorRMI() throws RemoteException;

  void setAcceptorRMI(final Acceptor acceptorRMI) throws RemoteException;
}
