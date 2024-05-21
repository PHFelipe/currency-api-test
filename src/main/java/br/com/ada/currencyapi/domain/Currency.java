package br.com.ada.currencyapi.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Currency implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "exchanges",
            joinColumns = {@JoinColumn(name = "currency_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "currency_name")
    private Map<String, BigDecimal> exchanges;

}
