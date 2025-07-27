package com.timekeeping.utilities;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.timekeeping.entitymodels.Employee;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "secret";

    //Generate JWT Token
    public String generateToken(Employee employee) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

        return JWT.create()
                .withSubject(employee.getEmployeeNo())
                .withClaim("role", employee.getRole())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000))
                .sign(algorithm);
    }

    //Extract claims from the token
    public DecodedJWT extractClaims(String token) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

        //Create verifier
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();

        //Decode and verify the token
        return jwtVerifier.verify(token);
    }

}
