package com.mycompany.product.catalog.utils.testdata;

import com.mycompany.product.catalog.model.domain.Product;
import java.math.BigDecimal;

/**
 * Base class for domain test data producers.
 */
public abstract class BaseDomainTestDataProducer {

  public static final String TXID_FOR_SUCCESS = "779d2950-7f02-440a-a4a3-b36b82ec6c29";
  public static final String TXID_FOR_REST_400_ERROR = "9d47cfe5-4bf3-41a3-abcf-9249e6b3fae4";
  public static final String TXID_FOR_REST_404_ERROR = "6911185b-f4f0-4435-b3e2-0720fc148b63";
  public static final String TXID_FOR_REST_500_ERROR = "6037ac1b-edae-4f82-8a8f-e7724041ae08";

  protected static Product generateProduct(String id, String name, String description,
      String category, double price, String currency, boolean inStock, String imageUrl) {
    return Product.builder()
        .id(id)
        .name(name)
        .description(description)
        .category(category)
        .price(BigDecimal.valueOf(price))
        .currency(currency)
        .inStock(inStock)
        .imageUrl(imageUrl)
        .build();
  }
}
