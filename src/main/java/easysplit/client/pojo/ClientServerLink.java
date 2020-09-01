package easysplit.client.pojo;

import easysplit.model.server.api.EasySplit;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientServerLink {

  private int portConnected;
  private EasySplit easySplit;
}
