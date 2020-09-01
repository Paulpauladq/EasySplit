package easysplit.model.server.paxos.pojo;

import java.io.Serializable;
import java.util.Set;

import easysplit.model.enums.OperationType;
import easysplit.model.pojo.ExpenseItem;
import easysplit.model.pojo.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Operation implements Serializable {

  private OperationType operationType;
  private String userName;
  private String password;
  private String teamName;
  private Set<String> members;
  private ExpenseItem expenseItem;
  private Payment payment;
}
