package com.tcc.persistance;

import com.tcc.entity.OrderProduct;
import com.tcc.entity.OrderProductKey;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("OrderProductJpaRepository")
public interface OrderProductRepository extends JpaRepository<OrderProduct, OrderProductKey> {
    @Query("SELECT op FROM OrderProduct op " +
     " WHERE (:lastOrderId IS NULL AND :lastProductId IS NULL) " +
    "  OR (op.orderProductKey.orderId > :lastOrderId) " +
    "  OR (op.orderProductKey.orderId = :lastOrderId AND op.orderProductKey.productId > :lastProductId) " +
    "  ORDER BY op.orderProductKey.orderId ASC, op.orderProductKey.productId ASC")
    List<OrderProduct> findNextKeySet(@Param("lastOrderId")Integer lastOrderId,
                                                       @Param("lastProductId") Integer lastProductId,
                                                       Pageable pageable);
    @Query("""
SELECT op FROM OrderProduct op
WHERE (:lastOrderId IS NULL AND :lastProductId IS NULL)
OR (op.orderProductKey.orderId < :lastOrderId)
OR (op.orderProductKey.orderId = :lastOrderId AND op.orderProductKey.productId  < :lastProductId)
ORDER BY op.orderProductKey.orderId DESC, op.orderProductKey.productId DESC
""")
    List<OrderProduct> findPreviousKeySet(@Param("lastOrderId")Integer lastOrderId,
                                          @Param("lastProductId") Integer lastProductId,
                                          Pageable pageable);
}
