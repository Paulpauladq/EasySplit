package easysplit.model.server.paxos.proposer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import easysplit.model.server.paxos.Paxos;
import easysplit.model.server.paxos.acceptor.Acceptor;
import easysplit.model.server.paxos.enums.PaxosStatus;
import easysplit.model.server.paxos.pojo.AcceptResponse;
import easysplit.model.server.paxos.pojo.Operation;
import easysplit.model.server.paxos.pojo.PrepareResponse;
import easysplit.model.server.paxos.pojo.ProposeResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProposerImpl extends UnicastRemoteObject implements Proposer {

  private final Map<Integer, Paxos> portPaxosMap;

  private final int port;

  // proposer
  private int proposalId;

  public ProposerImpl(final Map<Integer, Paxos> portPaxosMap,
                      final int port) throws RemoteException {

    // Create remote object at any available port
    super(port);
    log.info("Stub initializing...");
    // Use concurrent map to ensure thread safe
    this.portPaxosMap = portPaxosMap;
    this.port = port;
    this.proposalId = 0;
    log.info("Stub initialization complete");
  }

  private synchronized void nextProposalId() {

    this.proposalId++;
    log.info("Get nextProposalId: " + this.proposalId);
  }

  @Override
  public synchronized void updateProposalId(int proposalId) throws RemoteException {

    this.proposalId = proposalId;
    log.info("Update ProposalId to: " + this.proposalId);
  }

  @Override  // Phase 1a: Proposer (PREPARE)
  public List<PrepareResponse> prepareNewProposal() throws RemoteException {

    log.info("Proposer prepare to send new proposal...");
    nextProposalId();
    final String fullProposalId = proposalId + "." + port;

    final List<PrepareResponse> responseList = new LinkedList<>();
    // send to all acceptors
    log.info("Proposer sends prepare proposal to all acceptors...");
    for (final Map.Entry<Integer, Paxos> entry : portPaxosMap.entrySet()) {
      final int acceptorPort = entry.getKey();
      try {
        final Acceptor acceptor = entry.getValue().getAcceptorRMI();
        final PrepareResponse response = acceptor.prepare(fullProposalId);
        responseList.add(response);
        log.info("Save response from acceptor at port: " + acceptorPort);
      } catch (Exception e) {
        log.warn("Fail to get response from acceptor at port: " + acceptorPort);
        log.warn("Error: " + e);
      }
    }

    return responseList;
  }

  @Override // Phase 2a: Proposer (PROPOSE)
  public ProposeResponse sendProposal(final int promisedProposalId, final Operation operation) throws RemoteException {

    log.info("Proposer send new proposal...");
    final String fullPromisedProposalId = promisedProposalId + "." + port;

    final List<AcceptResponse> acceptResponseList = new LinkedList<>();
    // send to all acceptors
    log.info("Proposer sends accept proposal to all acceptors...");
    for (final Map.Entry<Integer, Paxos> entry : portPaxosMap.entrySet()) {
      final int acceptorPort = entry.getKey();
      try {
        final Acceptor acceptor = entry.getValue().getAcceptorRMI();
        final AcceptResponse response = acceptor.accept(fullPromisedProposalId, operation);
        acceptResponseList.add(response);
        log.info("Save response from acceptor at port: " + acceptorPort);
      } catch (Exception e) {
        log.warn("Fail to get response from acceptor at port: " + acceptorPort);
        log.warn("Error: " + e);
      }
    }

    // Error: no response from majority instances
    final int majoritySize = portPaxosMap.size() / 2 + 1;
    if (acceptResponseList.size() < majoritySize) {
      log.error("Request Fail: no response from majority instances...");
      throw new IllegalStateException("Request Fail: no response from majority instances...");
    } else {
      final long successCount = acceptResponseList.stream()
              .filter(response -> response.getPaxosStatus() == PaxosStatus.ACCEPTED).count();

      if (successCount < majoritySize) {
        log.warn("Not receive enough ACCEPTED response from acceptor, retry...");
        return ProposeResponse.builder()
                .paxosStatus(PaxosStatus.FAILED)
                .build();
      }

      // reset for next paxos run
      log.info("Reset acceptor for next paxos run...");
      for (final Map.Entry<Integer, Paxos> entry : portPaxosMap.entrySet()) {
        final int acceptorPort = entry.getKey();
        try {
          final Acceptor acceptor = entry.getValue().getAcceptorRMI();
          acceptor.resetForNextPaxosRun();
        } catch (Exception e) {
          log.warn("Fail to reset acceptor at port: " + acceptorPort);
          log.warn("Error: " + e);
        }
      }
      final String result = acceptResponseList.iterator().next().getOperationResult();
      log.info("Result for '" + operation + "'; is '" + result + "'");
      return ProposeResponse.builder()
              .paxosStatus(PaxosStatus.COMPLETE)
              .result(result)
              .build();
    }
  }
}
