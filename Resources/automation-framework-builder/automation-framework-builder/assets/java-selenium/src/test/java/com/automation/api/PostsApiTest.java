package com.automation.api;

import com.automation.utils.ConfigReader;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/** API tests need no browser, so they do not extend BaseTest. */
public class PostsApiTest {

    @Test(groups = {"api", "smoke"}, description = "GET /posts/1 returns the post")
    public void getPostReturnsPost() {
        given().baseUri(ConfigReader.get("api.base.url"))
        .when().get("/posts/1")
        .then().statusCode(200)
               .body("id", equalTo(1))
               .body("title", notNullValue());
    }

    @Test(groups = {"api", "regression"}, description = "POST /posts creates a post")
    public void createPostReturns201() {
        given().baseUri(ConfigReader.get("api.base.url"))
               .header("Content-Type", "application/json")
               .body("{\"title\":\"qa\",\"body\":\"demo\",\"userId\":1}")
        .when().post("/posts")
        .then().statusCode(201)
               .body("title", equalTo("qa"));
    }
}
