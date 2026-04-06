package com.mycompany.catalog.mcp.utils.testdata;

import com.mycompany.catalog.mcp.model.domain.Product;
import com.mycompany.catalog.mcp.model.domain.ProductPage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for domain test data producers.
 * Contains common functionality shared across all test data producers.
 */
public abstract class BaseDomainTestDataProducer {

  public static final String TXID_FOR_SUCCESS = "779d2950-7f02-440a-a4a3-b36b82ec6c29";
  public static final String TXID_FOR_REST_400_ERROR = "error-rest-400";
  public static final String TXID_FOR_REST_404_ERROR = "error-rest-404";
  public static final String TXID_FOR_REST_500_ERROR = "error-rest-500";

  /**
   * Creates a test product with specified parameters.
   */
  protected static Product generateProduct(String id, String name, String description,
      String category, BigDecimal price, String currency, Boolean inStock, String imageUrl) {

    return Product.builder()
        .id(id)
        .name(name)
        .description(description)
        .category(category)
        .price(price)
        .currency(currency)
        .inStock(inStock)
        .imageUrl(imageUrl)
        .build();
  }

  /**
   * Creates a list of test products.
   */
  protected static List<Product> generateProducts(int count) {
    List<Product> products = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      products.add(generateProduct(
          "prod-" + String.format("%03d", i),
          "Product " + i,
          "Description for product " + i,
          i % 2 == 0 ? "Electronics" : "Office",
          new BigDecimal("10.00").add(new BigDecimal(i * 5)),
          "USD",
          i % 3 != 0,
          "https://example.com/images/product-" + i + ".jpg"
      ));
    }
    return products;
  }

  /**
   * Creates a ProductPage with specified parameters.
   */
  protected static ProductPage generateProductPage(List<Product> products, int page, int pageSize, int totalPages, int totalItems) {
    return ProductPage.builder()
        .products(products)
        .page(page)
        .pageSize(pageSize)
        .totalPages(totalPages)
        .totalItems(totalItems)
        .build();
  }

}
