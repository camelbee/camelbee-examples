package com.mycompany.mapper.api;

import com.mycompany.model.api.mcp.Order;
import com.mycompany.model.domain.OrderItem;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-17T00:33:16+0100",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Eclipse Adoptium)"
)
@ApplicationScoped
public class McpOrderMapperImpl implements McpOrderMapper {

    @Override
    public com.mycompany.model.domain.Order mcpToDomainOrder(Order order) {
        if ( order == null ) {
            return null;
        }

        com.mycompany.model.domain.Order.OrderBuilder order1 = com.mycompany.model.domain.Order.builder();

        if ( order.getId() != null ) {
            order1.id( order.getId() );
        }
        if ( order.getSalesChannel() != null ) {
            order1.salesChannel( order.getSalesChannel() );
        }
        if ( order.getStatus() != null ) {
            order1.status( statusEnumToStatusEnum( order.getStatus() ) );
        }
        if ( order.getOrderDate() != null ) {
            order1.orderDate( order.getOrderDate() );
        }
        if ( order.getLastUpdateTimestamp() != null ) {
            order1.lastUpdateTimestamp( order.getLastUpdateTimestamp() );
        }
        List<OrderItem> list = mcpToDomainOrderItems( order.getItems() );
        if ( list != null ) {
            order1.items( list );
        }

        return order1.build();
    }

    @Override
    public Order domainToMcpOrder(com.mycompany.model.domain.Order order) {
        if ( order == null ) {
            return null;
        }

        Order order1 = new Order();

        if ( order.getId() != null ) {
            order1.setId( order.getId() );
        }
        if ( order.getSalesChannel() != null ) {
            order1.setSalesChannel( order.getSalesChannel() );
        }
        if ( order.getStatus() != null ) {
            order1.setStatus( statusEnumToStatusEnum1( order.getStatus() ) );
        }
        if ( order.getOrderDate() != null ) {
            order1.setOrderDate( order.getOrderDate() );
        }
        if ( order.getLastUpdateTimestamp() != null ) {
            order1.setLastUpdateTimestamp( order.getLastUpdateTimestamp() );
        }
        if ( order.getItems() != null ) {
            for ( OrderItem item : order.getItems() ) {
                order1.addItemsItem( domainToMcpOrderItem( item ) );
            }
        }

        return order1;
    }

    @Override
    public OrderItem mcpToDomainOrderItem(com.mycompany.model.api.mcp.OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        OrderItem.OrderItemBuilder orderItem1 = OrderItem.builder();

        if ( orderItem.getId() != null ) {
            orderItem1.id( orderItem.getId() );
        }
        if ( orderItem.getProductId() != null ) {
            orderItem1.productId( orderItem.getProductId() );
        }
        if ( orderItem.getProductName() != null ) {
            orderItem1.productName( orderItem.getProductName() );
        }
        if ( orderItem.getQuantity() != null ) {
            orderItem1.quantity( orderItem.getQuantity() );
        }
        if ( orderItem.getPrice() != null ) {
            orderItem1.price( orderItem.getPrice() );
        }

        return orderItem1.build();
    }

    @Override
    public com.mycompany.model.api.mcp.OrderItem domainToMcpOrderItem(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        com.mycompany.model.api.mcp.OrderItem orderItem1 = new com.mycompany.model.api.mcp.OrderItem();

        if ( orderItem.getId() != null ) {
            orderItem1.setId( orderItem.getId() );
        }
        if ( orderItem.getProductId() != null ) {
            orderItem1.setProductId( orderItem.getProductId() );
        }
        if ( orderItem.getProductName() != null ) {
            orderItem1.setProductName( orderItem.getProductName() );
        }
        if ( orderItem.getQuantity() != null ) {
            orderItem1.setQuantity( orderItem.getQuantity() );
        }
        if ( orderItem.getPrice() != null ) {
            orderItem1.setPrice( orderItem.getPrice() );
        }

        return orderItem1;
    }

    @Override
    public List<com.mycompany.model.domain.Order> mcpToDomainOrders(List<Order> orders) {
        if ( orders == null ) {
            return null;
        }

        List<com.mycompany.model.domain.Order> list = new ArrayList<com.mycompany.model.domain.Order>( orders.size() );
        for ( Order order : orders ) {
            list.add( mcpToDomainOrder( order ) );
        }

        return list;
    }

