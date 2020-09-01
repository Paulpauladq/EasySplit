package easysplit.model.server.dao.item;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserItem {

  private final String userName;
  private final String password;
  private final Set<String> teamSet;
}
