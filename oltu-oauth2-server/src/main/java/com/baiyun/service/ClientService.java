package com.baiyun.service;



import com.baiyun.model.Client;

import java.util.List;

public interface ClientService {

    Client createClient(Client client);

    Client updateClient(Client client);

    void deleteClient(Long clientId);

    Client findOne(Long clientId);

    List<Client> findAll();

    Client findByClientId(String clientId);

    Client findByClientSecret(String clientSecret);

}
