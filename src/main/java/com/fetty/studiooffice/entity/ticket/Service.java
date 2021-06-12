package com.fetty.studiooffice.entity.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "service")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_service_type")
    private ServiceType serviceType;

    @Column
    private String name;

    @Column(name = "approximate_price_grn")
    private Integer approximateGrnPrice;

    @Column
    private Integer count;
}
