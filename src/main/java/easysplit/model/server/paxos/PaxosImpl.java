package easysplit.model.server.paxos;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;

import easysplit.model.server.paxos.acceptor.Acceptor;
import easysplit.model.server.paxos.acceptor.AcceptorImpl;
import easysplit.model.server.paxos.enums.PaxosStatus;
import easysplit.model.server.paxos.leader.Leader;
import easysplit.model.server.paxos.leader.LeaderImpl;
import easysplit.model.server.paxos.pojo.Operation;
import easysplit.model.server.paxos.pojo.PrepareResponse;
import easysplit.model.server.paxos.pojo.ProposeResponse;
import easysplit.model.server.paxos.proposer.Proposer;
import easysplit.model.server.paxos.proposer.ProposerImpl;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PaxosImpl extends UnicastRemoteObject implements Paxos {

  private final Map<Integer, Paxos> portPaxosMap;

  private final int port;

  // leader
  private Leader leaderRMI;

  // proposer
  private Proposer proposerRMI;

  // acceptor
  private Acceptor acceptorRMI;

  @Override
  public Leader getLeaderRMI() throws RemoteException {

    return leaderRMI;
  }

  @Override
  public Acceptor getAcceptorRMI() throws RemoteException {

    return acceptorRMI;
  }

  @Override
  public synchronized void setAcceptorRMI(final Acceptor acceptorRMI) throws RemoteException {

    this.acceptorRMI = acceptorRMI;
  }

  public PaxosImpl(final Map<Integer, Paxos> portPaxosMap,
                   final int port) throws RemoteException {

    // Create remote object at any available port
    super(port);
    log.info("Stub initializing...");
    // Use concurrent map to ensure thread safe
    this.portPaxosMap = portPaxosMap;
    this.port = port;
    // used by paxos
    this.leaderRMI = new LeaderImpl(portPaxosMap, port);
    this.acceptorRMI = new AcceptorImpl(port);
    this.proposerRMI = new ProposerImpl(portPaxosMap, port);
    log.info("Stub initialization complete");
  }

  private void leaderHealthCheck() throws RemoteException, InterruptedException {

    log.info("Check leader health...");
    try {
      final Leader leader = portPaxosMap.get(leaderRMI.getLeaderPort()).getLeaderRMI();
      if (leader.isAlive()) {
        log.info("Leader health check: good!!!");
        return;
      }
    } catch (RemoteException e) {
      log.warn("No response from leader");
    } catch (Exception e) {
      log.warn("Invalid leader, elect a new one...");
    }

    leaderRMI.startNewElection();
    Thread.sleep(100);
    if (leaderRMI.getLeaderPort() == -1) {
      log.error("All instances are currently down...");
      throw new IllegalStateException("All instances are currently down...");
    } else {
      log.info("Current leader is at port: " + leaderRMI.getLeaderPort());
    }
  }

  @Override
  public String paxosOperation(final Operation operation) throws RemoteException, IllegalArgumentException, InterruptedException {

    log.info("paxosOperation: " + operation + "; received at port: " + port);

    // leader health check -> elect new leader if failed
    leaderHealthCheck();

    // not leader -> proxy to leader
    if (leaderRMI.getLeaderPort() != port) {
      log.info("Proxy request to leader...");
      final Paxos leaderPaxos = portPaxosMap.get(leaderRMI.getLeaderPort());
      return leaderPaxos.paxosOperation(operation);
    }

    // only leader will proceed the following

    // Phase 1a: Proposer (PREPARE)
    List<PrepareResponse> preparedResponseList = proposerRMI.prepareNewProposal();

    log.info("Check all prepare responses to determine the proposed operation...");
    final int majoritySize = portPaxosMap.size() / 2 + 1;

    // Error: no response from majority instances
    if (preparedResponseList.size() < majoritySize) {
      log.error("Request Fail: no response from majority instances...");
      throw new IllegalStateException("Request Fail: no response from majority instances...");
    }

    // Only if enough response received, proceed to decision making
    int promisedCount = 0;
    int promisedProposalId = -1;
    String acceptedId = "0.0";
    Operation acceptedOperation = null;
    for (PrepareResponse response : preparedResponseList) {
      if (PaxosStatus.PROMISED == response.getPaxosStatus()) {
        promisedCount++;
        promisedProposalId = response.getProposalId();
        final String newAcceptedId = response.getAcceptedId();
        if (null != newAcceptedId && Double.valueOf(newAcceptedId).compareTo(Double.valueOf(acceptedId)) > 0) {
          log.warn("Newer proposal has been promised with acceptedId: " + newAcceptedId);
          acceptedId = newAcceptedId;
          acceptedOperation = response.getAcceptedOperation();
        }
      }
    }

    // not receive PROMISE responses from a majority, retry current proposal with high proposalId
    if (promisedCount < majoritySize) {
      log.warn("Receive failure from majority, retry current proposal...");
      proposerRMI.updateProposalId(Double.valueOf(acceptorRMI.getMaxProposalId()).intValue());
      return paxosOperation(operation);
    }

    // Phase 2a: Proposer (PROPOSE)
    // receive PROMISE responses from a majority of acceptors
    if (null == acceptedOperation) {
      // case1.1 no newer proposal promised, use own proposed value
      log.info("Receive promise from majority and no pending paxos run, use own proposal");
      final ProposeResponse response = proposerRMI.sendProposal(promisedProposalId, operation);
      if (PaxosStatus.FAILED == response.getPaxosStatus()) {
        log.info("Retry current paxos run with higher proposalId...");
        return paxosOperation(operation);
      } else {
        return response.getResult();
      }
    } else {
      // case1.2 responses contain accepted values (from other proposals)
      log.warn("Receive promise from majority and has pending paxos run, use pending proposal");
      final ProposeResponse response = proposerRMI.sendProposal(promisedProposalId, acceptedOperation);
      if (PaxosStatus.FAILED == response.getPaxosStatus()) {
        log.info("Retry current paxos run with higher proposalId...");
      } else {
        log.info("Pending paxos run has finished, proceed to current proposal...");
      }
      return paxosOperation(operation);
    }
  }
}
