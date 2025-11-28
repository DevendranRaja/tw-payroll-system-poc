package com.tw.coupang.one_payroll.paygroups.controller;

import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupCreateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.request.PayGroupUpdateRequest;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupDetailsResponse;
import com.tw.coupang.one_payroll.paygroups.dto.response.PayGroupResponse;
import com.tw.coupang.one_payroll.paygroups.enums.PaymentCycle;
import com.tw.coupang.one_payroll.paygroups.exception.DuplicatePayGroupException;
import com.tw.coupang.one_payroll.paygroups.exception.PayGroupNotFoundException;
import com.tw.coupang.one_payroll.paygroups.service.PayGroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

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
    void createPayGroupWhenInvalidRequestThenReturnsBadRequest() throws Exception {
        String invalidJson = """
            { "groupName": "" }
        """;

        mockMvc.perform(post("/pay-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details.groupName").exists())
                .andExpect(jsonPath("$.details.paymentCycle").exists())
                .andExpect(jsonPath("$.details.baseTaxRate").exists())
                .andExpect(jsonPath("$.details.benefitRate").exists())
                .andExpect(jsonPath("$.details.deductionRate").exists());
    }

    @Test
    void createPayGroupWhenMissingRequiredFieldsThenReturnsBadRequest() throws Exception {
        String invalidJson = """
            {
              "groupName": "Valid Name"
            }
        """;

        mockMvc.perform(post("/pay-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details.paymentCycle").exists())
                .andExpect(jsonPath("$.details.baseTaxRate").exists())
                .andExpect(jsonPath("$.details.benefitRate").exists())
                .andExpect(jsonPath("$.details.deductionRate").exists());
    }

    @Test
    void createPayGroupWhenNegativeDecimalValueThenReturnsBadRequest() throws Exception {
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
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details.baseTaxRate").value("baseTaxRate must be >= 0.0"));
    }

    @Test
    void createPayGroupWhenInvalidEnumValueThenReturnsBadRequest() throws Exception {
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
    void createPayGroupWhenDuplicatePayGroupThenReturnsConflict() throws Exception {
        when(payGroupService.create(any(PayGroupCreateRequest.class)))
                .thenThrow(new DuplicatePayGroupException("Pay group already exists!"));

        String validJson = """
        {
          "groupName": "ValidGroup",
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
    void createPayGroupWhenUnexpectedExceptionThenReturnsInternalServerError() throws Exception {
        when(payGroupService.create(any(PayGroupCreateRequest.class)))
                .thenThrow(new RuntimeException("Something failed"));

        String validJson = """
        {
          "groupName": "ValidGroup",
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
    void createPayGroupWhenValidRequestThenReturnsCreated() throws Exception {
        when(payGroupService.create(any(PayGroupCreateRequest.class)))
                .thenReturn(PayGroupResponse.builder().payGroupId(1).build());

        String validJson = """
        {
          "groupName": "ValidGroup",
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

    @Test
    void updatePayGroupWhenBlankGroupNameIsAllowedAndReturnsOk() throws Exception {
        when(payGroupService.update(eq(1), any(PayGroupUpdateRequest.class)))
                .thenReturn(PayGroupResponse.builder().payGroupId(1).build());

        String json = """
            { "groupName": null }
        """;

        mockMvc.perform(put("/pay-groups/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payGroupId").value(1));
    }

    @Test
    void updatePayGroupWhenInvalidEnumValueThenReturnsBadRequest() throws Exception {
        String invalidJson = """
            {
                "paymentCycle": "YEARLY"
            }
        """;

        mockMvc.perform(put("/pay-groups/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request format."));
    }

    @Test
    void updatePayGroupWhenNegativeDecimalValueThenReturnsBadRequest() throws Exception {
        String invalidJson = """
            {
                "baseTaxRate": -5
            }
        """;

        mockMvc.perform(put("/pay-groups/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details.baseTaxRate").value("baseTaxRate must be >= 0.0"));
    }

    @Test
    void updatePayGroupWhenDuplicateNameThenReturnsConflict() throws Exception {
        when(payGroupService.update(eq(1), any(PayGroupUpdateRequest.class)))
                .thenThrow(new DuplicatePayGroupException("Pay group already exists!"));

        String validJson = """
            {
                "groupName": "Finance",
                "paymentCycle": "MONTHLY",
                "baseTaxRate": 10
            }
        """;

        mockMvc.perform(put("/pay-groups/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Pay group already exists!"));
    }

    @Test
    void updatePayGroupWhenNotFoundThenReturnsNotFound() throws Exception {
        when(payGroupService.update(eq(99), any(PayGroupUpdateRequest.class)))
                .thenThrow(new PayGroupNotFoundException("Pay group with ID '99' not found!"));

        String validJson = """
        {
            "groupName": "UpdatedName",
            "paymentCycle": "MONTHLY"
        }
        """;

        mockMvc.perform(put("/pay-groups/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pay group with ID '99' not found!"));
    }

    @Test
    void updatePayGroupWhenUnexpectedExceptionThenReturnsInternalServerError() throws Exception {
        when(payGroupService.update(eq(1), any(PayGroupUpdateRequest.class)))
                .thenThrow(new RuntimeException("Something failed"));

        String validJson = """
            {
                "groupName": "Name",
                "paymentCycle": "MONTHLY"
            }
        """;

        mockMvc.perform(put("/pay-groups/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value("An unexpected error occurred. Please try again later."));
    }

    @Test
    void updatePayGroupWhenValidRequestThenReturnsOk() throws Exception {
        when(payGroupService.update(eq(1), any(PayGroupUpdateRequest.class)))
                .thenReturn(PayGroupResponse.builder().payGroupId(1).build());

        String validJson = """
        {
            "groupName": "UpdatedGroup",
            "paymentCycle": "MONTHLY"
        }
        """;

        mockMvc.perform(put("/pay-groups/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payGroupId").value(1));
    }

    @Test
    void getAllPayGroupsWhenNoFilterThenReturnsOk() throws Exception {
        when(payGroupService.getAll(null))
                .thenReturn(List.of(
                        PayGroupDetailsResponse.builder()
                                .payGroupId(1)
                                .groupName("Monthly Engineers")
                                .paymentCycle(PaymentCycle.MONTHLY)
                                .build()
                ));

        mockMvc.perform(get("/pay-groups"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    [
                      {
                        "payGroupId": 1,
                        "groupName": "Monthly Engineers",
                        "paymentCycle": "MONTHLY"
                      }
                    ]
                    """));
    }

    @Test
    void getAllPayGroupsWithFilterThenReturnsFiltered() throws Exception {
        when(payGroupService.getAll(PaymentCycle.WEEKLY))
                .thenReturn(List.of(
                        PayGroupDetailsResponse.builder()
                                .payGroupId(2)
                                .groupName("Weekly Engineers")
                                .paymentCycle(PaymentCycle.WEEKLY)
                                .build()
                ));

        mockMvc.perform(get("/pay-groups")
                        .param("paymentCycle", "WEEKLY"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    [
                      {
                        "payGroupId": 2,
                        "groupName": "Weekly Engineers",
                        "paymentCycle": "WEEKLY"
                      }
                    ]
                    """));
    }

    @Test
    void getAllPayGroupsWhenEmptyListThenReturnsOk() throws Exception {
        when(payGroupService.getAll(null))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/pay-groups"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}