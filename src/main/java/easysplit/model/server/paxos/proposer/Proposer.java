package easysplit.model.server.paxos.proposer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import easysplit.model.server.paxos.pojo.Operation;
import easysplit.model.server.paxos.pojo.PrepareResponse;
import easysplit.model.server.paxos.pojo.ProposeResponse;

public interface Proposer extends Remote {

  String REGISTRY_NAME = "Proposer";

  List<PrepareResponse> prepareNewProposal() throws RemoteException;

  ProposeResponse sendProposal(final int promisedProposalId, final Operation operation) throws RemoteException;

  void updateProposalId(final int proposalId) throws RemoteException;
}
