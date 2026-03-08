package com.buildings.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;

@Builder
@Entity
@Table(name = "invalid_token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvalidTokenEntity {

    @Id
    private String id;

    @Column
    private Date expiryTime;

}

