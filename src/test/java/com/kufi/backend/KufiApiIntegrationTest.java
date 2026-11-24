package com.kufi.backend;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kufi.backend.appointment.AppointmentRepository;
import com.kufi.backend.shop.BarberShopRepository;
import com.kufi.backend.shop.ServiceOfferingRepository;
import com.kufi.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.time.LocalDate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc
class KufiApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired AppointmentRepository appointments;
    @Autowired ServiceOfferingRepository services;
    @Autowired BarberShopRepository shops;
    @Autowired UserRepository users;
    private String customerToken, ownerToken, otherCustomerToken;
    private long shopId, serviceId;
    @BeforeEach void setup() throws Exception {
        appointments.deleteAll(); services.deleteAll(); shops.deleteAll(); users.deleteAll();
        customerToken=register("customer@test.local","CUSTOMER");
        otherCustomerToken=register("other@test.local","CUSTOMER");
        ownerToken=register("barber@test.local","BARBER");
        MvcResult shop=postJson("/api/v1/shops",ownerToken,Map.of("name","Kufi Studio","city","Istanbul",
               "district","Gungoren","address","Test Street","openingTime","09:00","closingTime","18:00"));
        assertEquals(201,shop.getResponse().getStatus());
        shopId=body(shop).get("shopId").asLong();
        MvcResult offer=postJson("/api/v1/shops/"+shopId+"/services",ownerToken,Map.of("name","Haircut",
               "durationMinutes",45,"price",350));
        assertEquals(201,offer.getResponse().getStatus());
        serviceId=body(offer).get("serviceId").asLong();
    }
    @Test void T06_registerHashesPasswordAndDuplicateEmailRejected() throws Exception {
        assertTrue(users.findByEmailIgnoreCase("customer@test.local").orElseThrow().passwordHash.startsWith("$2"));
        assertNotEquals("TestPassword123!",users.findByEmailIgnoreCase("customer@test.local").orElseThrow().passwordHash);
        assertEquals(409,postJson("/api/v1/auth/register",null,Map.of("email","customer@test.local",
                    "password","TestPassword123!","phone","5551112233","role","CUSTOMER")).getResponse().getStatus());
    }
    @Test void T07_invalidLoginAndNoTokenRejected() throws Exception {
        assertEquals(401,postJson("/api/v1/auth/login",null,Map.of("email","customer@test.local","password","wrongpassword"))
               .getResponse().getStatus());
        mvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/users/me").header("Authorization","Bearer bad.token.value"))
            .andExpect(status().isUnauthorized());
    }
    @Test void T08_districtSearchAndRoleGuard() throws Exception {
        mvc.perform(get("/api/v1/shops").param("district","gUNgoRen"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Kufi Studio"));
        assertEquals(403,postJson("/api/v1/shops",customerToken,Map.of("name","Blocked","city","Istanbul",
               "district","Fatih","address","Street","openingTime","09:00","closingTime","18:00"))
               .getResponse().getStatus());
    }
    @Test void T09_negativePriceRejected() throws Exception {
        assertEquals(400,postJson("/api/v1/shops/"+shopId+"/services",ownerToken,
             Map.of("name","Invalid","durationMinutes",30,"price",-1)).getResponse().getStatus());
    }
    @Test void T10_validBookingAndOverlapConflict() throws Exception {
        MvcResult good=book(customerToken,time(14,0));
        assertEquals(201,good.getResponse().getStatus());
        assertTrue(body(good).get("endTime").asText().contains("14:45"));
        assertEquals(409,book(otherCustomerToken,time(14,30)).getResponse().getStatus());
        assertEquals(1,appointments.count());
        mvc.perform(get("/api/v1/appointments/my").header("Authorization","Bearer "+customerToken))
           .andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }
    @Test void T11_adjacentBookingAllowedAndPastDateRejected() throws Exception {
        assertEquals(201,book(customerToken,time(14,0)).getResponse().getStatus());
        assertEquals(201,book(otherCustomerToken,time(14,45)).getResponse().getStatus());
        assertEquals(400,book(otherCustomerToken,LocalDate.now().minusDays(1).atTime(14,0).toString())
             .getResponse().getStatus());
    }
    @Test void T12_cancelAndRebookAndPreventUnauthorizedCancel() throws Exception {
        long appointmentId=body(book(customerToken,time(14,0))).get("appointmentId").asLong();
        mvc.perform(patch("/api/v1/appointments/"+appointmentId+"/cancel")
                .header("Authorization","Bearer "+otherCustomerToken)).andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/appointments/"+appointmentId+"/cancel")
                .header("Authorization","Bearer "+customerToken)).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        assertEquals(201,book(otherCustomerToken,time(14,0)).getResponse().getStatus());
    }
    private String time(int h,int m) {return LocalDate.now().plusDays(7).atTime(h,m).toString();}
    private String register(String email,String role) throws Exception {
        MvcResult result=postJson("/api/v1/auth/register",null,
            Map.of("email",email,"password","TestPassword123!","phone","5551112233","role",role));
        assertEquals(201,result.getResponse().getStatus());
        return body(result).get("accessToken").asText();
    }
    private MvcResult book(String token,String start) throws Exception {
        return postJson("/api/v1/appointments",token,Map.of("shopId",shopId,"serviceId",serviceId,"startTime",start));
    }
    private MvcResult postJson(String url,String token,Object data) throws Exception {
        var request=post(url).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(data));
        if(token!=null) request.header("Authorization","Bearer "+token);
        return mvc.perform(request).andReturn();
    }
    private JsonNode body(MvcResult response) throws Exception {return mapper.readTree(response.getResponse().getContentAsString());}
}
