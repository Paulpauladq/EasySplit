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
public class AcceptResponse implements Serializable {

  private PaxosStatus paxosStatus;
  private Operation acceptedOperation;
  private String operationResult;
}
