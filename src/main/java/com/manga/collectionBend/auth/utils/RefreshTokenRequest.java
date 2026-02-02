package com.manga.collectionBend.auth.utils;

import lombok.*;

// Lombok is working after updating IntelliJ IDEA(v2025.3.3) and so using its common methods to optimize code base
// About Lombok - 1) The Lombok Java dependency is a library that automates the generation of boilerplate code—such as getters, setters, constructors, toString(), builder()
// and 2) equals()—at compile-time using annotations. It reduces clutter, improves code readability, and minimizes manual coding errors.
// 3) It is typically added to Maven/Gradle projects as a provided dependency.
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// This file used when we need new Jwt Access Token when valid RefreshToken is provided to our var
public class RefreshTokenRequest {
    private String refreshToken;

//    but we can use our Manual Method creations if Lombok not working
//    and which improves Developer Experience(DX/UX) to know which method of this class is being used by other class file
}
