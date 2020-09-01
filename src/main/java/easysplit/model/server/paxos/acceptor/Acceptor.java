package easysplit.model.server.paxos.acceptor;

import java.rmi.Remote;
import java.rmi.RemoteException;

import easysplit.model.server.paxos.pojo.AcceptResponse;
import easysplit.model.server.paxos.pojo.Operation;
import easysplit.model.server.paxos.pojo.PrepareResponse;

public interface Acceptor extends Remote {

  String REGISTRY_NAME = "Acceptor";

  PrepareResponse prepare(final String fullProposalId) throws RemoteException;

  AcceptResponse accept(final String fullProposalId, final Operation operation) throws RemoteException;

  void resetForNextPaxosRun() throws RemoteException;

  String getMaxProposalId() throws RemoteException;
}
