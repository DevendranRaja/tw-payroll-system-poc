package com.tw.coupang.one_payroll.paygroups.controller;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;
import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.service.PayGroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayGroupController.class)
class PayGroupControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PayGroupService payGroupService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        PayGroupService payGroupService() {
            return org.mockito.Mockito.mock(PayGroupService.class);
        }
    }

    @Test
    void createPayGroup_invalidRequest_returnsBadRequest() throws Exception {
        String invalidJson = """
            { "groupName": "" }
        """;

        mockMvc.perform(post("/pay-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.groupName").exists())
                .andExpect(jsonPath("$.errors.paymentCycle").exists())
                .andExpect(jsonPath("$.errors.baseTaxRate").exists())
                .andExpect(jsonPath("$.errors.benefitRate").exists())
                .andExpect(jsonPath("$.errors.deductionRate").exists());;
    }

    @Test
    void createPayGroup_missingRequiredFields_returnsBadRequest() throws Exception {
        String invalidJson = """
            {
              "groupName": "Valid Name"
            }
        """;

        mockMvc.perform(post("/pay-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.paymentCycle").exists())
                .andExpect(jsonPath("$.errors.baseTaxRate").exists())
                .andExpect(jsonPath("$.errors.benefitRate").exists())
                .andExpect(jsonPath("$.errors.deductionRate").exists());
    }

    @Test
    void createPayGroup_negativeDecimalValue_returnsBadRequest() throws Exception {
        String invalidJson = """
            {
              "groupName": "Valid",
              "paymentCycle": "MONTHLY",
              "baseTaxRate": -1,
              "benefitRate": 5,
              "deductionRate": 3
            }
        """;

        mockMvc.perform(post("/pay-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.baseTaxRate").value("baseTaxRate must be >= 0.0"));
    }

    @Test
    void createPayGroup_invalidEnumValue_returnsBadRequest() throws Exception {
        String invalidJson = """
            {
              "groupName": "Valid",
              "paymentCycle": "YEARLY",
              "baseTaxRate": 10,
              "benefitRate": 5,
              "deductionRate": 3
            }
        """;

        mockMvc.perform(post("/pay-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request format."));
    }

    @Test
    void createPayGroup_duplicatePayGroup_returnsConflict() throws Exception {
        when(payGroupService.create(any(PayGroupCreateRequest.class)))
                .thenThrow(new DuplicatePayGroupException("Pay group already exists!"));

        String validJson = """
        {
          "groupName": "Valid Group",
          "paymentCycle": "MONTHLY",
          "baseTaxRate": 10,
          "benefitRate": 5,
          "deductionRate": 3
        }
        """;

        mockMvc.perform(post("/pay-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Pay group already exists!"));
    }

    @Test
    void createPayGroup_unexpectedException_returnsInternalServerError() throws Exception {
        when(payGroupService.create(any(PayGroupCreateRequest.class)))
                .thenThrow(new RuntimeException("Something failed"));

        String validJson = """
        {
          "groupName": "Valid Group",
          "paymentCycle": "MONTHLY",
          "baseTaxRate": 10,
          "benefitRate": 5,
          "deductionRate": 3
        }
        """;

        mockMvc.perform(post("/pay-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value("An unexpected error occurred. Please try again later."));
    }

    @Test
    void createPayGroup_validRequest_returnsCreated() throws Exception {
        when(payGroupService.create(any(PayGroupCreateRequest.class)))
                .thenReturn(PayGroupResponse.builder().payGroupId(1).build());

        String validJson = """
            {
              "groupName": "Valid Group",
              "paymentCycle": "MONTHLY",
              "baseTaxRate": 10,
              "benefitRate": 5,
              "deductionRate": 3
            }
        """;

        mockMvc.perform(post("/pay-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.payGroupId").value(1));
    }
}