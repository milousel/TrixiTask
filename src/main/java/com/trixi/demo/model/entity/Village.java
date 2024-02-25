package com.trixi.demo.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity(name = "village")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Village {

    @Id
    @GeneratedValue
    private int id;
    private String code;
    private String name;
}