    @Override
    public List<Order> domainToMcpOrders(List<com.mycompany.model.domain.Order> orders) {
        if ( orders == null ) {
            return null;
        }

        List<Order> list = new ArrayList<Order>( orders.size() );
        for ( com.mycompany.model.domain.Order order : orders ) {
            list.add( domainToMcpOrder( order ) );
        }

        return list;
    }

    @Override
    public List<OrderItem> mcpToDomainOrderItems(List<com.mycompany.model.api.mcp.OrderItem> orderItems) {
        if ( orderItems == null ) {
            return null;
        }

        List<OrderItem> list = new ArrayList<OrderItem>( orderItems.size() );
        for ( com.mycompany.model.api.mcp.OrderItem orderItem : orderItems ) {
            list.add( mcpToDomainOrderItem( orderItem ) );
        }

        return list;
    }

    @Override
    public List<com.mycompany.model.api.mcp.OrderItem> domainToMcpOrderItems(List<OrderItem> orderItems) {
        if ( orderItems == null ) {
            return null;
        }

        List<com.mycompany.model.api.mcp.OrderItem> list = new ArrayList<com.mycompany.model.api.mcp.OrderItem>( orderItems.size() );
        for ( OrderItem orderItem : orderItems ) {
            list.add( domainToMcpOrderItem( orderItem ) );
        }

        return list;
    }

    protected com.mycompany.model.domain.Order.StatusEnum statusEnumToStatusEnum(Order.StatusEnum statusEnum) {
        if ( statusEnum == null ) {
            return null;
        }

        com.mycompany.model.domain.Order.StatusEnum statusEnum1;

        switch ( statusEnum ) {
            case PENDING: statusEnum1 = com.mycompany.model.domain.Order.StatusEnum.PENDING;
            break;
            case CONFIRMED: statusEnum1 = com.mycompany.model.domain.Order.StatusEnum.CONFIRMED;
            break;
            case PROCESSING: statusEnum1 = com.mycompany.model.domain.Order.StatusEnum.PROCESSING;
            break;
            case SHIPPED: statusEnum1 = com.mycompany.model.domain.Order.StatusEnum.SHIPPED;
            break;
            case DELIVERED: statusEnum1 = com.mycompany.model.domain.Order.StatusEnum.DELIVERED;
            break;
            case COMPLETED: statusEnum1 = com.mycompany.model.domain.Order.StatusEnum.COMPLETED;
            break;
            case CANCELED: statusEnum1 = com.mycompany.model.domain.Order.StatusEnum.CANCELED;
            break;
            case RETURNED: statusEnum1 = com.mycompany.model.domain.Order.StatusEnum.RETURNED;
            break;
            case FAILED: statusEnum1 = com.mycompany.model.domain.Order.StatusEnum.FAILED;
            break;
            case ON_HOLD: statusEnum1 = com.mycompany.model.domain.Order.StatusEnum.ON_HOLD;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + statusEnum );
        }

        return statusEnum1;
    }

    protected Order.StatusEnum statusEnumToStatusEnum1(com.mycompany.model.domain.Order.StatusEnum statusEnum) {
        if ( statusEnum == null ) {
            return null;
        }

        Order.StatusEnum statusEnum1;

        switch ( statusEnum ) {
            case PENDING: statusEnum1 = Order.StatusEnum.PENDING;
            break;
            case CONFIRMED: statusEnum1 = Order.StatusEnum.CONFIRMED;
            break;
            case PROCESSING: statusEnum1 = Order.StatusEnum.PROCESSING;
            break;
            case SHIPPED: statusEnum1 = Order.StatusEnum.SHIPPED;
            break;
            case DELIVERED: statusEnum1 = Order.StatusEnum.DELIVERED;
            break;
            case COMPLETED: statusEnum1 = Order.StatusEnum.COMPLETED;
            break;
            case CANCELED: statusEnum1 = Order.StatusEnum.CANCELED;
            break;
            case RETURNED: statusEnum1 = Order.StatusEnum.RETURNED;
            break;
            case FAILED: statusEnum1 = Order.StatusEnum.FAILED;
            break;
            case ON_HOLD: statusEnum1 = Order.StatusEnum.ON_HOLD;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + statusEnum );
        }

        return statusEnum1;
    }
}
