package com.administrative.dtos;

public record AnnualizationEmployerIdentityDTO(
        String employerName,
        String tin,
        String birBranchCode,
        String rdoCode,
        String address,
        String zipCode) {
}
