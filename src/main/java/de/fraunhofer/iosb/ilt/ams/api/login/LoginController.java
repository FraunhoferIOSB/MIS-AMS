package de.fraunhofer.iosb.ilt.ams.api.login;

import java.io.IOException;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class LoginController {

    @Value("${app.keycloak.login-url}")
    private String loginUrl;

    @Value("${app.keycloak.client-secret}")
    private String clientSecret;

    @Value("${app.keycloak.grant-type}")
    private String grantType;

    @Value("${app.keycloak.client-id}")
    private String clientId;

    @PostMapping(
            value = "/token",
            consumes = {"application/json"})
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/x-www-form-urlencoded");
        okhttp3.RequestBody requestBody =
                okhttp3.RequestBody.create(
                        mediaType,
                        String.format(
                                "username=%s&password=%s&client_id=%s&client_secret=%s&grant_type=%s",
                                loginRequest.getUsername(),
                                loginRequest.getPassword(),
                                clientId,
                                clientSecret,
                                grantType));
        okhttp3.RequestBody formBody =
                new FormBody.Builder()
                        .add("username", loginRequest.getUsername())
                        .add("password", loginRequest.getPassword())
                        .add("client_id", clientId)
                        .add("client_secret", clientSecret)
                        .add("grant_type", grantType)
                        .build();

        Request request =
                new Request.Builder()
                        .url(loginUrl)
                        .post(requestBody)
                        .addHeader("Accept", "*/*")
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();

        try (Response response = client.newCall(request).execute()) {
            var responseBodyString = response.body().string();

            return ResponseEntity.ok(responseBodyString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }
}
