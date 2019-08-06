/*
 * ****************************************************************************
 *     Cloud Foundry
 *     Copyright (c) [2009-2017] Pivotal Software, Inc. All Rights Reserved.
 *
 *     This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *     You may not use this product except in compliance with the License.
 *
 *     This product includes a number of subcomponents with
 *     separate copyright notices and license terms. Your use of these
 *     subcomponents is subject to the terms and conditions of the
 *     subcomponent's license, as noted in the LICENSE file.
 * ****************************************************************************
 */

package org.cloudfoundry.identity.uaa.mock.limited;

import org.cloudfoundry.identity.uaa.DefaultTestContext;
import org.cloudfoundry.identity.uaa.mock.util.MockMvcUtils;
import org.cloudfoundry.identity.uaa.util.JsonUtils;
import org.cloudfoundry.identity.uaa.web.LimitedModeUaaFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.cloudfoundry.identity.uaa.web.LimitedModeUaaFilter.DEGRADED;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DefaultTestContext
@ActiveProfiles({"default", DEGRADED})
class LimitedModeNegativeTests {
    // To set Predix UAA limited/degraded mode, use environment variable instead of StatusFile

    private String adminToken;

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        FilterChainProxy springSecurityFilterChain = webApplicationContext.getBean("springSecurityFilterChain", FilterChainProxy.class);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(springSecurityFilterChain)
                .build();
        adminToken = MockMvcUtils.getClientCredentialsOAuthAccessToken(mockMvc,
                "admin",
                "adminsecret",
                "uaa.admin",
                null,
                true);
    }

    @BeforeEach
    void ensureLimitedModeContext(@Autowired LimitedModeUaaFilter limitedModeUaaFilter) {
        assertTrue(limitedModeUaaFilter.isEnabled());
    }

    @Test
    void identity_zone_can_read() throws Exception {
        mockMvc.perform(
                get("/identity-zones")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().is(OK.value()));

        mockMvc.perform(
                get("/identity-zones/{id}", "some-invalid-id")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().is(NOT_FOUND.value()));
    }

    @Test
    void identity_zone_can_not_write() throws Exception {
        mockMvc.perform(
                post("/identity-zones")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.writeValueAsString(""))
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("error").value(LimitedModeUaaFilter.ERROR_CODE))
                .andExpect(jsonPath("error_description").value(LimitedModeUaaFilter.ERROR_MESSAGE));

        mockMvc.perform(
                put("/identity-zones/{id}", "some-invalid-id")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.writeValueAsString(""))
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("error").value(LimitedModeUaaFilter.ERROR_CODE))
                .andExpect(jsonPath("error_description").value(LimitedModeUaaFilter.ERROR_MESSAGE));
    }

    @Test
    void identity_provider_can_read() throws Exception {
        mockMvc.perform(
                get("/identity-providers")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().is(OK.value()));

        mockMvc.perform(
                get("/identity-providers/{id}", "some-invalid-id")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().is(NOT_FOUND.value()));
    }

    @Test
    void identity_provider_can_not_write() throws Exception {
        mockMvc.perform(
                post("/identity-providers")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("error").value(LimitedModeUaaFilter.ERROR_CODE))
                .andExpect(jsonPath("error_description").value(LimitedModeUaaFilter.ERROR_MESSAGE));

        mockMvc.perform(
                put("/identity-providers/{id}", "some-invalid-id")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("error").value(LimitedModeUaaFilter.ERROR_CODE))
                .andExpect(jsonPath("error_description").value(LimitedModeUaaFilter.ERROR_MESSAGE));
    }

    @Test
    void clients_can_read() throws Exception {
        mockMvc.perform(
                get("/oauth/clients")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().is(OK.value()));

        mockMvc.perform(
                get("/oauth/clients/{id}", "some-invalid-id")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().is(NOT_FOUND.value()));
    }

    @Test
    void clients_can_not_write() throws Exception {
        mockMvc.perform(
                post("/oauth/clients")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("error").value(LimitedModeUaaFilter.ERROR_CODE))
                .andExpect(jsonPath("error_description").value(LimitedModeUaaFilter.ERROR_MESSAGE));

        mockMvc.perform(
                put("/oauth/clients/{id}", "some-invalid-id")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("error").value(LimitedModeUaaFilter.ERROR_CODE))
                .andExpect(jsonPath("error_description").value(LimitedModeUaaFilter.ERROR_MESSAGE));
    }

    @Test
    void groups_can_read() throws Exception {
        mockMvc.perform(
                get("/Groups")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().is(OK.value()));

        mockMvc.perform(
                get("/Groups/{id}", "some-invalid-id")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().is(NOT_FOUND.value()));
    }

    @Test
    void groups_can_not_write() throws Exception {
        mockMvc.perform(
                post("/Groups")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("error").value(LimitedModeUaaFilter.ERROR_CODE))
                .andExpect(jsonPath("error_description").value(LimitedModeUaaFilter.ERROR_MESSAGE));

        mockMvc.perform(
                put("/Groups/{id}", "some-invalid-id")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("error").value(LimitedModeUaaFilter.ERROR_CODE))
                .andExpect(jsonPath("error_description").value(LimitedModeUaaFilter.ERROR_MESSAGE));
    }

    @Test
    void users_can_read() throws Exception {
        mockMvc.perform(
                get("/Users")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().is(OK.value()));

        mockMvc.perform(
                get("/Users/{id}", "some-invalid-id")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().is(NOT_FOUND.value()));
    }

    @Test
    void users_can_not_write() throws Exception {
        mockMvc.perform(
                post("/Users")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("error").value(LimitedModeUaaFilter.ERROR_CODE))
                .andExpect(jsonPath("error_description").value(LimitedModeUaaFilter.ERROR_MESSAGE));

        mockMvc.perform(
                put("/Users/{id}", "some-invalid-id")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "bearer " + adminToken))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("error").value(LimitedModeUaaFilter.ERROR_CODE))
                .andExpect(jsonPath("error_description").value(LimitedModeUaaFilter.ERROR_MESSAGE));
    }

}
