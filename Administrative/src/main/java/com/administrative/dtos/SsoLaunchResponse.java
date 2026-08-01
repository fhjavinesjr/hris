package com.administrative.dtos;

import java.time.Instant;

public record SsoLaunchResponse(String code, String target, Instant expiresAt) {}
