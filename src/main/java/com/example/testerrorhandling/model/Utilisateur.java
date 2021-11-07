package com.example.testerrorhandling.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.util.Date;

@UserLogInAndMdpCheck
public @Data class Utilisateur {

    @NotNull
    private String login;

    @Size(min = 8, max = 16)
    private String mdp;

    @Past
    private Date dateDeNaissance;

    //Constructeurs, getters et setters
}
