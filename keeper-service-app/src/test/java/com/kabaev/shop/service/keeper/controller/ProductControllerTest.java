package com.kabaev.shop.service.keeper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kabaev.shop.service.keeper.domain.Product;
import com.kabaev.shop.service.keeper.dto.ProductDto;
import com.kabaev.shop.service.keeper.dto.ProductDtoList;
import com.kabaev.shop.service.keeper.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    private final ProductRepository productRepository;
    private final MockMvc mockMvc;
    private final ObjectMapper mapper;

    private ProductDto productDto;

    @Autowired
    public ProductControllerTest(
            ProductRepository productRepository,
            MockMvc mockMvc,
            ObjectMapper mapper) {
        this.productRepository = productRepository;
        this.mockMvc = mockMvc;
        this.mapper = mapper;
    }

    @BeforeEach
    void setUp() {
        Product product = new Product();
        product.setCode("7dd7360f-af3f-42a2-8615-b11dc7b69b2b");
        product.setName("AMD Ryzen 7 PRO 5750G, SocketAM4, OEM");
        product.setDescription("Product description");
        product.setPrice(BigDecimal.valueOf(500));
        product.setIsDeleted(false);

        Product saved = productRepository.save(product);

        productDto = new ProductDto(saved);
    }

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
    }

    @Test
    void getAllProducts_ReturnAllProducts() throws Exception {
        // given
        ProductDtoList productDtoList = new ProductDtoList(
                Collections.singletonList(productDto)
        );

        // then
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(productDtoList)));
    }

    @Test
    void getProductByCode_ReturnProduct_IfProductWithCodeExists() throws Exception {
        // given
        String code = "7dd7360f-af3f-42a2-8615-b11dc7b69b2b";

        // then
        mockMvc.perform(get("/api/v1/products/" + code))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(productDto)));
    }

    @Test
    void getProductByCode_ThrowException_IfProductWithCodeDoesNotExist() throws Exception {
        // given
        String code = "8d0000f-af3f-42a2-8615-b11dc7b69b2b";

        // then
        mockMvc.perform(get("/api/v1/products/" + code))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(
                        "There is no product with the code: " + code,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()
                ));
    }

//    @Test
//    void deleteProductByCode_DeleteProduct_IfProductWithCodeExist() throws Exception {
//        // given
//        String code = "7dd7360f-af3f-42a2-8615-b11dc7b69b2b";
//
//        // then
//        mockMvc.perform(delete("/api/v1/products/" + code))
//                .andExpect(status().isOk());
//    }

    @Test
    void deleteProductByCode_ThrowException_IfProductWithCodeDoesNotExist() throws Exception {
        // given
        String code = "8d0000f-af3f-42a2-8615-b11dc7b69b2b";

        // then
        mockMvc.perform(delete("/api/v1/products/" + code))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(
                        "There is no product with the code: " + code,
                        Objects.requireNonNull(result.getResolvedException()).getMessage()
                ));
    }

}