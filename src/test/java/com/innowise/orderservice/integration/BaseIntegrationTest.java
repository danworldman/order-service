package com.innowise.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.innowise.orderservice.dao.ItemDAO;
import com.innowise.orderservice.dao.OrderDAO;
import com.innowise.orderservice.model.dto.user.UserResponse;
import com.innowise.orderservice.model.entity.Item;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    protected RestTemplate restTemplate;
    protected static final WireMockServer wireMockServer;

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("order_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    protected ItemDAO itemRepository;

    @Autowired
    protected OrderDAO orderRepository;

    static {
        POSTGRES.start();
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("spring.liquibase.url", POSTGRES::getJdbcUrl);
        registry.add("spring.liquibase.user", POSTGRES::getUsername);
        registry.add("spring.liquibase.password", POSTGRES::getPassword);

        registry.add("user-service.url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeEach
    void setUpBase() {
        restTemplate = createRestTemplate();
        wireMockServer.resetRequests();
        wireMockServer.resetToDefaultMappings();

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    @AfterEach
    void tearDownBase() {
        RequestContextHolder.resetRequestAttributes();
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    protected RestTemplate createRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(HttpClients.createDefault());
        return new RestTemplate(factory);
    }

    protected static final String DEFAULT_ITEM_NAME = "Book";
    protected static final BigDecimal DEFAULT_ITEM_PRICE = new BigDecimal("1000.00");
    protected static final Long NON_EXISTENT_ID = 10000L;
    protected static final Long INVALID_ID = 0L;

    protected Item createItem() {
        Item item = new Item();
        item.setName(DEFAULT_ITEM_NAME);
        item.setPrice(DEFAULT_ITEM_PRICE);
        return itemRepository.save(item);
    }

    protected static final Long DEFAULT_USER_ID = 1L;
    protected static final String DEFAULT_USER_NAME = "Bob";
    protected static final String DEFAULT_USER_SURNAME = "Duck";
    protected static final String DEFAULT_USER_EMAIL = "bob@email.com";

    protected void mockUserService() {
        UserResponse user = new UserResponse(DEFAULT_USER_ID, DEFAULT_USER_NAME, DEFAULT_USER_SURNAME, DEFAULT_USER_EMAIL);

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/api/users/" + DEFAULT_USER_ID))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(toJson(user))));

        wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/users/by-email"))
                .withQueryParam("email", WireMock.equalTo(DEFAULT_USER_EMAIL))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(toJson(user))));
    }

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    protected String toJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}