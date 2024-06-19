package tests;

import io.restassured.RestAssured;
import models.lombok.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static specs.ReqresSpec.*;

public class ReqresTests extends TestBase {

    @DisplayName("Check that user data are received")
    @Tag("API")
    @Test
    void fetchUserInfoTest() {
        GetUserResponseModel httpResponse = step("Fetch user data", () ->
                given(requestSpec)
                        .when()
                        .get("users/{id}", 1)
                        .then()
                        .spec(okResponseSpec)
                        .extract().as(GetUserResponseModel.class));
        step("Check that id is equal to expected value", () ->
                assertThat(httpResponse.getData().getId(), equalTo(1)));
        step("Check that first_name = George", () ->
                assertThat(httpResponse.getData().getFirst_name(), equalTo("George")));
        step("Check that email = george.bluth@reqres.in", () ->
                assertThat(httpResponse.getData().getEmail(), equalTo("george.bluth@reqres.in")));
    }

    @DisplayName("Check that new user can be created")
    @Tag("API")
    @Test
    void createNewUserTest() {
        CreateUserBodyModel user = new CreateUserBodyModel();
        user.setName("Vlad");
        user.setJob("QA");
        CreateUserResponseModel httpResponse = step("Create new user", () ->
                given(requestSpec)
                        .body(user)
                        .when()
                        .post("/users/")
                        .then()
                        .spec(createdResponseSpec)
                        .extract().as(CreateUserResponseModel.class));
        step("Check that name equal to name from request", () ->
                assertThat(httpResponse.getName(), equalTo(user.getName())));
        step("Check that job equal to job from request", () ->
                assertThat(httpResponse.getJob(), equalTo(user.getJob())));
        step("Check that id is not null", () ->
                assertThat(httpResponse.getId(), notNullValue()));
        given(requestSpec).delete("users/" + httpResponse.getId());
    }

    @DisplayName("Check that user can be deleted")
    @Tag("API")
    @Test
    void deleteUserTest() {
        CreateUserBodyModel user = new CreateUserBodyModel();
        user.setName("Ivan");
        user.setJob("DevOps");

        CreateUserResponseModel httpResponse = step("Create new user", () ->
                given(requestSpec)
                        .body(user)
                        .when()
                        .post("/users/")
                        .then()
                        .spec(createdResponseSpec)
                        .extract().as(CreateUserResponseModel.class));
        String deletedRes = step("Delete created user", () ->
                RestAssured.given(requestSpec)
                        .delete("users/" + httpResponse.getId())
                        .then()
                        .spec(noContentResponseSpec)
                        .extract().asString()
        );
        System.out.println(deletedRes);
        step("Check that empty response body is returned", () ->
                assertThat(deletedRes, equalTo("")));
    }

    @Test
    @DisplayName("Check that error is returned when user can't register")
    @Tag("API")
    void unsuccessfulRegistrationTest() {
        LoginBodyLombokModel regData = new LoginBodyLombokModel();
        regData.setEmail("vlad@reqres.in");
        regData.setPassword("test");
        BadRequestModel httpResponse = step("Send POST /register request with forbidden email and password", () ->
                given(requestSpec)
                        .body(regData)
                        .when()
                        .post("/register")
                        .then()
                        .spec(badRequestResponseSpec)
                        .extract().as(BadRequestModel.class));
        step("Check that error message is returned", () ->
                assertThat(httpResponse.getError(), equalTo("Note: Only defined users succeed registration")));
    }

    @Test
    @DisplayName("Check that not existed user can't login")
    @Tag("API")
    void notExistingUserLoginTest() {
        LoginBodyLombokModel authData = new LoginBodyLombokModel();
        authData.setEmail("test@reqres.in");
        authData.setPassword("test");

        BadRequestModel httpResponse = step("Send POST /login request with not existing email and password", () ->
                given(requestSpec)
                        .body(authData)
                        .when()
                        .post("/login")
                        .then()
                        .spec(badRequestResponseSpec)
                        .extract().as(BadRequestModel.class));
        step("Check that error " + "'User not found'" + " is returned", () ->
                assertThat(httpResponse.getError(), equalTo("user not found")));
    }

    @Test
    @DisplayName("Check that login with empty body returns error")
    @Tag("API")
    void loginWithEmptyDataTest() {
        BadRequestModel httpResponse = step("Send POST /login with empty credentials", () ->
                given(requestSpec)
                        .post("/login")
                        .then()
                        .spec(badRequestResponseSpec)
                        .extract().as(BadRequestModel.class));
        step("Check that error " + "'Missing email or username'" + " is returned", () ->
                assertThat(httpResponse.getError(), equalTo("Missing email or username")));
    }
}
