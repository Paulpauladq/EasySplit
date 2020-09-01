package easysplit.model.server.paxos.pojo;

import java.io.Serializable;

import easysplit.model.server.paxos.enums.PaxosStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrepareResponse implements Serializable {

  private PaxosStatus paxosStatus;
  private String fullProposalId;
  private String acceptedId;
  private Operation acceptedOperation;

  public int getProposalId() {
    if (null != fullProposalId) {
      return Double.valueOf(fullProposalId).intValue();
    } else {
      return -1;
    }
  }
}
