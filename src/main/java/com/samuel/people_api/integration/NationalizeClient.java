package com.samuel.people_api.integration;

import com.samuel.people_api.config.NationalizeProperties;
import com.samuel.people_api.exception.ExternalServiceException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NationalizeClient {
    private final RestClient restClient;

    public NationalizeClient(NationalizeProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public NationalizeApiResponse findByName(String name) {
        try {
            NationalizeApiResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/").queryParam("name", name).build())
                    .retrieve()
                    .body(NationalizeApiResponse.class);
            if (response == null) {
                throw new ExternalServiceException("A API de nacionalidade retornou uma resposta vazia");
            }
            return response;
        } catch (ExternalServiceException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ExternalServiceException("Não foi possível consultar a API de nacionalidade no momento");
        }
    }
}
