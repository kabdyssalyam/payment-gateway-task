package com.checkout.payment.gateway.service.integration;

import com.checkout.payment.gateway.enums.BankClientType;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BankClientFactory {
  private Map<BankClientType, BankClient> clientMap;

  public BankClientFactory(List<BankClient> clients) {
    clientMap = new HashMap<>();

    for (BankClient client: clients) {
      clientMap.put(client.getType(), client);
    }
  }

  public BankClient getClient(BankClientType clientType) {
    BankClient client = clientMap.get(clientType);

    if (client == null) {
      throw new IllegalArgumentException(String.format("Bank client %s is not implemented", clientType));
    }

    return client;
  }
}
