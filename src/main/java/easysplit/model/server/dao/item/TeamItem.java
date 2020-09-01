package easysplit.model.server.dao.item;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import easysplit.model.pojo.ExpenseItem;
import easysplit.model.pojo.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamItem {

  private final String teamName;
  private final Set<String> memberSet;
  private final List<ExpenseItem> history;
  // user -> (member, payment)
  private final Map<String, Map<String, Payment>> snapshot;
  // user -> (member -> (id -> payment))
  private final Map<String, Map<String, Map<UUID, Payment>>> paymentHistory;
}
